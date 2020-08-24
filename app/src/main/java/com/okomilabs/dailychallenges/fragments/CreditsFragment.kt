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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.okomilabs.dailychallenges.R

class CreditsFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_credits, container, false)

            // Sets up recycler view adapter for our team list
            setList(
                root.findViewById(R.id.team_list),
                getString(R.string.team_label),
                CreditsListAdapter(
                    listOf(
                        Triple(
                            getString(R.string.sang_name),
                            getString(R.string.sang_role),
                            Pair(getString(R.string.linkedin_label), getString(R.string.sang_link))
                        ),
                        Triple(
                            getString(R.string.ned_name),
                            getString(R.string.ned_role),
                            Pair(getString(R.string.linkedin_label), getString(R.string.ned_link))
                        ),
                        Triple(
                            getString(R.string.saif_name),
                            getString(R.string.saif_role),
                            Pair(getString(R.string.linkedin_label), getString(R.string.saif_link))
                        ),
                        Triple(
                            getString(R.string.ross_name),
                            getString(R.string.ross_role),
                            Pair(getString(R.string.linkedin_label), getString(R.string.ross_link))
                        )
                    )
                )
            )

            // Sets up recycler view adapter for special thanks list
            setList(
                root.findViewById(R.id.thanks_list),
                getString(R.string.thanks_label),
                CreditsListAdapter(
                    listOf(
                        Triple(
                            getString(R.string.kenny_name),
                            getString(R.string.kenny_role),
                            Pair(getString(R.string.linkedin_label), getString(R.string.kenny_link))
                        ),
                        Triple(
                            getString(R.string.rijal_name),
                            getString(R.string.rijal_role),
                            Pair(getString(R.string.instagram_label),
                                getString(R.string.rijal_link)
                            )
                        )
                    )
                )
            )

        // Transitions
        enterTransition = Slide(Gravity.END).setInterpolator(LinearOutSlowInInterpolator())
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
    private fun setList(layout: LinearLayout, heading: String, adapter: CreditsListAdapter) {
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
     * Class which manages the items to be displayed in the team and special thanks recycler views
     *
     * @param creditsItems The list of items to be displayed
     */
    private inner class CreditsListAdapter(
        private val creditsItems: List<Triple<String, String, Pair<String, String>>>
    ): RecyclerView.Adapter<CreditsListAdapter.CreditsItemHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditsItemHolder {
            return CreditsItemHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.credits_item, parent, false
                )
            )
        }

        override fun getItemCount(): Int = creditsItems.size

        override fun onBindViewHolder(holder: CreditsItemHolder, position: Int) {
            holder.bind(creditsItems[position], position == 0)
        }

        /**
         * Class that holds and binds the information to each credit item in the recycler view
         *
         * @param view The credit item view
         */
        private inner class CreditsItemHolder(view: View): RecyclerView.ViewHolder(view) {
            val creditsItem: RelativeLayout = view.findViewById(R.id.credits_item)
            val creditsName: TextView = view.findViewById(R.id.credits_name)
            val creditsRole: TextView = view.findViewById(R.id.credits_role)
            val creditsLinkIcon: ImageView = view.findViewById(R.id.credits_link_icon)
            val topDivider: View = view.findViewById(R.id.top_divider)

            /**
             * Assigns the text to text views, sets appropriate links, icons and a divider above
             * the first element
             *
             * @param item The triple containing the name, role, link type and link of the credited
             * @param isFirst Boolean stating if the item is the first in the list
             */
            fun bind(item: Triple<String, String, Pair<String, String>>, isFirst: Boolean) {
                val (name, role, linkPair) = item
                val (type, link) = linkPair

                creditsName.text = name
                creditsRole.text = role

                // Assigns the appropriate icon for the credited person
                when (type) {
                    getString(R.string.instagram_label) ->
                        creditsLinkIcon.setImageResource(R.mipmap.instagram_icon)

                    getString(R.string.linkedin_label) ->
                        creditsLinkIcon.setImageResource(R.mipmap.linkedin_icon)
                }

                // Directs the user to the appropriate link to the credited person
                creditsItem.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                }

                // Places divider on top of first element only
                if (isFirst) {
                    topDivider.visibility = View.VISIBLE
                }
            }
        }
    }
}