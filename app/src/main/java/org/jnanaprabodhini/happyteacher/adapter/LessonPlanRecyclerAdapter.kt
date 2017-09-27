package org.jnanaprabodhini.happyteacher.adapter

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment
import org.jnanaprabodhini.happyteacher.R
import org.jnanaprabodhini.happyteacher.YOUTUBE_API_KEY
import org.jnanaprabodhini.happyteacher.extension.*
import org.jnanaprabodhini.happyteacher.model.LessonCard
import org.jnanaprabodhini.happyteacher.viewholder.LessonCardViewHolder
import android.widget.LinearLayout
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.util.Log
import android.webkit.WebSettings
import android.widget.FrameLayout
import java.util.*


/**
 * Created by grahamearley on 9/25/17.
 */
class LessonPlanRecyclerAdapter(val lessonCards: List<LessonCard>, val activity: Activity): RecyclerView.Adapter<LessonCardViewHolder>() {

    override fun getItemCount(): Int = lessonCards.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): LessonCardViewHolder {
        val cardView = LayoutInflater.from(parent?.context).inflate(R.layout.list_item_lesson_card, parent, false)
        return LessonCardViewHolder(cardView)
    }

    override fun onBindViewHolder(holder: LessonCardViewHolder?, position: Int) {
        val card = lessonCards[position]

        resetViewVisibility(holder)
        setupText(card, holder)

        if (card.youtubeId.isNotEmpty()) {
            setupYoutubePlayer(card.youtubeId, holder)
        } else if (card.imageUrls.isNotEmpty()) {
            setupImages(card.getCardImageUrls(), holder)
        }
    }

    private fun resetViewVisibility(holder: LessonCardViewHolder?) {
        holder?.headerMediaFrame?.setVisibilityGone()
        holder?.youtubeWebView?.setVisibilityGone()
        holder?.headerImageView?.setVisibilityGone()
    }

    private fun setupText(card: LessonCard, holder: LessonCardViewHolder?) {
        if (card.header.isNotEmpty()) {
            holder?.headerTextView?.setVisible()
            holder?.headerTextView?.text = card.header
        } else {
            holder?.headerTextView?.setVisibilityGone()
        }

        if (card.body.isNotEmpty()) {
            holder?.bodyTextView?.setVisible()
            holder?.bodyTextView?.setHtmlText(card.body)
        } else {
            holder?.bodyTextView?.setVisibilityGone()
        }
    }

    private fun setupYoutubePlayer(youtubeId: String, holder: LessonCardViewHolder?) {
        holder?.headerMediaFrame?.setVisible()
        holder?.youtubeWebView?.setVisible()
        holder?.youtubeWebView?.loadYoutubeVideo(youtubeId)
    }

    private fun setupImages(imageUrls: List<String>, holder: LessonCardViewHolder?) {
        holder?.headerMediaFrame?.setVisible()
        holder?.headerImageView?.setVisible()
        holder?.headerImageView?.loadImage(imageUrls.first())
    }
}