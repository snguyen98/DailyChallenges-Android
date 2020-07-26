package com.okomilabs.dailychallenges.fragments

import android.os.Build
import android.os.Bundle
import android.text.util.Linkify
import android.transition.Slide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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

        readMoreViewModel = ViewModelProvider(this).get(ReadMoreViewModel::class.java)
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

        setEnterSharedElementCallback(callback)
         */

        // Simple slide enter transition
        enterTransition = Slide()


        setReadMoreLayout(
            root.findViewById(R.id.challenge_title),
            root.findViewById(R.id.challenge_category),
            root.findViewById(R.id.challenge_summary),
            root.findViewById(R.id.challenge_desc),
            root.findViewById(R.id.read_more_pointer),
            root.findViewById(R.id.read_more_detail)
        )
      
        return root
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
        detail: LinearLayout
    ) {
        val challengeObserver = Observer<Challenge> { newChallenge ->
            title.text = newChallenge.title
            category.text = newChallenge.category
            summary.text = newChallenge.summary

            if (newChallenge.desc != null) {
                desc.text = newChallenge.desc
                showDetail(pointer, detail)
            }

            else {
                pointer.visibility = View.GONE
                detail.visibility = View.GONE
            }

        }

        readMoreViewModel.challenge.observe(viewLifecycleOwner, challengeObserver)
    }

    private fun showDetail(pointer: LinearLayout, detail: LinearLayout) {
        pointer.visibility = View.VISIBLE
        detail.visibility = View.VISIBLE

        val linksObserver = Observer<List<Link>> { newLinks ->
            if (newLinks != null) {

                for (link in newLinks) {
                    val linkView = TextView(context)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        linkView.setTextAppearance(R.style.SubText)
                    }
                    else {
                        linkView.setTextAppearance(context, R.style.SubText)
                    }

                    linkView.setPadding(0, 0, 0, 15)
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
        }

        readMoreViewModel.links.observe(viewLifecycleOwner, linksObserver)
    }

//    private fun setLinkStyle(): TextView {
//        val linkView = TextView(context)
//
//
//    }
}
