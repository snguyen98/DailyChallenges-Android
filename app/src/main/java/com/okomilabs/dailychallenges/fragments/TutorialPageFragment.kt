package com.okomilabs.dailychallenges.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.okomilabs.dailychallenges.R

class TutorialPageFragment(pos: Int): Fragment() {
    private val position = pos

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_tutorial_page, container, false)

        val image: ImageView = root.findViewById(R.id.example_image)
        val text: TextView = root.findViewById(R.id.tutorial_text)

        when (position) {
            0 -> {
                image.setImageResource(R.mipmap.tutorial_page_1)
                text.setText(R.string.tutorial_text_1)
            }
            1 -> {
                image.setImageResource(R.mipmap.tutorial_page_2)
                text.setText(R.string.tutorial_text_2)
            }
            2 ->{
                image.setImageResource(R.mipmap.tutorial_page_3)
                text.setText(R.string.tutorial_text_3)
            }
            3 -> {
                image.setImageResource(R.mipmap.tutorial_page_4)
                text.setText(R.string.tutorial_text_4)
            }
        }

        return root
    }
}