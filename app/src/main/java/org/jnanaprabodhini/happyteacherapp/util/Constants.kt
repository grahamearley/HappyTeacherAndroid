package org.jnanaprabodhini.happyteacherapp.util

/**
 * Constants used in the app (and in Firestore)
 */
object ResourceStatus {
    const val PUBLISHED = "published"
    const val DRAFT = "draft"
    const val AWAITING_REVIEW = "awaiting review"
    const val CHANGES_REQUESTED = "changes requested"

    // This string *isn't* a value for the "status" key,
    //  rather it is a boolean field on resource objects
    //  that lets us fake a logical-OR query for resources
    //  with one or both of these statuses:
    const val AWAITING_REVIEW_OR_CHANGES_REQUESTED = "isAwaitingReviewOrHasChangesRequested"
}

object ResourceType {
    const val LESSON = "lesson"
    const val CLASSROOM_RESOURCE = "classroom resource"
}

object UserRoles {
    const val ADMIN = "admin"
    const val MODERATOR = "moderator"
}

// TODO: move keys to this object from string resources
object FirestoreKeys {
    const val STATUS = "status"
    const val IS_FEATURED = "isFeatured"
    const val FEEDBACK = "feedback"
    const val DATE_UPDATED = "dateUpdated"
    const val FEEDBACK_PREVIEW_COMMENT = "feedbackPreviewComment"
    const val FEEDBACK_PREVIEW_COMMENT_PATH = "feedbackPreviewCommentPath"
    const val COMMENT_TEXT = "commentText"
    const val REVIEWER_COMMENT = "reviewerComment"
    const val LOCKED = "locked"
    const val REGISTRATION_TOKEN = "registrationToken"
    const val RESOURCE_TYPE = "resourceType"
    const val REFERENCE_PATH = "referencePath"
    const val NAME = "name"
    const val RESOURCE_NAME = "resourceName"
}