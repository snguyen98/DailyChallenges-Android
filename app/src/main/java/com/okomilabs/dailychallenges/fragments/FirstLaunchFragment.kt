package com.okomilabs.dailychallenges.fragments

import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        root.findViewById<Button>(R.id.launch_yes_button).setOnClickListener {
            findNavController().navigate(FirstLaunchFragmentDirections.firstLaunchToHelp())
        }

        root.findViewById<Button>(R.id.launch_no_button).setOnClickListener {
            findNavController().navigate(FirstLaunchFragmentDirections.firstLaunchToWelcome())
        }

        exitTransition = Slide(Gravity.START).setInterpolator(LinearOutSlowInInterpolator())

        return root
    }
}