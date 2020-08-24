package com.okomilabs.dailychallenges.fragments

import android.animation.Animator
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.okomilabs.dailychallenges.R

class WelcomeFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // If first launch, set first launch shared prefs to false and redirect to first launch page
        if (checkFirstLaunch()) {
            activity?.applicationContext?.let { appContext ->
                appContext
                    .getSharedPreferences(
                        getString(R.string.settings_key), Context.MODE_PRIVATE
                    )
                    .edit()
                    .putBoolean(getString(R.string.first_launch), false)
                    .apply()

                findNavController().navigate(WelcomeFragmentDirections.welcomeToFirstLaunch())
            }
        }

        // Otherwise animate welcome message after view has loaded
        else {
            animateWelcome(view.findViewById(R.id.welcome_message))
        }
    }

    /**
     * Checks if this is the user's first time launching the app and redirects if true
     *
     * @return True if first launch, false otherwise
     */
    private fun checkFirstLaunch(): Boolean {
        activity?.applicationContext?.let {
            val settingsPrefs: SharedPreferences = it.getSharedPreferences(
                getString(R.string.settings_key), Context.MODE_PRIVATE
            )

            return settingsPrefs.getBoolean(getString(R.string.first_launch), true)
        }

        return false
    }

    /**
     * Starts the animation to show of the welcome message
     *
     * @param welcome The text view containing the welcome message
     */
    private fun animateWelcome(welcome: TextView) {
        welcome
            .animate()
            .translationYBy(-80f)
            .alpha(1.0f)
            .setDuration(1000L)
            .setStartDelay(300L)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    hideWelcome(welcome)    // Waits and then hides the welcome message
                }
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
            })
            .interpolator = DecelerateInterpolator()

    }

    /**
     * Starts the animation to hide the welcome message
     *
     * @param welcome The text view containing the welcome message
     */
    private fun hideWelcome(welcome: TextView) {
        welcome
            .animate()
            .translationYBy(-80f)
            .alpha(0.0f)
            .setDuration(1000L)
            .setStartDelay(300L)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    navigateToChallenge()       // Waits and then redirects to the challenge page
                }
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
            })
            .interpolator = AccelerateInterpolator()
    }

    /**
     * Redirects to the challenge fragment
     */
    private fun navigateToChallenge() {
        val navController: NavController = findNavController()

        if (navController.currentDestination?.id == R.id.welcome_fragment) {
            navController.navigate(WelcomeFragmentDirections.welcomeToChallenge())
        }
    }
}