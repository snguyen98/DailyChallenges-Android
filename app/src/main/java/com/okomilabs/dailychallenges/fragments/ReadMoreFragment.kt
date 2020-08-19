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
                root.findViewById(R.id.challenge_title),
                root.findViewById(R.id.challenge_category),
                root.findViewById(R.id.challenge_summary),
                root.findViewById(R.id.challenge_desc),
                root.findViewById(R.id.read_more_pointer),
                root.findViewById(R.id.read_more_gradient),
                root.findViewById(R.id.read_more_detail),
                root.findViewById(R.id.category_icon)
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
     * Observes changes in challenge info in view model, updates respective text view values and
     * displays the description and links if available
     */
    private fun setReadMoreLayout(
        title: TextView,
        category: TextView,
        summary: TextView,
        desc: TextView,
        pointer: LinearLayout,
        gradient: LinearLayout,
        detail: LinearLayout,
        icon: ImageView
    ) {
        val challengeObserver = Observer<Challenge> { newChallenge ->
            val readMoreToggle: TextView = pointer.findViewById(R.id.read_more_toggle)
                                                     
            title.text = newChallenge.title
            category.text = newChallenge.category
            summary.text = newChallenge.summary

            if (newChallenge.desc != null) {
                desc.text = newChallenge.desc
                showDetail(pointer, gradient, detail, true)
            }
            else {
                showDetail(pointer, gradient, detail, false)
            }
            showCategoryIcon(icon, newChallenge.category)

            readMoreToggle.setOnClickListener {
                showHide(pointer)
                showHide(gradient)
                showHide(detail)
            }
    }

        readMoreViewModel.challenge.observe(viewLifecycleOwner, challengeObserver)
    }



    private fun showHide(view:View){
        view.visibility = if (view.visibility == View.GONE) {
            View.VISIBLE }
        else{
            View.GONE}
    }

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

    private fun showDetail(pointer: LinearLayout, gradient: LinearLayout, detail: LinearLayout, hasDesc: Boolean) {
        val linksObserver = Observer<List<Link>> { newLinks ->
            if (!newLinks.isNullOrEmpty()) {
                pointer.visibility = View.VISIBLE
                gradient.visibility=View.GONE
                detail.visibility = View.GONE

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

                detail.findViewById<TextView>(R.id.links_label).visibility = View.VISIBLE
            }
            else {
                if (hasDesc) {
                    pointer.visibility = View.VISIBLE
                    gradient.visibility = View.VISIBLE
                    detail.visibility = View.VISIBLE
                }
                else {
                    pointer.visibility = View.GONE
                    gradient.visibility = View.GONE
                    detail.visibility = View.GONE
                }
            }
        }

        readMoreViewModel.links.observe(viewLifecycleOwner, linksObserver)
    }

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
