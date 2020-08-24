package com.okomilabs.dailychallenges.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.okomilabs.dailychallenges.R

class AboutFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_about, container, false)

        // Sets up recycler view adapter for about list
        setList(
            root.findViewById(R.id.about_list),
            getString(R.string.about_label),
            AboutListAdapter(
                listOf(
                    Pair(getString(R.string.twitter_label), getString(R.string.twitter_link)),
                    Pair(getString(R.string.instagram_label), getString(R.string.instagram_link)),
                    Pair(getString(R.string.facebook_label), getString(R.string.facebook_link)),
                    Pair(getString(R.string.linkedin_label), getString(R.string.linkedin_link)),
                    Pair(getString(R.string.team_label), "")
                )
            )
        )

        // Sets up recycler view adapter for legal list
        setList(
            root.findViewById(R.id.legal_list),
            getString(R.string.legal_label),
            AboutListAdapter(
                listOf(
                    Pair(getString(R.string.terms_label), getString(R.string.terms_link)),
                    Pair(getString(R.string.privacy_label), getString(R.string.privacy_link))
                )
            )
        )

        // Transitions
        enterTransition = Slide(Gravity.END).setInterpolator(LinearOutSlowInInterpolator())
        exitTransition = Slide(Gravity.START).setInterpolator(LinearOutSlowInInterpolator())
        postponeEnterTransition()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        startPostponedEnterTransition()
    }

    /**
     * Sets up the recycler view given a layout, heading and adapter
     *
     * @param layout The layout containing the recycler view
     * @param heading The heading label
     * @param adapter The adapter to be assigned to the recycler view
     */
    private fun setList(layout: LinearLayout, heading: String, adapter: AboutListAdapter) {
        val recyclerView: RecyclerView = layout.findViewById(R.id.list_items)

        layout.findViewById<TextView>(R.id.list_heading).text = heading

        recyclerView.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            RecyclerView.VERTICAL,
            false
        )

        recyclerView.adapter = adapter
    }

    /**
     * Class which manages the items to be displayed in the about and legal list recycler views
     *
     * @param aboutItems The list of items to be displayed
     */
    private inner class AboutListAdapter(
        private val aboutItems: List<Pair<String, String>>
    ): RecyclerView.Adapter<AboutListAdapter.AboutItemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AboutItemHolder {
            return AboutItemHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.about_item, parent, false
                )
            )
        }

        override fun getItemCount(): Int = aboutItems.size

        override fun onBindViewHolder(holder: AboutItemHolder, position: Int) {
            holder.bind(aboutItems[position], position == 0)
        }

        /**
         * Class that holds and binds the information to each about item in the recycler view
         *
         * @param view The about item view
         */
        private inner class AboutItemHolder(view: View): RecyclerView.ViewHolder(view) {
            val aboutItem: RelativeLayout = view.findViewById(R.id.about_item)
            val aboutText: TextView = view.findViewById(R.id.about_text)
            val aboutIcon: ImageView = view.findViewById(R.id.about_icon)
            val topDivider: View = view.findViewById(R.id.top_divider)

            /**
             * Assigns the text to text views, sets appropriate links, icons and a divider above
             * the first element
             *
             * @param item The pair containing the about item label and link
             * @param isFirst Boolean stating if the item is the first in the list
             */
            fun bind(item: Pair<String, String>, isFirst: Boolean) {
                val (label, link) = item

                aboutText.text = label

                // Assigns the appropriate link to the about item
                aboutItem.setOnClickListener {
                    if (item.second != "") {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    }
                    else {
                        findNavController().navigate(AboutFragmentDirections.aboutToCredits())
                    }
                }

                // Assigns the appropriate icon to the about item
                when (label) {
                    getString(R.string.twitter_label) ->
                        aboutIcon.setImageResource(R.mipmap.twitter_icon)

                    getString(R.string.instagram_label) ->
                        aboutIcon.setImageResource(R.mipmap.instagram_icon)

                    getString(R.string.facebook_label) ->
                        aboutIcon.setImageResource(R.mipmap.facebook_icon)

                    getString(R.string.linkedin_label) ->
                        aboutIcon.setImageResource(R.mipmap.linkedin_icon)

                    getString(R.string.team_label) ->
                        aboutIcon.setImageResource(R.mipmap.team_icon)

                    getString(R.string.terms_label) ->
                        aboutIcon.setImageResource(R.mipmap.terms_icon)

                    getString(R.string.privacy_label) ->
                        aboutIcon.setImageResource(R.mipmap.privacy_icon)
                }

                // Places divider on top of first element only
                if (isFirst) {
                    topDivider.visibility = View.VISIBLE
                }
            }
        }
    }

}