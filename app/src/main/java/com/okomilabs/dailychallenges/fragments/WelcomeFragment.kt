package com.okomilabs.dailychallenges.fragments

import android.animation.Animator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.viewmodels.WelcomeViewModel

class WelcomeFragment: Fragment() {
    private lateinit var welcomeViewModel: WelcomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        welcomeViewModel = ViewModelProvider(this).get(WelcomeViewModel::class.java)

        return inflater.inflate(R.layout.fragment_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // If first launch, set first launch shared prefs to false and redirect to first launch page
        if (welcomeViewModel.checkFirstLaunch()) {
            welcomeViewModel.disableFirstLaunch()

            findNavController().navigate(WelcomeFragmentDirections.welcomeToFirstLaunch())
        }

        // Otherwise animate welcome message after view has loaded
        else {
            if (welcomeViewModel.hasShownWelcome()) {
                navigateToChallenge()
            }
            else {
                animateWelcome(view.findViewById(R.id.welcome_message))
            }
        }
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