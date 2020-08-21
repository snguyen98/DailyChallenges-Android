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
        enterTransition = Slide(Gravity.END)

        val root = inflater.inflate(R.layout.fragment_about, container, false)

        activity?.applicationContext?.let { appContext ->
            setList(
                root.findViewById(R.id.about_list),
                appContext.getString(R.string.about_label),
                AboutListAdapter(
                    listOf(
                        Pair(
                            appContext.getString(R.string.twitter_label),
                            appContext.getString(R.string.twitter_link)
                        ),
                        Pair(
                            appContext.getString(R.string.instagram_label),
                            appContext.getString(R.string.instagram_link)
                        ),
                        Pair(
                            appContext.getString(R.string.facebook_label),
                            appContext.getString(R.string.facebook_link)
                        ),
                        Pair(
                            appContext.getString(R.string.linkedin_label),
                            appContext.getString(R.string.linkedin_link)
                        ),
                        Pair(appContext.getString(R.string.team_label), "")
                    )
                )
            )

            setList(
                root.findViewById(R.id.legal_list),
                appContext.getString(R.string.legal_label),
                AboutListAdapter(
                    listOf(
                        Pair(appContext.getString(R.string.terms_label),
                            appContext.getString(R.string.terms_link)
                        ),
                        Pair(appContext.getString(R.string.privacy_label),
                            appContext.getString(R.string.privacy_link)
                        )
                    )
                )
            )
        }

        return root
    }

    private fun setList(layout: LinearLayout, heading: String, adapter: AboutListAdapter) {
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

        private inner class AboutItemHolder(view: View): RecyclerView.ViewHolder(view) {
            val aboutItem: RelativeLayout = view.findViewById(R.id.about_item)
            val aboutText: TextView = view.findViewById(R.id.about_text)
            val aboutIcon: ImageView = view.findViewById(R.id.about_icon)
            val topDivider: View = view.findViewById(R.id.top_divider)

            fun bind(item: Pair<String, String>, isFirst: Boolean) {
                val (label, link) = item

                aboutText.text = label
                aboutItem.setOnClickListener {
                    if (item.second != "") {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link)))
                    }
                    else {
                        findNavController().navigate(AboutFragmentDirections.aboutToCredits())
                    }
                }

                activity?.applicationContext?.let { appContext ->
                    when (label) {
                        appContext.getString(R.string.twitter_label) ->
                            aboutIcon.setImageResource(R.mipmap.twitter_icon)

                        appContext.getString(R.string.instagram_label) ->
                            aboutIcon.setImageResource(R.mipmap.instagram_icon)

                        appContext.getString(R.string.facebook_label) ->
                            aboutIcon.setImageResource(R.mipmap.facebook_icon)

                        appContext.getString(R.string.linkedin_label) ->
                            aboutIcon.setImageResource(R.mipmap.linkedin_icon)

                        appContext.getString(R.string.team_label) ->
                            aboutIcon.setImageResource(R.mipmap.team_icon)

                        appContext.getString(R.string.terms_label) ->
                            aboutIcon.setImageResource(R.mipmap.terms_icon)

                        appContext.getString(R.string.privacy_label) ->
                            aboutIcon.setImageResource(R.mipmap.privacy_icon)
                    }
                }
                if (isFirst) {
                    topDivider.visibility = View.VISIBLE
                }
            }
        }
    }

}