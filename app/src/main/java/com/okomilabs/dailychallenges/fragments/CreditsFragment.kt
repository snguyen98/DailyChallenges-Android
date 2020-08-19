package com.okomilabs.dailychallenges.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
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

        activity?.applicationContext?.let { appContext ->
            setList(
                root.findViewById(R.id.team_list),
                appContext.getString(R.string.team_label),
                CreditsListAdapter(
                    listOf(
                        Triple(
                            appContext.getString(R.string.sang_name),
                            appContext.getString(R.string.sang_role),
                            Pair(
                                appContext.getString(R.string.linkedin_label),
                                appContext.getString(R.string.sang_link)
                            )
                        ),
                        Triple(
                            appContext.getString(R.string.ned_name),
                            appContext.getString(R.string.ned_role),
                            Pair(
                                appContext.getString(R.string.linkedin_label),
                                appContext.getString(R.string.ned_link)
                            )
                        ),
                        Triple(
                            appContext.getString(R.string.saif_name),
                            appContext.getString(R.string.saif_role),
                            Pair(
                                appContext.getString(R.string.linkedin_label),
                                appContext.getString(R.string.saif_link)
                            )
                        ),
                        Triple(
                            appContext.getString(R.string.ross_name),
                            appContext.getString(R.string.ross_role),
                            Pair(
                                appContext.getString(R.string.linkedin_label),
                                appContext.getString(R.string.ross_link)
                            )
                        )
                    )
                )
            )

            setList(
                root.findViewById(R.id.thanks_list),
                appContext.getString(R.string.thanks_label),
                CreditsListAdapter(
                    listOf(
                        Triple(
                            appContext.getString(R.string.kenny_name),
                            appContext.getString(R.string.kenny_role),
                            Pair(
                                appContext.getString(R.string.linkedin_label),
                                appContext.getString(R.string.kenny_link)
                            )
                        ),
                        Triple(
                            appContext.getString(R.string.rijal_name),
                            appContext.getString(R.string.rijal_role),
                            Pair(
                                appContext.getString(R.string.instagram_label),
                                appContext.getString(R.string.rijal_link)
                            )
                        )
                    )
                )
            )
        }

        return root
    }

    private fun setList(layout: LinearLayout, heading: String, adapter: CreditsListAdapter) {
        val appContext = activity?.applicationContext
        val recyclerView: RecyclerView = layout.findViewById(R.id.list_items)

        layout.findViewById<TextView>(R.id.list_heading).text = heading

        recyclerView.layoutManager = LinearLayoutManager(
            appContext,
            RecyclerView.VERTICAL,
            false
        )

        recyclerView.suppressLayout(true)
        recyclerView.adapter = adapter
    }

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

        private inner class CreditsItemHolder(view: View): RecyclerView.ViewHolder(view) {
            val creditsItem: RelativeLayout = view.findViewById(R.id.credits_item)
            val creditsName: TextView = view.findViewById(R.id.credits_name)
            val creditsRole: TextView = view.findViewById(R.id.credits_role)
            val creditsLinkIcon: ImageView = view.findViewById(R.id.credits_link_icon)
            val topDivider: View = view.findViewById(R.id.top_divider)

            fun bind(item: Triple<String, String, Pair<String, String>>, isFirst: Boolean) {
                val (name, role, linkPair) = item
                val (type, link) = linkPair

                creditsName.text = name
                creditsRole.text = role

                activity?.applicationContext?.let { appContext ->
                    when (type) {
                        appContext.getString(R.string.instagram_label) ->
                            creditsLinkIcon.setImageResource(R.mipmap.instagram_icon)

                        appContext.getString(R.string.linkedin_label) ->
                            creditsLinkIcon.setImageResource(R.mipmap.linkedin_icon)
                    }
                }

                creditsItem.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                }

                if (isFirst) {
                    topDivider.visibility = View.VISIBLE
                }
            }
        }
    }
}