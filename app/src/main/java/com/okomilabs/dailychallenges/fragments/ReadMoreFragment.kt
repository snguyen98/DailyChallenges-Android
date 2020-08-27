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
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.data.entities.Challenge
import com.okomilabs.dailychallenges.data.entities.Link
import com.okomilabs.dailychallenges.viewmodels.ReadMoreViewModel
import java.util.regex.Pattern

class ReadMoreFragment: Fragment() {
    private lateinit var readMoreViewModel: ReadMoreViewModel

    private var hasDesc: MutableLiveData<Boolean> = MutableLiveData(false)
    private var hasLinks: MutableLiveData<Boolean> = MutableLiveData(false)

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

            val detail: LinearLayout = root.findViewById(R.id.read_more_detail)

            setTextViews(root.findViewById(R.id.read_more_summary), detail)
            addLinks(detail)
            observeState(root.findViewById(R.id.read_more_pointer), detail)

            // Transitions
            enterTransition = Slide().setInterpolator(LinearOutSlowInInterpolator())
            allowEnterTransitionOverlap = false
            postponeEnterTransition()

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadBannerAd(view.findViewById(R.id.banner_ad))
        startPostponedEnterTransition()
    }

    override fun onDestroy() {
        activity?.viewModelStore?.clear()
        super.onDestroy()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// AdMob Functions //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Loads the banner ad on the challenge page
     */
    private fun loadBannerAd(bannerAd: AdView) {
        val adRequest = AdRequest.Builder().build()
        bannerAd.loadAd(adRequest)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// Observing Functions ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Observes when the hasDesc and hasLinks variables change and fix the layout accordingly
     *
     * @param pointer The linear layout containing the read more button
     * @param detail The linear layout containing the challenge description and links
     */
    private fun observeState(pointer: TextView, detail: LinearLayout) {
        val stateObserver = Observer<Boolean> { _ ->
            hasDesc.value?.let { hasDescVal ->
                hasLinks.value?.let { hasLinksVal ->
                    // If there is a description then show pointer and hide detail
                    if (hasDescVal) {
                        pointer.visibility = View.VISIBLE
                        detail.visibility = View.GONE
                        pointerFunctionality(pointer, detail)

                        detail.findViewById<TextView>(R.id.info_label).visibility = View.VISIBLE
                        detail.findViewById<TextView>(R.id.challenge_desc).visibility = View.VISIBLE

                        // If there are links then show the links label
                        if (hasLinksVal) {
                            detail.findViewById<TextView>(R.id.links_label).visibility =
                                View.VISIBLE
                        }

                        // Otherwise hide it
                        else {
                            detail.findViewById<TextView>(R.id.links_label).visibility = View.GONE
                        }
                    }

                    // If there is no description then hide the pointer, info label and desc
                    else {
                        pointer.visibility = View.GONE
                        detail.findViewById<TextView>(R.id.info_label).visibility = View.GONE
                        detail.findViewById<TextView>(R.id.challenge_desc).visibility = View.GONE

                        // If there are links then show the whole of the detail layout
                        if (hasLinksVal) {
                            detail.visibility = View.VISIBLE
                        }

                        // Otherwise hide it
                        else {
                            detail.visibility = View.GONE
                        }
                    }

                }
            }
        }

        hasDesc.observe(viewLifecycleOwner, stateObserver)
        hasLinks.observe(viewLifecycleOwner, stateObserver)
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// Setting Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Observes changes in challenge info in view model, updates respective text views
     *
     * @param summary The linear layout containing the title, category, image and read more button
     * @param detail The linear layout containing the challenge description and links
     */
    private fun setTextViews(summary: RelativeLayout, detail: LinearLayout) {
        val challengeObserver = Observer<Challenge> { newChallenge ->
            summary.findViewById<TextView>(R.id.challenge_title).text = newChallenge.title
            summary.findViewById<TextView>(R.id.challenge_category).text = newChallenge.category
            summary.findViewById<TextView>(R.id.challenge_summary).text = newChallenge.summary

            showCategoryIcon(summary.findViewById(R.id.category_icon), newChallenge.category)

            if (newChallenge.desc != null) {
                detail.findViewById<TextView>(R.id.challenge_desc).text = newChallenge.desc
                hasDesc.value = true
            }

            else {
                hasDesc.value = false
            }
        }

        readMoreViewModel.challenge.observe(viewLifecycleOwner, challengeObserver)
    }

    /**
     * Checks if there are links in the view model, if so, adds them to the detail layout and sets
     * hasDesc instance variable to true, otherwise sets to false.
     *
     * @param detail The linear layout containing the challenge description and links
     */
    private fun addLinks(detail: LinearLayout) {
        val linksObserver = Observer<List<Link>> { newLinks ->
            if (!newLinks.isNullOrEmpty()) {
                for (link in newLinks) {
                    val linkView: TextView = generateLinkView(link.title)
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

                hasLinks.value = true
            }

            else {
                hasLinks.value = false
            }
        }

        readMoreViewModel.links.observe(viewLifecycleOwner, linksObserver)
    }

    /**
     * Sets the appropriate category image for the current challenge category
     *
     * @param icon The category image view
     * @param category The current challenge category
     */
    private fun showCategoryIcon(icon: ImageView, category: String) {
        when (category) {
            getString(R.string.physical_wellbeing) ->
                icon.setImageResource(R.mipmap.physical_wellbeing)

            getString(R.string.mental_wellbeing) ->
                icon.setImageResource(R.mipmap.mental_wellbeing)

            getString(R.string.socialising) ->
                icon.setImageResource(R.mipmap.socialising)

            getString(R.string.education_learning) ->
                icon.setImageResource(R.mipmap.education_learning)

            getString(R.string.skills_hobbies) ->
                icon.setImageResource(R.mipmap.skills_hobbies)
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// Layout Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Shows the read more button and set tap functionality
     *
     * @param pointer The linear layout containing the read more button
     * @param detail The linear layout containing the challenge description and links
     */
    private fun pointerFunctionality(pointer: TextView, detail: LinearLayout) {
        pointer.visibility = View.VISIBLE

        pointer.setOnClickListener {
            pointer.visibility = View.INVISIBLE
            detail.visibility = View.VISIBLE
        }
    }

    /**
     * Generates the layout attributes for each text view holding the links
     *
     * @param text The text to be displayed on the text view
     * @return The text view holding the link
     */
    @Suppress("DEPRECATION")
    private fun generateLinkView(text: String): TextView {
        val linkView = TextView(context)

        activity?.applicationContext?.let { appContext ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                linkView.setTextAppearance(R.style.SubText)
            }

            else {
                linkView.setTextAppearance(appContext, R.style.SubText)
            }

            val padding: Int = resources.getDimension(R.dimen.rm_link_padding_vertical).toInt()
            linkView.setPadding(0, padding, 0, padding)
            linkView.typeface = ResourcesCompat.getFont(appContext, R.font.timeless)
        }

        linkView.text = text

        return linkView
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// Inner Classes ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Factory to allow a challenge ID to be passed to the read more view model
     */
    @Suppress("UNCHECKED_CAST")
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
