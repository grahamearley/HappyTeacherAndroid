package org.jnanaprabodhini.happyteacher.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.CollectionReference
import kotlinx.android.synthetic.main.activity_card_list_content_viewer.*
import org.jnanaprabodhini.happyteacher.R
import org.jnanaprabodhini.happyteacher.activity.base.HappyTeacherActivity
import org.jnanaprabodhini.happyteacher.adapter.contentlist.CardListContentRecyclerAdapter
import org.jnanaprabodhini.happyteacher.adapter.helper.FirebaseDataObserver
import org.jnanaprabodhini.happyteacher.extension.onSingleValueEvent
import org.jnanaprabodhini.happyteacher.extension.setVisibilityGone
import org.jnanaprabodhini.happyteacher.extension.setVisible
import org.jnanaprabodhini.happyteacher.model.CardListContent
import org.jnanaprabodhini.happyteacher.model.CardListContentHeader
import org.jnanaprabodhini.happyteacher.model.ContentCard
import java.io.File

abstract class CardListContentViewerActivity : HappyTeacherActivity(), FirebaseDataObserver {
    // TODO: Add data observer functions!

    companion object Constants {
        val WRITE_STORAGE_PERMISSION_CODE = 1

        fun launchLessonViewerActivity(from: Activity, cardRef: CollectionReference, cardListContentHeader: CardListContentHeader, topicName: String) {
            val lessonViewerIntent = Intent(from, LessonViewerActivity::class.java)
            launchIntentWithExtras(lessonViewerIntent, from, cardRef, cardListContentHeader, topicName)
        }

        fun launchClassroomResourcesActivity(from: Activity, cardRef: CollectionReference, cardListContentHeader: CardListContentHeader, topicName: String) {
            val classroomResourcesViewerIntent = Intent(from, ClassroomResourceViewerActivity::class.java)
            launchIntentWithExtras(classroomResourcesViewerIntent, from, cardRef, cardListContentHeader, topicName)
        }

        private fun launchIntentWithExtras(intent: Intent, activity: Activity, cardRef: CollectionReference, cardListContentHeader: CardListContentHeader, topicName: String) {
            intent.apply {
                putExtra(CardListContentViewerActivity.CARD_REF_PATH, cardRef.path)
                putExtra(CardListContentViewerActivity.TOPIC_NAME, topicName)
                putExtra(CardListContentViewerActivity.HEADER, cardListContentHeader)
            }
            activity.startActivity(intent)
        }

        val CARD_REF_PATH: String = "CARD_REF_PATH"
        fun Intent.hasCardRefPath(): Boolean = hasExtra(CARD_REF_PATH)
        fun Intent.getCardRefPath(): String = getStringExtra(CARD_REF_PATH)

        val TOPIC_NAME: String = "TOPIC_NAME"
        fun Intent.hasTopicName(): Boolean = hasExtra(TOPIC_NAME)
        fun Intent.getTopicName(): String = getStringExtra(TOPIC_NAME)

        val HEADER: String = "HEADER"
        fun Intent.hasHeader(): Boolean = hasExtra(HEADER)
        fun Intent.getHeader(): CardListContentHeader = getParcelableExtra(HEADER)

        fun Intent.hasAllExtras(): Boolean = hasCardRefPath() && hasTopicName() && hasHeader()
    }

    val cardRefPath by lazy { intent.getCardRefPath() }
    val topicName by lazy { intent.getTopicName() }
    val header by lazy { intent.getHeader() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_list_content_viewer)

        if (!intent.hasAllExtras()) {
            showErrorToastAndFinish()
        }

        setHeaderView()
        initializeUiForContentFromDatabase()
    }

    private fun initializeUiForContentFromDatabase() {
        progressBar.setVisible()

        // This directory will be used to store any attachments downloaded from this contentKey.
        val attachmentDestinationDirectory = File(Environment.getExternalStorageDirectory().path
                                                        + File.separator
                                                        + getString(R.string.app_name)
                                                        + File.separator
                                                        + header.subjectName + File.separator + topicName + File.separator + header.name)

        val cardRef = firestoreRoot.collection(cardRefPath)

        initializeUiForContent(cardRef, attachmentDestinationDirectory)
    }

    private fun initializeUiForContent(cardRef: CollectionReference, attachmentDestinationDirectory: File) {
        progressBar.setVisibilityGone()
        initializeRecyclerView(cardRef, attachmentDestinationDirectory)
    }

    open fun setHeaderView() {
        headerView.setVisible()
        supportActionBar?.title = header.name

        subjectTextView.text = header.subjectName
        authorNameTextView.text = header.authorName
        institutionTextView.text = header.authorInstitution
        locationTextView.text = header.authorLocation
    }

    private fun initializeRecyclerView(cardRef: CollectionReference, attachmentDestinationDirectory: File) {
        cardRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = getCardRecyclerAdapter(cardRef, attachmentDestinationDirectory)
        adapter.startListening()
        cardRecyclerView?.adapter = adapter
    }

    abstract fun getCardRecyclerAdapter(cardRef: CollectionReference, attachmentDestinationDirectory: File): CardListContentRecyclerAdapter

    private fun showErrorToastAndFinish() {
        // TODO: Log error to analytics.
        Toast.makeText(this, R.string.there_was_an_error_loading_the_lesson, Toast.LENGTH_LONG).show()
        finish()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == WRITE_STORAGE_PERMISSION_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            // Re-draw items to reflect new permissions
            cardRecyclerView.adapter.notifyDataSetChanged()
        }
    }
}

