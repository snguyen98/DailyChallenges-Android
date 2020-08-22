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
        val root = inflater.inflate(R.layout.fragment_welcome, container, false)

        if (!checkFirstLaunch()) {
            val welcome: TextView = root.findViewById(R.id.welcome_message)
            animateWelcome(welcome)
        }

        return root
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
            val firstLaunchStr: String = getString(R.string.first_launch)

            return if (!settingsPrefs.getBoolean(firstLaunchStr, false)) {
                settingsPrefs.edit().putBoolean(firstLaunchStr, true).apply()
                findNavController().navigate(WelcomeFragmentDirections.welcomeToFirstLaunch())
                true
            } else {
                false
            }
        }

        return false
    }

    private fun animateWelcome(welcome: TextView) {
        welcome
            .animate()
            .translationYBy(-80f)
            .alpha(1.0f)
            .setDuration(1000L)
            .setStartDelay(300L)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    hideWelcome(welcome)
                }
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
            })
            .interpolator = DecelerateInterpolator()

    }

    private fun hideWelcome(welcome: TextView) {
        welcome
            .animate()
            .translationYBy(-80f)
            .alpha(0.0f)
            .setDuration(1000L)
            .setStartDelay(300L)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    navigateToChallenge()
                }
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
            })
            .interpolator = AccelerateInterpolator()
    }

    private fun navigateToChallenge() {
        val navController: NavController = findNavController()

        if (navController.currentDestination?.id == R.id.welcome_fragment) {
            navController.navigate(WelcomeFragmentDirections.welcomeToChallenge())
        }
    }
}