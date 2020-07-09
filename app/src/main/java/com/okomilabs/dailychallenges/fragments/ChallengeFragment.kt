package com.okomilabs.dailychallenges.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.viewmodels.ChallengeViewModel

class ChallengeFragment: Fragment() {
    private lateinit var challengeViewModel: ChallengeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        challengeViewModel = ViewModelProvider(this).get(ChallengeViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_challenge, container, false)

        val title: TextView = root.findViewById(R.id.challenge_title)
        val category: TextView = root.findViewById(R.id.challenge_category)

        val titleObserver = Observer<String> { newTitle ->
            title.text = newTitle
        }
        val categoryObserver = Observer<String> { newCategory ->
            category.text = newCategory
        }

        challengeViewModel.title.observe(viewLifecycleOwner, titleObserver)
        challengeViewModel.category.observe(viewLifecycleOwner, categoryObserver)

        val skip: Button = root.findViewById(R.id.skip_button)
        skip.setOnClickListener {

            if (challengeViewModel.isNewDay()) {
                refreshFragment()
            }

            else {
                val skips: Int = challengeViewModel.getSkips()
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Skip Challenge")

                if (skips > 0) {
                    val msg = "You have $skips skips left. Would you like to skip?"
                    builder.setMessage(msg)
                    builder.setPositiveButton("Yes") { _, _ ->
                        challengeViewModel.skipChallenge()
                    }
                    builder.setNeutralButton("Cancel") { _, _ -> }
                }

                else {
                    builder.setMessage("You have no skips left")
                    builder.setPositiveButton("Ok") { _, _ -> }
                }

                val alert: AlertDialog = builder.create()
                alert.show()
            }
        }

        val complete: Button = root.findViewById(R.id.complete_button)
        complete.setOnClickListener {
            if (challengeViewModel.isNewDay()) {
                refreshFragment()
            }
            else {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                builder.setTitle("Mark as Complete")
                builder.setMessage("Would you like to mark this challenge as complete?")

                builder.setPositiveButton("Yes") { _, _ ->
                    challengeViewModel.markComplete()
                }
                builder.setNeutralButton("No") { _, _ -> }

                val alert: AlertDialog = builder.create()
                alert.show()

            }
        }

        // TEMPORARY NAVIGATION
        val card: CardView = root.findViewById(R.id.challenge_card)
        card.setOnClickListener {
            card.findNavController().navigate(
                ChallengeFragmentDirections.challengeToReadMore(arrayOf(
                    challengeViewModel.title.value.toString(),
                    challengeViewModel.category.value.toString(),
                    challengeViewModel.summary.value.toString(),
                    challengeViewModel.desc.value.toString()
                ))/*,
                // Code for shared element transition
                FragmentNavigatorExtras(
                    card to "card_element",
                    title to "title_element",
                    category to "category_element"
                )*/
            )
        }

        return root
    }

    /**
     * Reloads the fragment
     */
    private fun refreshFragment() {
        val transaction = this.parentFragmentManager.beginTransaction()
        transaction.detach(this).attach(this).commit()
    }
}
