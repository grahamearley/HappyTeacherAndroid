package org.jnanaprabodhini.happyteacher.activity.base

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.jnanaprabodhini.happyteacher.R
import org.jnanaprabodhini.happyteacher.extension.withCurrentLocale
import org.jnanaprabodhini.happyteacher.util.PreferencesManager
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

/**
 * An abstract activity for all activities in the app. Includes access
 *  to the Firebase root database and the database for the current language,
 *  and locale switching.
 */
abstract class HappyTeacherActivity: AppCompatActivity() {

    val firestoreRoot: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val firestoreLocalized: DocumentReference by lazy {
        firestoreRoot.collection(getString(R.string.localized)).document(prefs.getCurrentLanguageCode())
    }

    val firestoreUsersCollection: CollectionReference by lazy {
        firestoreRoot.collection(getString(R.string.users))
    }

    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val prefs: PreferencesManager by lazy {
        PreferencesManager.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resetTitle()
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase.withCurrentLocale()))
    }

    protected fun refreshActivity() {
        val intent = intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        finish()
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Make "Up" button go Back
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun resetTitle() {
        try {
            val titleId = packageManager.getActivityInfo(componentName, PackageManager.GET_META_DATA).labelRes
            if (titleId != 0) {
                setTitle(titleId)
            }
        } catch (e: PackageManager.NameNotFoundException) {
            // Then we will show the title as it is set in top-level resources.
        }
    }

    fun getUserReference(): DocumentReference? {
        auth.currentUser?.let { user ->
            val id = user.uid
            return firestoreUsersCollection.document(id)
        }

        return null
    }
}