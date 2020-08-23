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

        // Exit transition
        exitTransition = Slide(Gravity.START).setInterpolator(LinearOutSlowInInterpolator())

        return root
    }

    /**
     * Starts the animation for the first message which prompts the next animations to start
     *
     * @param message The first view containing a thank you message
     * @param prompt The second view asking the user if they want to see the guide
     * @param yes The button which directs the user to the tutorial page
     * @param no The button which directs the user to the welcome page
     */
    private fun animateMessage(message: CardView, prompt: CardView, yes: Button, no: Button) {
        val messageAnimator: ViewPropertyAnimator = message.animate()

        applyAnimation(messageAnimator)
        messageAnimator
            .setStartDelay(700L)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    animatePrompt(prompt, yes, no)      // Starts next animations
                }
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
            })
    }

    /**
     * Starts the animation for the second message which prompts the next animations to start
     *
     * @param prompt The second view asking the user if they want to see the guide
     * @param yes The button which directs the user to the tutorial page
     * @param no The button which directs the user to the welcome page
     */
    private fun animatePrompt(prompt: CardView, yes: Button, no: Button) {
        val promptAnimator: ViewPropertyAnimator = prompt.animate()

        applyAnimation(promptAnimator)
        promptAnimator
            .setStartDelay(1200L)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    animateButtons(yes, no)      // Starts next animations
                }
                override fun onAnimationStart(animation: Animator?) {}
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
            })
    }

    /**
     * Starts the animation for the buttons which prompts the last animation
     *
     * @param yes The button which directs the user to the tutorial page
     * @param no The button which directs the user to the welcome page
     */
    private fun animateButtons(yes: Button, no: Button) {
        val yesAnimator: ViewPropertyAnimator = yes.animate()

        applyAnimation(yesAnimator)
        yesAnimator
            .setStartDelay(200L)
            .setListener(object: Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    applyAnimation(no.animate())      // Starts last animation
                }
                override fun onAnimationStart(animation: Animator?) {
                    buttonFunctionality(yes, no)      // Allows the user to press buttons
                }
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
            })
    }

    /**
     * Applies the shared animation to the view being animated
     *
     * @param animator The animator of the view to be animated
     */
    private fun applyAnimation(animator: ViewPropertyAnimator) {
        animator
            .alpha(1.0f)
            .setDuration(1100L)
            .interpolator = DecelerateInterpolator()
    }

    /**
     * Sets up the functionality for both buttons
     *
     * @param yes The button which directs the user to the tutorial page
     * @param no The button which directs the user to the welcome page
     */
    private fun buttonFunctionality(yes: Button, no: Button) {
        yes.setOnClickListener {
            findNavController().navigate(FirstLaunchFragmentDirections.firstLaunchToHelp())
        }

        no.setOnClickListener {
            findNavController().navigate(FirstLaunchFragmentDirections.firstLaunchToWelcome())
        }
    }
}