package com.okomilabs.dailychallenges.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
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

        return root
    }

    /**
     * Reloads the fragment
     */
    private fun refreshFragment() {
        activity?.recreate()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// Navigation Functions ///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds listener to navigate to read more section when clicked
     */
    private fun setNavigation(card: CardView) {
        card.setOnClickListener {
            card.findNavController().navigate(ChallengeFragmentDirections.challengeToReadMore())
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

    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////// Button Functions /////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets up the appropriate alert dialogs when the complete button is pressed
     */
    private fun completeFunctionality(complete: Button) {
        complete.setOnClickListener {
            if (challengeViewModel.isNewDay()) {
                challengeViewModel.refreshChallenge()
                refreshFragment()
            }

            else {
                val (builder: AlertDialog.Builder, built: Boolean) = initialBuilder()
                builder.setTitle("Mark as Complete")

                if (!built) {
                    builder.setMessage("Would you like to mark this challenge as complete?")

                    builder.setPositiveButton("Yes") { _, _ ->
                        challengeViewModel.markComplete()
                    }
                    builder.setNeutralButton("No") { _, _ -> }
                }

                val alert: AlertDialog = builder.create()
                alert.show()

            }
        }
    }

    /**
     * Sets up the appropriate alert dialogs when the skip button is pressed
     */
    private fun skipFunctionality(skip: Button) {
        skip.setOnClickListener {

            if (challengeViewModel.isNewDay()) {
                challengeViewModel.refreshChallenge()
                refreshFragment()
            }

            else {
                val skips: Int = challengeViewModel.getSkips()

                val (builder: AlertDialog.Builder, built: Boolean) = initialBuilder()
                builder.setTitle("Skip Challenge")

                if (!built) {
                    if (skips <= -100) {
                        builder.setMessage("You have no skips left")
                        builder.setPositiveButton("Ok") { _, _ -> }
                    }

                    else {
                        if (rewardedAd.isLoaded) {
                            val msg =
                                "You have $skips skips left. " +
                                "Would you like to watch a short ad to skip this challenge?"

                            builder.setMessage(msg)
                            builder.setPositiveButton("Yes") { _, _ ->
                                showRewardedAd()
                            }
                            builder.setNeutralButton("No") { _, _ -> }
                        }

                        else {
                            builder.setMessage("Sorry, there are no ads available right now")
                            builder.setPositiveButton("Ok") { _, _ -> }
                        }
                    }
                }

                val alert: AlertDialog = builder.create()
                alert.show()
            }
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
            builder.setPositiveButton("Ok") { _, _ -> }
            built = true
        }

        else if (challengeViewModel.isFrozen()) {
            builder.setMessage("Challenge is currently frozen")
            builder.setPositiveButton("Ok") { _, _ -> }
            built = true
        }

        return Pair(builder, built)
    }

}
