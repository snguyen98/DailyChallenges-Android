package com.okomilabs.dailychallenges.fragments

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.transition.Slide
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
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

        enterTransition = Slide(Gravity.END).setInterpolator(LinearOutSlowInInterpolator())
        exitTransition = Slide(Gravity.START).setInterpolator(LinearOutSlowInInterpolator())

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setNavigation(view.findViewById(R.id.challenge_card))
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
            val id: Int? = challengeViewModel.challenge.value?.id

            if (!checkIsNewDay() && id != null) {
                findNavController().navigate(
                    ChallengeFragmentDirections.challengeToReadMore(id)
                )
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
        val appContext: Context? = activity?.applicationContext

        return if (challengeViewModel.isNewDay()) {
            if (appContext != null) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context)

                builder
                    .setCustomTitle(
                        createDialogTitle(appContext.getString(R.string.refresh_title))
                    )
                    .setMessage(appContext.getString(R.string.refresh_message))
                    .setPositiveButton(appContext.getString(R.string.ok_label)) { _, _ ->
                        challengeViewModel.initialise()
                        findNavController()
                            .navigate(ChallengeFragmentDirections.challengeToWelcome())
                    }

                val alert = builder.create()
                alert.show()
                setDialogFont(alert)
            }

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

            rewardedAd = RewardedAd(
                activity?.applicationContext, getString(R.string.test_reward_ad)
            )
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
            val appContext: Context? = activity?.applicationContext

            if (!checkIsNewDay()) {
                if (appContext != null) {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)

                    builder
                        .setCustomTitle(
                            createDialogTitle(appContext.getString(R.string.complete_title))
                        )
                        .setMessage(appContext.getString(R.string.complete_message))
                        .setPositiveButton(appContext.getString(R.string.yes_label)) { _, _ ->
                            if (!checkIsNewDay()) {
                                challengeViewModel.markComplete()
                            }
                        }
                        .setNeutralButton(appContext.getString(R.string.no_label)) { _, _ ->
                            checkIsNewDay()
                        }

                    val alert = builder.create()
                    alert.show()
                    setDialogFont(alert)
                }
            }
        }
    }

    /**
     * Sets up the appropriate alert dialogs when the skip button is pressed
     */
    private fun skipFunctionality(skip: Button) {
        skip.animate().alpha(1.0f).duration = 100L

        skip.setOnClickListener {
            val appContext: Context? = activity?.applicationContext

            if (!checkIsNewDay()) {
                val skips: Int = challengeViewModel.getSkips()
                val builder: AlertDialog.Builder = AlertDialog.Builder(context)

                if (appContext != null) {
                    builder.setCustomTitle(
                        createDialogTitle(appContext.getString(R.string.skip_title))
                    )

                    if (skips <= 0) {
                        builder
                            .setMessage(appContext.getString(R.string.skip_unavailable_message))
                            .setPositiveButton(appContext.getString(R.string.ok_label)) { _, _ ->
                                checkIsNewDay()
                            }
                    }

                    else {
                        if (rewardedAd.isLoaded) {
                            builder
                                .setMessage(
                                    "You have $skips skip(s) left." +
                                            appContext.getString(R.string.skip_available_message)
                                )
                                .setPositiveButton(
                                    appContext.getString(R.string.yes_label)) { _, _ ->
                                    if (!checkIsNewDay()) {
                                        showRewardedAd()
                                    }
                                }
                                .setNeutralButton(
                                    appContext.getString(R.string.no_label)) { _, _ ->
                                    checkIsNewDay()
                                }
                        }

                        else {
                            builder
                                .setMessage(appContext.getString(R.string.skip_no_ads_message))
                                .setPositiveButton(
                                    appContext.getString(R.string.ok_label)) { _, _ ->
                                    checkIsNewDay()
                                }
                        }
                    }

                    val alert = builder.create()
                    alert.show()
                    setDialogFont(alert)
                }
            }
        }
    }

    /**
     * Sets up the appropriate alert dialogs when the freeze icon is pressed
     */
    private fun freezeFunctionality(freeze: ImageView) {
        freeze.setOnClickListener {
            val appContext: Context? = activity?.applicationContext

            if (!checkIsNewDay()) {
                val freezes: Int = challengeViewModel.getFreezes()
                val builder: AlertDialog.Builder = AlertDialog.Builder(context)

                if (appContext != null) {
                    builder.setCustomTitle(
                        createDialogTitle(appContext.getString(R.string.freeze_title))
                    )

                    if (freezes <= 0) {
                        builder
                            .setMessage(appContext.getString(R.string.freeze_unavailable_message))
                            .setPositiveButton(appContext.getString(R.string.ok_label)) { _, _ ->
                                checkIsNewDay()
                            }
                    }

                    else {
                        builder
                            .setMessage(
                                "You have $freezes freeze(s) left. " +
                                        appContext.getString(R.string.freeze_available_message)
                            )
                            .setPositiveButton(appContext.getString(R.string.yes_label)) { _, _ ->
                                if (!checkIsNewDay()) {
                                    challengeViewModel.freezeDay()
                                }
                            }
                            .setNeutralButton(appContext.getString(R.string.no_label)) { _, _ ->
                                checkIsNewDay()
                            }
                    }

                    val alert = builder.create()
                    alert.show()
                    setDialogFont(alert)
                }
            }
        }
    }

    /**
     * Checks if the user gain a freeze since last login and shows an message if true
     */
    private fun checkGainedFreeze() {
        if (challengeViewModel.showFreezeMsg()) {
            val appContext: Context? = activity?.applicationContext

            if (appContext != null) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context)

                builder
                    .setCustomTitle(
                        createDialogTitle(appContext.getString(R.string.freeze_gained_title))
                    )
                    .setMessage(
                        appContext.getString(
                            R.string.freeze_gained_message) +
                                "You have ${challengeViewModel.getFreezes()} freeze(s)."
                    )
                    .setPositiveButton(appContext.getString(R.string.ok_label)) { _, _ ->
                        checkIsNewDay()
                    }

                val alert = builder.create()
                alert.show()
                setDialogFont(alert)
            }
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

    private fun createDialogTitle(text: String): TextView {
        val appContext = activity?.applicationContext

        val paddingVal: Int = (resources.displayMetrics.density * 22f).toInt()
        val title = TextView(appContext)
        title.setPadding(paddingVal, paddingVal, paddingVal, 0)

        title.text = text
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)

        if (appContext != null) {
            title.setTextColor(ContextCompat.getColor(appContext, android.R.color.black))
            title.typeface = ResourcesCompat.getFont(appContext, R.font.asap)
        }

        return title
    }

    private fun setDialogFont(alert: AlertDialog) {
        val window: Window? = alert.window
        val appContext = activity?.applicationContext

        var messageFont: Typeface? = null
        var buttonFont: Typeface? = null

        appContext?.let {
            messageFont = ResourcesCompat.getFont(it, R.font.timeless)
            buttonFont = ResourcesCompat.getFont(it, R.font.asap_bold)
        }

        if (window != null) {
            window.findViewById<TextView>(android.R.id.message).typeface = messageFont
            window.findViewById<TextView>(android.R.id.button1).typeface = buttonFont
            window.findViewById<TextView>(android.R.id.button3).typeface = buttonFont
        }
    }

}
