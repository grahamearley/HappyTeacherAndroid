package org.jnanaprabodhini.happyteacher.activity

import android.os.Bundle
import android.support.annotation.IntegerRes
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.TextView
import com.firebase.ui.database.FirebaseListOptions
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.android.synthetic.main.activity_board_lessons.*
import org.jnanaprabodhini.happyteacher.dialog.BoardChoiceDialog
import org.jnanaprabodhini.happyteacher.R
import org.jnanaprabodhini.happyteacher.activity.base.BottomNavigationActivity
import org.jnanaprabodhini.happyteacher.adapter.firebase.FirebaseObserverListAdapter
import org.jnanaprabodhini.happyteacher.adapter.firebase.SyllabusLessonRecyclerAdapter
import org.jnanaprabodhini.happyteacher.adapter.helper.FirebaseDataObserver
import org.jnanaprabodhini.happyteacher.extension.*
import org.jnanaprabodhini.happyteacher.model.Subject
import org.jnanaprabodhini.happyteacher.model.SyllabusLesson
import org.jnanaprabodhini.happyteacher.prefs


class BoardLessonsActivity : BottomNavigationActivity(), FirebaseDataObserver {

    @IntegerRes override val bottomNavigationMenuItemId: Int = R.id.navigation_board

    object SavedInstanceStateConstants {
        val LEVEL_SPINNER_SELECTION = "LEVEL_SPINNER_SELECTION"
        val SUBJECT_SPINNER_SELECTION = "SUBJECT_SPINNER_SELECTION"
    }

