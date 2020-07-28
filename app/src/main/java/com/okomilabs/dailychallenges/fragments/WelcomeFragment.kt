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
import androidx.navigation.fragment.findNavController
import com.okomilabs.dailychallenges.R

class WelcomeFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_welcome, container, false)

        val welcome: TextView = root.findViewById(R.id.welcome_message)
        animateWelcome(welcome)

        return root
    }

    private fun animateWelcome(welcome: TextView) {
        welcome
            .animate()
            .translationYBy(-100f)
            .alpha(1.0f)
            .setDuration(1000L)
            .setStartDelay(500L)
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
            .translationYBy(-100f)
            .alpha(0.0f)
            .setDuration(1000L)
            .setStartDelay(500L)
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
        findNavController().navigate(WelcomeFragmentDirections.welcomeToChallenge())
    }
}