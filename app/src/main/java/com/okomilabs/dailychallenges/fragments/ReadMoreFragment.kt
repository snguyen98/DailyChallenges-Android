package com.okomilabs.dailychallenges.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.viewmodels.ChallengeViewModel

class ReadMoreFragment : Fragment() {
    private lateinit var challengeViewModel: ChallengeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        challengeViewModel = ViewModelProvider(this).get(ChallengeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_read_more, container, false)

        val title: TextView = root.findViewById(R.id.challenge_title)
        val category: TextView = root.findViewById(R.id.challenge_category)
        val summary: TextView = root.findViewById(R.id.challenge_summary)
        val desc: TextView = root.findViewById(R.id.challenge_desc)

        val titleObserver = Observer<String> { newTitle ->
            title.text = newTitle
        }
        val categoryObserver = Observer<String> { newCategory ->
            category.text = newCategory
        }
        val summaryObserver = Observer<String> { newSummary ->
            summary.text = newSummary
        }
        val descObserver = Observer<String> { newDesc ->
            desc.text = newDesc
        }

        challengeViewModel.title.observe(viewLifecycleOwner, titleObserver)
        challengeViewModel.category.observe(viewLifecycleOwner, categoryObserver)
        challengeViewModel.summary.observe(viewLifecycleOwner, summaryObserver)
        challengeViewModel.desc.observe(viewLifecycleOwner, descObserver)

        return root
    }
}