    private var levelSpinnerSelectionIndex = 0
    private var subjectSpinnerSelectionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_board_lessons)

        bottomNavigation.selectedItemId = bottomNavigationMenuItemId
        bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        savedInstanceState?.let { setSpinnerSelectionIndicesFromSavedInstanceState(it) }

        setupRecyclerView()

        if (!prefs.hasChosenBoard()) {
            // Prompt the user to select which board they would like
            //  to see syllabus lesson plans from.
            showBoardChooser()
        } else {
            initializeUi()
        }
    }

    private fun showBoardChooser() {
        val dialog = BoardChoiceDialog(this)
        dialog.setOnDismissListener {
            clearAdapters()
            initializeUi()
        }
        dialog.show()
    }

    private fun setSpinnerSelectionIndicesFromSavedInstanceState(savedInstanceState: Bundle) {
        val levelSpinnerStoredSelection = savedInstanceState.getInt(SavedInstanceStateConstants.LEVEL_SPINNER_SELECTION, 0)
        val subjectSpinnerStoredSelection = savedInstanceState.getInt(SavedInstanceStateConstants.SUBJECT_SPINNER_SELECTION, 0)

        this.levelSpinnerSelectionIndex = levelSpinnerStoredSelection
        this.subjectSpinnerSelectionIndex = subjectSpinnerStoredSelection
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        syllabusLessonsRecyclerView.layoutManager = layoutManager

        // Leave space between each item in the list:
        val dividerItemDecoration = DividerItemDecoration(this, layoutManager.orientation)
        dividerItemDecoration.setDrawable(ResourcesCompat.getDrawable(resources, R.drawable.divider_vertical, null)!!)
        syllabusLessonsRecyclerView.addItemDecoration(dividerItemDecoration)
    }

    override fun onBottomNavigationItemReselected() {
        syllabusLessonsRecyclerView.smoothScrollToPosition(0)
    }

    private fun initializeUi() {
        boardLessonsProgressBar.setVisible()
        setupSubjectSpinner()
    }

    private fun setupSubjectSpinner() {
        // Get an index list of subjects that are used by the currently active board:
        val boardSubjectIndexQuery = databaseReference.child(getString(R.string.boards))
                .child(prefs.getBoardKey())
                .child(getString(R.string.subjects))

        val subjectRef = databaseReference.child(getString(R.string.subjects))

        val subjectSpinnerAdapterOptions = FirebaseListOptions.Builder<Subject>()
                .setIndexedQuery(boardSubjectIndexQuery, subjectRef, Subject::class.java)
                .setLayout(R.layout.spinner_item).build()

        val subjectDataObserver = object: FirebaseDataObserver {
            override fun onDataNonEmpty() {
                setSpinnersVisible()
                boardLessonsProgressBar.setVisibilityGone()
            }
        }

        val boardSubjectSpinnerAdapter = object : FirebaseObserverListAdapter<Subject>(subjectSpinnerAdapterOptions, subjectDataObserver) {
            override fun populateView(view: View, subject: Subject, position: Int) {
                (view as TextView).text = subject.name
            }
        }

        boardSubjectSpinnerAdapter.startListening()

        // The level spinner depends on what subject is selected:
        subjectSpinner.onItemSelected { pos -> setupLevelSpinnerForSubject(boardSubjectSpinnerAdapter.getRef(pos).key) }

        subjectSpinner.adapter = boardSubjectSpinnerAdapter
        subjectSpinner.selectIndexWhenPopulated(subjectSpinnerSelectionIndex)
    }

    private fun setupLevelSpinnerForSubject(subjectKey: String) {
        val previousSelection = levelSpinner.selectedItem

        val levelRef = databaseReference.child(getString(R.string.levels))

        // Get an index list of levels that are used by the currently active board:
        val boardLevelIndexQuery = databaseReference.child(getString(R.string.boards))
                                                    .child(prefs.getBoardKey())
                                                    .child(getString(R.string.subjects))
                                                    .child(subjectKey)

        // Observe data in this spinner, and if the previously selected item
        //  is loaded into the data, then select that item.
        val levelDataObserver = object: FirebaseDataObserver {
            override fun onDataNonEmpty() {
                if (previousSelection != null && previousSelection is Int) {
                    val indexOfPreviousSelection = levelSpinner.items().indexOf(previousSelection)
                    if (indexOfPreviousSelection != -1) {
                        levelSpinner.setSelection(indexOfPreviousSelection, true)
                    }
                }
            }
        }

        val boardLevelSpinnerAdapterOptions = FirebaseListOptions.Builder<Int>()
                .setIndexedQuery(boardLevelIndexQuery, levelRef, Int::class.java)
                .setLayout(R.layout.spinner_item).build()

        val boardLevelSpinnerAdapter = object : FirebaseObserverListAdapter<Int>(boardLevelSpinnerAdapterOptions, levelDataObserver) {
            override fun populateView(view: View, level: Int, position: Int) {
                (view as TextView).text = getString(R.string.standard_n, level)
            }
        }

        boardLevelSpinnerAdapter.startListening()

        // Once a level is selected, we can update the list of lessons
        levelSpinner.onItemSelected { pos -> updateSyllabusLessonList(subjectKey, boardLevelSpinnerAdapter.getRef(pos).key) }

        levelSpinner.adapter = boardLevelSpinnerAdapter
        levelSpinner.selectIndexWhenPopulated(levelSpinnerSelectionIndex)
    }

    private fun updateSyllabusLessonList(selectedSubjectKey: String, selectedLevel: String) {
        val syllabusLessonQuery = firestoreLocalized.collection("syllabusLessons")
                .whereEqualTo("board", prefs.getBoardKey())
                .whereEqualTo("subject", selectedSubjectKey)
                .whereEqualTo("level", selectedLevel)

        val adapterOptions = FirestoreRecyclerOptions.Builder<SyllabusLesson>()
                .setQuery(syllabusLessonQuery, SyllabusLesson::class.java).build()

        val adapter = org.jnanaprabodhini.happyteacher.adapter.firestore.SyllabusLessonRecyclerAdapter(adapterOptions, this, this)
        adapter.startListening()

        syllabusLessonsRecyclerView.adapter = adapter
    }

    override fun onRequestNewData() {
        statusTextView.setVisibilityGone()
        boardLessonsProgressBar.setVisible()
    }

    override fun onDataLoaded() {
        boardLessonsProgressBar.setVisibilityGone()
    }

    override fun onDataEmpty() {
        // Show empty view
        statusTextView.setVisible()
        statusTextView.setText(R.string.there_are_currently_no_lesson_plans_for_this_subject_and_level)
    }

    override fun onDataNonEmpty() {
        // Hide empty view
        statusTextView.setVisibilityGone()

        // Animate layout changes
        syllabusLessonsRecyclerView.scheduleLayoutAnimation()
        syllabusLessonsRecyclerView.invalidate()
    }

    private fun setSpinnersVisible() {
        subjectSpinner.setVisible()
        levelSpinner.setVisible()
    }

    private fun clearAdapters() {
        subjectSpinner.adapter = null
        levelSpinner.adapter = null
        syllabusLessonsRecyclerView.adapter = null
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        val levelSpinnerSelectionIndex = levelSpinner.selectedItemPosition
        val subjectSpinnerSelectionIndex = subjectSpinner.selectedItemPosition

        savedInstanceState.putInt(SavedInstanceStateConstants.LEVEL_SPINNER_SELECTION, levelSpinnerSelectionIndex)
        savedInstanceState.putInt(SavedInstanceStateConstants.SUBJECT_SPINNER_SELECTION, subjectSpinnerSelectionIndex)

        super.onSaveInstanceState(savedInstanceState)
    }

}

