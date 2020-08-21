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
            override fun onPageSelected(position: Int) {
                setDots(
                    position,
                    root.findViewById(R.id.pager_dots)
                )
                setButtons(position, root.findViewById(R.id.tutorial_button))
                startPostponedEnterTransition()
            }
        })

        enterTransition = Slide(Gravity.END).setInterpolator(LinearOutSlowInInterpolator())
        exitTransition = Slide(Gravity.START).setInterpolator(LinearOutSlowInInterpolator())
        postponeEnterTransition()

        return root
    }

    private fun setDots(active: Int, dots: LinearLayout) {
        val appContext = activity?.applicationContext

        if (appContext != null) {
            val dotsArray: Array<View> = arrayOf(
                dots.findViewById(R.id.pager_dot_1),
                dots.findViewById(R.id.pager_dot_2),
                dots.findViewById(R.id.pager_dot_3),
                dots.findViewById(R.id.pager_dot_4)
            )

            for (num in 0..3) {
                var dot: Drawable? = ContextCompat.getDrawable(appContext, R.drawable.inactive_dot)

                if (num == active) {
                    dot = ContextCompat.getDrawable(appContext, R.drawable.active_dot)
                }
                else {
                    dotsArray[num].setOnClickListener {
                        viewPager.currentItem = num
                    }
                }

                dotsArray[num].background = dot
            }
        }
    }

    private fun setButtons(active: Int, button: Button) {
        val appContext = activity?.applicationContext

        if (appContext != null) {
            if (active == 3) {
                button.text = appContext.getString(R.string.finish_label)
                finishButtonFunctionality(button)
            }
            else {
                button.text = appContext.getString(R.string.next_label)
                nextButtonFunctionality(active, button)
            }
        }
    }

    private fun nextButtonFunctionality(position: Int, button: Button) {
        button.setOnClickListener {
            viewPager.currentItem = position + 1
        }
    }

    private fun finishButtonFunctionality(button: Button) {
        button.setOnClickListener {
            findNavController().navigate(HelpFragmentDirections.helpToWelcome())
        }
    }

    private inner class SlideAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = PAGES
        override fun createFragment(position: Int): Fragment = TutorialPageFragment(position)
    }

}