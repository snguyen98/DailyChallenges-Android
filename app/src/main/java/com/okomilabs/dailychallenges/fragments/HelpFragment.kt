package com.okomilabs.dailychallenges.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
                setDots(position, root.findViewById(R.id.pager_dots))
            }
        })

        return root
    }

    private inner class SlideAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = PAGES
        override fun createFragment(position: Int): Fragment = TutorialPageFragment(position)
    }

    private fun setDots(active: Int, dots: LinearLayout) {
        val appContext = activity?.applicationContext

        if (appContext != null) {
            for (num in 0..3) {
                var dot: Drawable? = ContextCompat.getDrawable(appContext, R.drawable.inactive_dot)

                if (num == active) {
                    dot = ContextCompat.getDrawable(appContext, R.drawable.active_dot)
                }

                when (num) {
                    0 -> dots.findViewById<View>(R.id.pager_dot_1).background = dot
                    1 -> dots.findViewById<View>(R.id.pager_dot_2).background = dot
                    2 -> dots.findViewById<View>(R.id.pager_dot_3).background = dot
                    3 -> dots.findViewById<View>(R.id.pager_dot_4).background = dot
                }
            }
        }
    }

}