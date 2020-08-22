package com.okomilabs.dailychallenges.fragments

import android.animation.Animator
import android.os.Bundle
import android.transition.Slide
import android.view.*
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import com.okomilabs.dailychallenges.R

class FirstLaunchFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_first_launch, container, false)

        animateMessage(
            root.findViewById(R.id.launch_message_1),
            root.findViewById(R.id.launch_message_2),
            root.findViewById(R.id.launch_yes_button),
            root.findViewById(R.id.launch_no_button)
        )

        exitTransition = Slide(Gravity.START).setInterpolator(LinearOutSlowInInterpolator())

        return root
    }

    private fun animateMessage(message: CardView, prompt: CardView, yes: Button, no: Button) {
        val messageAnimator: ViewPropertyAnimator = message.animate()

        applyAnimation(messageAnimator)
        messageAnimator
            .setStartDelay(700L)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    animatePrompt(prompt, yes, no)
                }
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
            })
    }


    private fun animatePrompt(prompt: CardView, yes: Button, no: Button) {
        val promptAnimator: ViewPropertyAnimator = prompt.animate()

        applyAnimation(promptAnimator)
        promptAnimator
            .setStartDelay(1200L)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    animateButtons(yes, no)
                }
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
            })
    }

    private fun animateButtons(yes: Button, no: Button) {
        val yesAnimator: ViewPropertyAnimator = yes.animate()

        applyAnimation(yesAnimator)
        yesAnimator
            .setStartDelay(200L)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    applyAnimation(no.animate())
                    buttonFunctionality(yes, no)
                }
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
            })
    }

    private fun applyAnimation(animator: ViewPropertyAnimator) {
        animator
            .alpha(1.0f)
            .setDuration(1100L)
            .interpolator = DecelerateInterpolator()
    }

    private fun buttonFunctionality(yes: Button, no: Button) {
        yes.setOnClickListener {
            findNavController().navigate(FirstLaunchFragmentDirections.firstLaunchToHelp())
        }

        no.setOnClickListener {
            findNavController().navigate(FirstLaunchFragmentDirections.firstLaunchToWelcome())
        }
    }
}