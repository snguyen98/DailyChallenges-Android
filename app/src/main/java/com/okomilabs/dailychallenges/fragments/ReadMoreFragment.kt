package com.okomilabs.dailychallenges.fragments

import android.os.Bundle
import android.transition.Slide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.okomilabs.dailychallenges.R



class ReadMoreFragment :


    Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_read_more, container, false)
        /* Shared element transition
        val callback: SharedElementCallback = object: SharedElementCallback() {
            override fun onSharedElementEnd(
                sharedElementNames: MutableList<String>?,
                sharedElements: MutableList<View>?,
                sharedElementSnapshots: MutableList<View>?
            ) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
            }
        }

        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.read_more_transition)

        setEnterSharedElementCallback(callback)
         */

        // Simple slide enter transition
        enterTransition = Slide()

        val challenge: Array<String>? = arguments?.getStringArray("challenge")

        val title: TextView = root.findViewById(R.id.challenge_title)
        val category: TextView = root.findViewById(R.id.challenge_category)
        val summary: TextView = root.findViewById(R.id.challenge_summary)
        val desc: TextView = root.findViewById(R.id.challenge_desc)

        //Adds back button to action bar
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
