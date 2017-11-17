package org.jnanaprabodhini.happyteacher.activity

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_card_list_content_viewer.*
import org.jnanaprabodhini.happyteacher.R
import org.jnanaprabodhini.happyteacher.activity.base.HappyTeacherActivity
import org.jnanaprabodhini.happyteacher.adapter.contentlist.ResourceContentRecyclerAdapter
import org.jnanaprabodhini.happyteacher.adapter.helper.FirebaseDataObserver
import org.jnanaprabodhini.happyteacher.extension.setVisibilityGone
import org.jnanaprabodhini.happyteacher.extension.setVisible
import org.jnanaprabodhini.happyteacher.model.ResourceHeader
import org.jnanaprabodhini.happyteacher.model.User
import java.io.File

abstract class ResourceContentViewerActivity : HappyTeacherActivity(), FirebaseDataObserver {

    companion object {
        const val WRITE_STORAGE_PERMISSION_CODE = 1

        const val CONTENT_REF_PATH: String = "CONTENT_REF_PATH"
        fun Intent.getContentRefPath(): String = getStringExtra(CONTENT_REF_PATH)

        const val HEADER: String = "HEADER"
        fun Intent.getHeader(): ResourceHeader = getParcelableExtra(HEADER)
    }

    protected val header by lazy { intent.getHeader() }
    protected val contentRef by lazy { firestoreRoot.document(intent.getContentRefPath()) }
    protected val cardsRef by lazy { contentRef.collection(getString(R.string.cards)) }

    abstract val cardRecyclerAdapter: ResourceContentRecyclerAdapter

    protected val attachmentDestinationDirectory by lazy {
        // This directory will be used to store any attachments downloaded from this contentKey.
        File(Environment.getExternalStorageDirectory().path
                + File.separator
                + getString(R.string.app_name)
                + File.separator
                + header.subjectName + File.separator + header.topicName + File.separator + header.name)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_list_content_viewer)

        setHeaderView()
        initializeUiForContentFromDatabase()
    }

    private fun initializeUiForContentFromDatabase() {
        progressBar.setVisible()
        initializeUiForContent()
    }

    private fun initializeUiForContent() {
        progressBar.setVisibilityGone()
        initializeRecyclerView()
    }

    open fun setHeaderView() {
        headerView.setVisible()
        supportActionBar?.title = header.name

        subjectTextView.text = header.subjectName
        authorNameTextView.text = header.authorName
        institutionTextView.text = header.authorInstitution
        locationTextView.text = header.authorLocation
    }

    open fun initializeRecyclerView() {
        cardRecyclerView.layoutManager = LinearLayoutManager(this)
        cardRecyclerAdapter.startListening()
        cardRecyclerView?.adapter = cardRecyclerAdapter
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == WRITE_STORAGE_PERMISSION_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            // Re-draw items to reflect new permissions
            cardRecyclerView.adapter.notifyDataSetChanged()
        }
    }

    override fun onRequestNewData() {
        progressBar.setVisible()
    }

    override fun onDataLoaded() {
        progressBar.setVisibilityGone()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_cards_viewer, menu)
        val editLessonButton = menu?.findItem(R.id.menu_admin_edit_card_list_content)
        editLessonButton?.isVisible = false

        // Only show edit button to admins!
        //  (Our Firestore security rules also
        //   only allow writes from admins)
        auth.currentUser?.uid?.let { uid ->
            firestoreUsersCollection.document(uid).get().addOnSuccessListener { snapshot ->
                val user = snapshot.toObject(User::class.java)
                if (user.role == User.Roles.ADMIN) {
                    editLessonButton?.isVisible = true
                }
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.menu_admin_edit_card_list_content -> openInEditor()
        }
        return true
    }

    private fun openInEditor() {
        // TODO: make this abstract -- launch separate editors for Lessons, Classroom Resources
        LessonEditorActivity.launch(this, contentRef, header)
    }

}

