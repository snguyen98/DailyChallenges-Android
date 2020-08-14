package com.okomilabs.dailychallenges.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.data.entities.ChallengeListItem
import com.okomilabs.dailychallenges.viewmodels.ChallengeListViewModel

class ChallengeListFragment: Fragment() {
    private lateinit var cListViewModel: ChallengeListViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        cListViewModel = ViewModelProvider(this).get(ChallengeListViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_challenge_list, container, false)

        val listView: RecyclerView = root.findViewById(R.id.challenge_list)

        listView.layoutManager = LinearLayoutManager(
            activity?.applicationContext,
            RecyclerView.VERTICAL,
            false
        )

        observeList(listView)

        return root
    }

    private fun observeList(listView: RecyclerView) {
        val listObserver = Observer<List<ChallengeListItem>> { newList ->
            listView.adapter = ChallengeListAdapter(newList)
        }

        cListViewModel.cList.observe(viewLifecycleOwner, listObserver)
    }

    private inner class ChallengeListAdapter(
        private val challengeItems: List<ChallengeListItem>
    ): RecyclerView.Adapter<ChallengeListAdapter.ChallengeHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeHolder {
            return ChallengeHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.challenge_list_item, parent, false
                )
            )
        }

        override fun getItemCount(): Int = challengeItems.size

        override fun onBindViewHolder(holder: ChallengeHolder, position: Int) {
            holder.bind(challengeItems[position])
        }

        private inner class ChallengeHolder(view: View): RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.item_title)
            val category: TextView = view.findViewById(R.id.item_category)
            val lastCompleted: TextView = view.findViewById(R.id.item_last_completed)
            val totalCompleted: TextView = view.findViewById(R.id.item_total_completed)

            fun bind(item: ChallengeListItem) {
                title.text = item.title
                category.text = item.category
                lastCompleted.text = item.lastCompleted
                totalCompleted.text = item.totalCompleted.toString()
            }
        }
    }

}