package com.okomilabs.dailychallenges.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.data.entities.Challenge
import com.okomilabs.dailychallenges.viewmodels.ChallengeViewModel

class ChallengeFragment: Fragment() {
    private lateinit var challengeViewModel: ChallengeViewModel
    private lateinit var rewardedAd: RewardedAd

    private var skip: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        challengeViewModel = ViewModelProvider(this).get(ChallengeViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_challenge, container, false)

        val card: CardView = root.findViewById(R.id.challenge_card)
        val title: TextView = root.findViewById(R.id.challenge_title)
        val category: TextView = root.findViewById(R.id.challenge_category)

        val bannerAd: AdView = root.findViewById(R.id.banner_ad)

        val complete: Button = root.findViewById(R.id.complete_button)
        val skip: Button = root.findViewById(R.id.skip_button)

        setNavigation(card)
        setChallengeInfo(title, category)

        loadBannerAd(bannerAd)
        loadRewardedAd()

        completeFunctionality(complete)
        skipFunctionality(skip)

        enterTransition = Slide(Gravity.END)

        return root
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// Navigation Functions ///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds listener to navigate to read more section when clicked
     */
    private fun setNavigation(card: CardView) {
        card.setOnClickListener {
            findNavController().navigate(ChallengeFragmentDirections.challengeToReadMore())
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// Challenge Info Functions /////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Observes changes in title and category in view model and updates respective text view values
     */
    private fun setChallengeInfo(title: TextView, category: TextView) {
        val challengeObserver = Observer<Challenge> { newChallenge ->
            title.text = newChallenge.title
            category.text = newChallenge.category
        }

        challengeViewModel.challenge.observe(viewLifecycleOwner, challengeObserver)
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

    /**
     * Loads the rewarded ad to be shown when the user skips the challenge
     */
    private fun loadRewardedAd() {
        if (!(::rewardedAd.isInitialized) || !rewardedAd.isLoaded) {
            val adCallback = object: RewardedAdLoadCallback() {
                override fun onRewardedAdLoaded() {
                    Log.d("Rewarded Ad", "Loaded")
                }

                override fun onRewardedAdFailedToLoad(errorCode: Int) {
                    Log.d("Rewarded Ad", "Failed to load with code: $errorCode")
                }
            }

            rewardedAd = RewardedAd(context, getString(R.string.test_reward_ad))
            rewardedAd.loadAd(AdRequest.Builder().build(), adCallback)
        }
    }

    /**
     * Displays the ad to be rewarded with a skip
     */
    private fun showRewardedAd() {
        val adCallback = object: RewardedAdCallback() {
            override fun onUserEarnedReward(reward: RewardItem) {
                skip = true
            }

            override fun onRewardedAdClosed() {
                loadRewardedAd()
                if (skip) {
                    challengeViewModel.skipChallenge()
                    skip = false
                }
            }

            override fun onRewardedAdFailedToShow(errorCode: Int) {
                Log.d("Rewarded Ad", "Failed to show with code: $errorCode")
            }
        }

        rewardedAd.show(activity, adCallback)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// Button Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets up the appropriate alert dialogs when the complete button is pressed
     */
    private fun completeFunctionality(complete: Button) {
        complete.setOnClickListener {
            if (!checkIsNewDay()) {
                val (builder: AlertDialog.Builder, built: Boolean) = initialBuilder()
                builder.setTitle("Mark as Complete")

                if (!built) {
                    builder.setMessage("Would you like to mark this challenge as complete?")

                    builder.setPositiveButton("Yes") { _, _ ->
                        if (!checkIsNewDay()) {
                            challengeViewModel.markComplete()
                        }
                    }
                    builder.setNeutralButton("No") { _, _ -> checkIsNewDay() }
                }

                builder.create().show()
            }
        }
    }

    /**
     * Sets up the appropriate alert dialogs when the skip button is pressed
     */
    private fun skipFunctionality(skip: Button) {
        skip.setOnClickListener {
            if (!checkIsNewDay()) {
                val skips: Int = challengeViewModel.getSkips()

                val (builder: AlertDialog.Builder, built: Boolean) = initialBuilder()
                builder.setTitle("Skip Challenge")

                if (!built) {
                    if (skips <= 0) {
                        builder.setMessage("You have no skips left")
                        builder.setPositiveButton("Ok") { _, _ -> checkIsNewDay() }
                    }

                    else {
                        if (rewardedAd.isLoaded) {
                            builder.setMessage(
                                "You have $skips skips left. Would you like " +
                                "to watch a short ad to skip this challenge?"
                            )
                            builder.setPositiveButton("Yes") { _, _ ->
                                if (!checkIsNewDay()) {
                                    showRewardedAd()
                                }
                            }
                            builder.setNeutralButton("No") { _, _ -> checkIsNewDay() }
                        }

                        else {
                            builder.setMessage("Sorry, there are no ads available right now")
                            builder.setPositiveButton("Ok") { _, _ -> checkIsNewDay() }
                        }
                    }
                }

                builder.create().show()
            }
        }
    }

    /**
     * Sets up the appropriate alert dialogs when the freeze icon is pressed
     */
    private fun freezeFunctionality(freeze: ImageView) {
        freeze.setOnClickListener {
            if (!checkIsNewDay()) {
                val freezes: Int = challengeViewModel.getFreezes()

                val (builder: AlertDialog.Builder, built: Boolean) = initialBuilder()
                builder.setTitle("Freeze Challenge")

                if (!built) {
                    if (freezes <= 0) {
                        builder.setMessage("You have no freezes left")
                        builder.setPositiveButton("Ok") { _, _ -> checkIsNewDay() }
                    }

                    else {
                        builder.setMessage(
                            "You have $freezes freezes left. Would " +
                            "you like to freeze this challenge?"
                        )
                        builder.setPositiveButton("Yes") { _, _ ->
                            if (!checkIsNewDay()) {
                                challengeViewModel.freezeDay()
                            }
                        }
                        builder.setNeutralButton("No") { _, _ -> checkIsNewDay() }
                    }
                }

                builder.create().show()
            }
        }
    }

    /**
     * Checks if a new day has started and refreshes the challenge if true
     *
     * @return True if a new day has started and false otherwise
     */
    private fun checkIsNewDay(): Boolean {
        return if (challengeViewModel.isNewDay()) {
            challengeViewModel.initialise()
            findNavController().navigate(ChallengeFragmentDirections.challengeToWelcome())
            true
        }
        else {
            false
        }
    }

    /**
     * Initialises alert dialog builder and adds the completed or frozen messages if applicable
     *
     * @return A pair containing the alert dialog and a boolean declaring if the builder is finished
     */
    private fun initialBuilder(): Pair<AlertDialog.Builder, Boolean> {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        var built = false

        if (challengeViewModel.isComplete()) {
            builder.setMessage("Challenge is already complete")
            builder.setPositiveButton("Ok") { _, _ -> checkIsNewDay() }
            built = true
        }

        else if (challengeViewModel.isFrozen()) {
            builder.setMessage("Challenge is currently frozen")
            builder.setPositiveButton("Ok") { _, _ -> checkIsNewDay() }
            built = true
        }

        return Pair(builder, built)
    }

}
