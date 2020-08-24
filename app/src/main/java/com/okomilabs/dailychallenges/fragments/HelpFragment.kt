package com.okomilabs.dailychallenges.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.okomilabs.dailychallenges.R

private const val PAGES = 4

class HelpFragment: Fragment() {
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_help, container, false)

        viewPager = root.findViewById(R.id.pager)
        viewPager.adapter = SlideAdapter(this)

        viewPager.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            // Sets the dots and button in the main layout whenever the page changes
            override fun onPageSelected(position: Int) {
                setDots(
                    position,
                    root.findViewById(R.id.pager_dots)
                )
                setButton(position, root.findViewById(R.id.tutorial_button))
                startPostponedEnterTransition()
            }
        })

        // Transitions
        enterTransition = Slide(Gravity.END).setInterpolator(LinearOutSlowInInterpolator())
        exitTransition = Slide(Gravity.START).setInterpolator(LinearOutSlowInInterpolator())
        postponeEnterTransition()

        return root
    }

    /**
     * Sets the navigation dots depending on the current page
     *
     * @param active The dot corresponding the the current page
     * @param dots The linear layout containing all of the dots
     */
    private fun setDots(active: Int, dots: LinearLayout) {
        activity?.applicationContext?.let { appContext ->
            val dotsArray: Array<View> = arrayOf(
                dots.findViewById(R.id.pager_dot_1),
                dots.findViewById(R.id.pager_dot_2),
                dots.findViewById(R.id.pager_dot_3),
                dots.findViewById(R.id.pager_dot_4)
            )

            for (num in 0..3) {
                var dot: Drawable? = ContextCompat.getDrawable(appContext, R.drawable.inactive_dot)

                // Sets the dot corresponding to the current page to white and the others to grey
                if (num == active) {
                    dot = ContextCompat.getDrawable(appContext, R.drawable.active_dot)
                }

                else {
                    // Also redirects to the corresponding page whenever an inactive dot is pressed
                    dotsArray[num].setOnClickListener {
                        viewPager.currentItem = num
                    }
                }

                dotsArray[num].background = dot
            }
        }
    }

    /**
     * Sets the button message and functionality depending on the current page
     *
     * @param active The dot corresponding the the current page
     * @param button The help page button
     */
    private fun setButton(active: Int, button: Button) {
        // If the current page is the last one then set up the button as a finish button
        if (active == 3) {
            button.text = getString(R.string.finish_label)
            finishButtonFunctionality(button)
        }
        // Otherwise set up the button as a next button
        else {
            button.text = getString(R.string.next_label)
            nextButtonFunctionality(active, button)
        }
    }

    /**
     * Sets up the button functionality to move to the next page
     *
     * @param position The current page position
     * @param button The tutorial page button
     */
    private fun nextButtonFunctionality(position: Int, button: Button) {
        button.setOnClickListener {
            viewPager.currentItem = position + 1
        }
    }

    /**
     * Sets up the button functionality to redirect to the welcome fragment
     *
     * @param button The tutorial page button
     */
    private fun finishButtonFunctionality(button: Button) {
        button.setOnClickListener {
            findNavController().navigate(HelpFragmentDirections.helpToWelcome())
        }
    }

    /**
     * Class that generates the appropriate fragments for each page in the view pager
     *
     * @param fragment The fragment containing the view pager
     */
    private inner class SlideAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = PAGES
        override fun createFragment(position: Int): Fragment = TutorialPageFragment(position)
    }

}