package com.okomilabs.dailychallenges.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.viewmodels.ChallengeViewModel

class ReadMoreFragment : Fragment() {
    private lateinit var challengeViewModel: ChallengeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_read_more, container, false)

        val challenge: Array<String>? = arguments?.getStringArray("challenge")

        val title: TextView = root.findViewById(R.id.challenge_title)
        val category: TextView = root.findViewById(R.id.challenge_category)
        val summary: TextView = root.findViewById(R.id.challenge_summary)
        val desc: TextView = root.findViewById(R.id.challenge_desc)

        title.text = challenge?.get(0) ?: ""
        category.text = challenge?.get(1) ?: ""
        summary.text = challenge?.get(2) ?: ""
        desc.text = challenge?.get(3) ?: ""

        // TEMPORARY NAVIGATION
        val image: ImageView = root.findViewById(R.id.category_image)
        image.setOnClickListener {
            image.findNavController().navigate(ReadMoreFragmentDirections.readMoreToChallenge())
        }

        return root
    }
}
