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
import com.okomilabs.dailychallenges.data.entities.LoginDay
import com.okomilabs.dailychallenges.helpers.State
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

        enterTransition = Slide(Gravity.END)

        val root = inflater.inflate(R.layout.fragment_challenge, container, false)

        observeState(
            root.findViewById(R.id.skip_button),
            root.findViewById(R.id.complete_button),
            root.findViewById(R.id.streak_icon),
            root.findViewById(R.id.streak_val),
            root.findViewById(R.id.freeze_icon)
        )

        checkGainedFreeze()

        setChallengeInfo(
            root.findViewById(R.id.challenge_title),
            root.findViewById(R.id.challenge_category)
        )

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setNavigation(view.findViewById(R.id.challenge_card))

        loadBannerAd(view.findViewById(R.id.banner_ad))
        loadRewardedAd()
    }

    override fun onDestroy() {
        activity?.viewModelStore?.clear()
        super.onDestroy()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// Navigation Functions ///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Adds listener to navigate to read more section when clicked
     */
    private fun setNavigation(card: CardView) {
        card.setOnClickListener {
            if (!checkIsNewDay()) {
                findNavController().navigate(ChallengeFragmentDirections.challengeToReadMore())
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// Observing Functions ///////////////////////////////////
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

    /**
     * Observes the state of the challenge and changes the top right icon accordingly
     */
    private fun observeState(
        skip: Button,
        complete: Button,
        streakIcon: ImageView,
        streakVal: TextView,
        freeze: ImageView
    ) {
        val stateObserver = Observer<LoginDay> { newLoginDay ->
            val streak: Int = challengeViewModel.getStreak()

            if (streak > 0) {
                streakIcon.visibility = View.VISIBLE
                streakVal.visibility = View.VISIBLE
                streakVal.text = streak.toString()
            }
            else {
                streakIcon.visibility = View.GONE
                streakVal.visibility = View.GONE
            }

            when (newLoginDay.state) {
                State.INCOMPLETE -> {
                    completeFunctionality(complete)
                    skipFunctionality(skip)
                    freezeFunctionality(freeze)
                }
                State.COMPLETE -> {
                    freeze.alpha = 0f
                    freeze.setImageResource(R.mipmap.complete_icon)
                    freeze.animate().alpha(1.0f).duration = 100L

                    disableButtons(skip, complete, freeze)
                }
                State.FROZEN -> {
                    freeze.alpha = 0f
                    freeze.setColorFilter(R.color.freeze_icon)
                    freeze.animate().alpha(1.0f).duration = 100L

                    disableButtons(skip, complete, freeze)
                }
            }
        }

        challengeViewModel.loginDay.observe(viewLifecycleOwner, stateObserver)
    }

    /**
     * Checks if a new day has started and refreshes the challenge if true
     *
     * @return True if a new day has started and false otherwise
     */
    private fun checkIsNewDay(): Boolean {
        return if (challengeViewModel.isNewDay()) {
            AlertDialog.Builder(context)
                .setTitle("Challenge period is over")
                .setMessage("A new day has started. Refreshing challenge...")
                .setPositiveButton("Ok") { _, _ ->
                    challengeViewModel.initialise()
                    findNavController().navigate(ChallengeFragmentDirections.challengeToWelcome())
                }
                .create()
                .show()

            true
        }
        else {
            false
        }
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

            rewardedAd = RewardedAd(activity?.applicationContext, getString(R.string.test_reward_ad))
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

                if (!checkIsNewDay()) {
                    if (skip) {
                        challengeViewModel.skipChallenge()
                    }
                    skip = false
                }
            }

            override fun onRewardedAdFailedToShow(errorCode: Int) {
                Log.d("Rewarded Ad", "Failed to show with code: $errorCode")
                checkIsNewDay()
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
        complete.animate().alpha(1.0f).duration = 100L

        complete.setOnClickListener {
            if (!checkIsNewDay()) {
                AlertDialog.Builder(context)
                    .setTitle("Mark as Complete")
                    .setMessage("Would you like to mark this challenge as complete?")
                    .setPositiveButton("Yes") { _, _ ->
                        if (!checkIsNewDay()) {
                            challengeViewModel.markComplete()
                        }
                    }
                    .setNeutralButton("No") { _, _ -> checkIsNewDay() }
                    .create()
                    .show()
            }
        }
    }

    /**
     * Sets up the appropriate alert dialogs when the skip button is pressed
     */
    private fun skipFunctionality(skip: Button) {
        skip.animate().alpha(1.0f).duration = 100L

        skip.setOnClickListener {
            if (!checkIsNewDay()) {
                val skips: Int = challengeViewModel.getSkips()

                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                builder.setTitle("Skip Challenge")

                if (skips <= 0) {
                    builder
                        .setMessage("You have no skips left")
                        .setPositiveButton("Ok") { _, _ -> checkIsNewDay() }
                }

                else {
                    if (rewardedAd.isLoaded) {
                        builder
                            .setMessage(
                                "You have $skips skip(s) left. Would you like " +
                                "to watch a short ad to skip this challenge?"
                            )
                            .setPositiveButton("Yes") { _, _ ->
                                if (!checkIsNewDay()) {
                                    showRewardedAd()
                                }
                            }
                            .setNeutralButton("No") { _, _ -> checkIsNewDay() }
                    }

                    else {
                        builder
                            .setMessage("Sorry, there are no ads available right now")
                            .setPositiveButton("Ok") { _, _ -> checkIsNewDay() }
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

                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                builder.setTitle("Freeze Challenge")

                if (freezes <= 0) {
                    builder
                        .setMessage("You have no freezes left. ")
                        .setPositiveButton("Ok") { _, _ -> checkIsNewDay() }
                }

                else {
                    builder
                        .setMessage(
                            "You have $freezes freeze(s) left. Would " +
                            "you like to freeze this challenge?"
                        )
                        .setPositiveButton("Yes") { _, _ ->
                            if (!checkIsNewDay()) {
                                challengeViewModel.freezeDay()
                            }
                        }
                        .setNeutralButton("No") { _, _ -> checkIsNewDay() }
                }

                builder.create().show()
            }
        }
    }

    /**
     * Checks if the user gain a freeze since last login and shows an message if true
     */
    private fun checkGainedFreeze() {
        if (challengeViewModel.showFreezeMsg()) {
            AlertDialog.Builder(context)
                .setTitle("Freeze Gained")
                .setMessage(
                    "You gained a freeze for keeping up your streak! " +
                    "You have ${challengeViewModel.getFreezes()} freeze(s)."
                )
                .setPositiveButton("Ok") { _, _ -> checkIsNewDay() }
                .create()
                .show()
        }
    }

    /**
     * Disables complete, skip and freeze buttons
     */
    private fun disableButtons(skip: Button, complete: Button, freeze: ImageView) {
        skip.animate().alpha(0.7f).duration = 100L
        complete.animate().alpha(0.7f).duration = 100L

        skip.setOnClickListener(null)
        complete.setOnClickListener(null)
        freeze.setOnClickListener(null)
    }

}
