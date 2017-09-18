package org.jnanaprabodhini.happyteacher.activity

import android.content.Intent
import android.os.Bundle
import android.support.annotation.IntegerRes
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.AdapterView
import android.widget.TextView
import com.firebase.ui.database.FirebaseListAdapter
import com.firebase.ui.database.FirebaseRecyclerAdapter
import kotlinx.android.synthetic.main.activity_board_lessons.*
import org.jnanaprabodhini.happyteacher.BoardChoiceDialog
import org.jnanaprabodhini.happyteacher.R
import org.jnanaprabodhini.happyteacher.activity.parent.BottomNavigationActivity
import org.jnanaprabodhini.happyteacher.adapter.FirebaseEmptyRecyclerAdapter
import org.jnanaprabodhini.happyteacher.extension.jiggle
import org.jnanaprabodhini.happyteacher.extension.setVisibilityGone
import org.jnanaprabodhini.happyteacher.extension.setVisible
import org.jnanaprabodhini.happyteacher.extension.showSnackbar
import org.jnanaprabodhini.happyteacher.model.Subject
import org.jnanaprabodhini.happyteacher.model.SyllabusLesson
import org.jnanaprabodhini.happyteacher.prefs
import org.jnanaprabodhini.happyteacher.viewholder.SyllabusLessonViewHolder


class BoardLessonsActivity : BottomNavigationActivity() {

    @IntegerRes override val bottomNavigationMenuItemId: Int = R.id.navigation_board

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_lessons)

        bottomNavigation.selectedItemId = bottomNavigationMenuItemId
        bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        val layoutManager = LinearLayoutManager(this)
        syllabusLessonsRecyclerView.layoutManager = layoutManager

        // Leave space between each item in the list:
        val dividerItemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        dividerItemDecoration.setDrawable(ResourcesCompat.getDrawable(resources, R.drawable.divider_vertical, null)!!)
        syllabusLessonsRecyclerView.addItemDecoration(dividerItemDecoration)

        initializeSpinners()

        if (!prefs.hasChosenBoard()) {
            // Prompt the user to select which board they would like
            //  to see syllabus lesson plans from.
            showBoardChooser()
        }
    }

    override fun onBottomNavigationItemReselected() {
        syllabusLessonsRecyclerView.smoothScrollToPosition(0)
    }

    private fun showBoardChooser() {
        val dialog = BoardChoiceDialog(this)
        dialog.setOnDismissListener {
            // Re-initialize spinners after board is chosen.
            initializeSpinners()
        }
        dialog.show()
    }

    private fun initializeSpinners() {
        val subjectQuery = databaseReference.child(getString(R.string.subjects))
                .orderByChild(getString(R.string.is_active))
                .equalTo(true)

        val subjectSpinnerAdapter = object : FirebaseListAdapter<Subject>(this, Subject::class.java, R.layout.spinner_item, subjectQuery) {
            override fun populateView(view: View, subject: Subject, position: Int) {
                (view as TextView).text = subject.name
            }
        }

        val levelQuery = databaseReference.child(getString(R.string.levels)).orderByValue().equalTo(true)

        val levelSpinnerAdapter = object : FirebaseListAdapter<Boolean>(this, Boolean::class.java, R.layout.spinner_item, levelQuery) {
            override fun populateView(view: View, level: Boolean, position: Int) {
                try {
                    val levelNumber = Integer.parseInt(this.getRef(position).key)
                    (view as TextView).text = getString(R.string.standard_n, levelNumber)
                } catch (e: NumberFormatException) {
                    (view as TextView).text = this.getRef(position).key
                }
            }
        }

        subjectSpinner.adapter = subjectSpinnerAdapter
        levelSpinner.adapter = levelSpinnerAdapter

        val spinnerSelectionListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedSubjectPosition = subjectSpinner.selectedItemPosition
                val selectedLevelPosition = levelSpinner.selectedItemPosition

                val selectedSubjectKey = subjectSpinnerAdapter.getRef(selectedSubjectPosition).key
                val selectedLevel = levelSpinnerAdapter.getRef(selectedLevelPosition).key

                updateSyllabusLessonList(selectedSubjectKey, selectedLevel)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        subjectSpinner.onItemSelectedListener = spinnerSelectionListener
        levelSpinner.onItemSelectedListener = spinnerSelectionListener
    }

    private fun  updateSyllabusLessonList(selectedSubjectKey: String, selectedLevel: String) {
        val syllabusLessonQuery = databaseReference.child(getString(R.string.syllabus_lessons))
                .child(prefs.getBoardKey())
                .child(selectedSubjectKey)
                .child(selectedLevel)
                .orderByChild(getString(R.string.lesson_number))

        val syllabusLessonAdapter = object: FirebaseEmptyRecyclerAdapter<SyllabusLesson, SyllabusLessonViewHolder>(SyllabusLesson::class.java, R.layout.list_item_syllabus_lesson, SyllabusLessonViewHolder::class.java, syllabusLessonQuery) {
            override fun onEmpty() {
                // Show empty view
                emptySyllabusLessonsTextView.setVisible()
            }

            override fun onNonEmpty() {
                // Hide empty view
                emptySyllabusLessonsTextView.setVisibilityGone()

                // Animate layout changes
                syllabusLessonsRecyclerView.scheduleLayoutAnimation()
                syllabusLessonsRecyclerView.invalidate()
            }

            override fun populateViewHolder(syllabusLessonViewHolder: SyllabusLessonViewHolder?, syllabusLessonModel: SyllabusLesson?, syllabusLessonPosition: Int) {
                syllabusLessonViewHolder?.lessonTitleTextView?.text = syllabusLessonModel?.name
                syllabusLessonViewHolder?.lessonNumberTextView?.text = syllabusLessonModel?.lessonNumber.toString()
                syllabusLessonViewHolder?.topicCountTextView?.text = resources.getQuantityString(R.plurals.topics_count, syllabusLessonModel?.topicCount ?: 0, syllabusLessonModel?.topicCount ?: 0)

                syllabusLessonViewHolder?.itemView?.setOnClickListener {

                    val topicCount = syllabusLessonModel?.topicCount ?: 0
                    if (topicCount == 0) {
                        // If there are no topics to display, jiggle the count and tell the user.
                        syllabusLessonViewHolder.topicCountTextView.jiggle()
                        rootLayout.showSnackbar(R.string.this_lesson_has_no_relevant_topics)
                    } else {
                        // Pass syllabus lesson data to the TopicsListActivity
                        //  so that it can display the relevant topics (instead
                        //  of all topics for that subject).

                        val topicsListIntent = Intent(this@BoardLessonsActivity, TopicsListActivity::class.java)
                        val keyUrl = getRef(syllabusLessonPosition).child(getString(R.string.topics)).toString()
                        val subject = syllabusLessonModel?.subject
                        val level = syllabusLessonModel?.level
                        val title = syllabusLessonModel?.name

                        topicsListIntent.putExtra(TopicsListActivity.EXTRA_TOPICS_KEY_URL, keyUrl)
                        topicsListIntent.putExtra(TopicsListActivity.EXTRA_SUBJECT_NAME, subject)
                        topicsListIntent.putExtra(TopicsListActivity.EXTRA_LESSON_TITLE, title)
                        topicsListIntent.putExtra(TopicsListActivity.EXTRA_LEVEL, level)

                        startActivity(topicsListIntent)
                    }
                }
            }
        }

        syllabusLessonsRecyclerView.adapter = syllabusLessonAdapter
    }
}

