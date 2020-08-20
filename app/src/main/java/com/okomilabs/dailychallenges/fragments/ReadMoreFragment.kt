package com.okomilabs.dailychallenges.fragments

import android.app.Application
import android.os.Build
import android.os.Bundle
import android.text.util.Linkify
import android.transition.Slide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.data.entities.Challenge
import com.okomilabs.dailychallenges.data.entities.Link
import com.okomilabs.dailychallenges.viewmodels.ReadMoreViewModel
import java.util.regex.Pattern

class ReadMoreFragment: Fragment() {
    private lateinit var readMoreViewModel: ReadMoreViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_read_more, container, false)

        val app = activity?.application
        val id: Int? = arguments?.getInt("challengeId")

        if (app != null && id != null) {

            readMoreViewModel = ViewModelProvider(
                this,
                ReadMoreFactory(app, id)
            ).get(ReadMoreViewModel::class.java)

            // Simple slide enter transition
            enterTransition = Slide()

            setReadMoreLayout(
                root.findViewById(R.id.read_more_summary),
                root.findViewById(R.id.read_more_detail)
            )

            /* Shared element transition
            val callback: SharedElementCallback = object: SharedElementCallback() {
                override fun onSharedElementEnd(
                    sharedElementNames: MutableList<String>?,
                    sharedElements: MutableList<View>?,
                    sharedElementSnapshots: MutableList<View>?
                ) {
                    super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
                }
            }

            sharedElementEnterTransition = TransitionInflater.from(context)
                .inflateTransition(R.transition.read_more_transition)

            setEnterSharedElementCallback(callback) */
        }

        return root
    }

    override fun onDestroy() {
        activity?.viewModelStore?.clear()
        super.onDestroy()
    }

    /**
     * Observes changes in challenge info in view model, updates respective text views
     *
     * @param summary The linear layout containing the title, category, image and read more button
     * @param detail The linear layout containing the challenge description and links
     */
    private fun setReadMoreLayout(summary: LinearLayout, detail: LinearLayout) {
        val challengeObserver = Observer<Challenge> { newChallenge ->
            val pointer: LinearLayout = summary.findViewById(R.id.read_more_pointer)
                                                     
            summary.findViewById<TextView>(R.id.challenge_title).text = newChallenge.title
            summary.findViewById<TextView>(R.id.challenge_category).text = newChallenge.category
            summary.findViewById<TextView>(R.id.challenge_summary).text = newChallenge.summary

            showCategoryIcon(summary.findViewById(R.id.category_icon), newChallenge.category)

            if (newChallenge.desc != null) {
                detail.findViewById<TextView>(R.id.challenge_desc).text = newChallenge.desc
                showDetail(pointer, detail, true)
            }

            else {
                showDetail(pointer, detail, false)
            }
        }

        readMoreViewModel.challenge.observe(viewLifecycleOwner, challengeObserver)
    }

    private fun showDetail(pointer: LinearLayout, detail: LinearLayout, hasDesc: Boolean) {
        val linksObserver = Observer<List<Link>> { newLinks ->
            if (!newLinks.isNullOrEmpty()) {
                pointerFunctionality(pointer, detail)
                detail.findViewById<TextView>(R.id.links_label).visibility = View.VISIBLE

                for (link in newLinks) {
                    val linkView = TextView(context)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        linkView.setTextAppearance(R.style.SubText)
                    }
                    else {
                        linkView.setTextAppearance(context, R.style.SubText)
                    }

                    linkView.setPadding(0, 15, 0, 15)
                    linkView.typeface = activity?.applicationContext?.let {
                        ResourcesCompat.getFont(it, R.font.timeless)
                    }
                  
                    linkView.text = link.title

                    val transform = Linkify.TransformFilter { _, _ -> link.link }

                    Linkify.addLinks(
                        linkView,
                        Pattern.compile(link.title),
                        null,
                        null,
                        transform
                    )

                    detail.addView(linkView)
                }
            }

            else {
                if (hasDesc) {
                    pointerFunctionality(pointer, detail)
                    detail.findViewById<TextView>(R.id.links_label).visibility = View.GONE
                }
                else {
                    pointer.visibility = View.GONE
                }

                detail.findViewById<TextView>(R.id.links_label).visibility = View.GONE
            }

            detail.visibility = View.GONE
        }

        readMoreViewModel.links.observe(viewLifecycleOwner, linksObserver)
    }

    /**
     * Shows the read more button and set tap functionality
     *
     * @param pointer The linear layout containing the read more button
     * @param detail The linear layout containing the challenge description and links
     */
    private fun pointerFunctionality(pointer: LinearLayout, detail: LinearLayout) {
        pointer.visibility = View.VISIBLE

        pointer.setOnClickListener {
            pointer.visibility = View.GONE
            detail.visibility = View.VISIBLE
        }
    }

    /**
     * Sets the appropriate category image for the current challenge category
     *
     * @param icon The category image view
     * @param category The current challenge category
     */
    private fun showCategoryIcon(icon: ImageView, category: String) {
        val context = activity?.applicationContext

        if (context != null) {
            when (category) {
                context.getString(R.string.physical_wellbeing) ->
                    icon.setImageResource(R.mipmap.physical_wellbeing)

                context.getString(R.string.mental_wellbeing) ->
                    icon.setImageResource(R.mipmap.mental_wellbeing)

                context.getString(R.string.socialising) ->
                    icon.setImageResource(R.mipmap.socialising)

                context.getString(R.string.education_learning) ->
                    icon.setImageResource(R.mipmap.education_learning)

                context.getString(R.string.skills_hobbies) ->
                    icon.setImageResource(R.mipmap.skills_hobbies)
            }
        }
    }

    /**
     * Factory to allow a challenge ID to be passed to the read more view model
     */
    private inner class ReadMoreFactory(
        app: Application, challengeId: Int
    ): ViewModelProvider.NewInstanceFactory() {
        private val application: Application = app
        private val id: Int = challengeId

        override fun <T: ViewModel?> create(modelClass: Class<T>): T {
            return ReadMoreViewModel(application, id) as T
        }
    }
}
