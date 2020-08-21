package com.okomilabs.dailychallenges.fragments

import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.transition.Slide
import android.util.TypedValue
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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

        observeList(listView, root.findViewById(R.id.completed_list_message))
        resetButtonFunctionality(root.findViewById(R.id.reset_button))

        enterTransition = Slide(Gravity.END).setInterpolator(LinearOutSlowInInterpolator())
        exitTransition = Slide(Gravity.START).setInterpolator(LinearOutSlowInInterpolator())

        return root
    }

    private fun resetButtonFunctionality(reset: Button) {
        reset.setOnClickListener {
            showResetDialog1()
        }
    }

    private fun showResetDialog1() {
        val appContext = activity?.applicationContext

        if (appContext != null) {
            val builder: AlertDialog.Builder = buildDialog(
                appContext.getString(R.string.reset_title_1),
                appContext.getString(R.string.reset_message_1)
            )

            builder
                .setPositiveButton(appContext.getString(R.string.yes_label)) { _, _ ->
                    showResetDialog2()
                }

            showAlert(builder)
        }
    }

    private fun showResetDialog2() {
        val appContext = activity?.applicationContext

        if (appContext != null) {
            val builder: AlertDialog.Builder = buildDialog(
                appContext.getString(R.string.reset_title_2),
                appContext.getString(R.string.reset_message_2)
            )

            builder
                .setPositiveButton(appContext.getString(R.string.yes_label)) { _, _ ->
                    cListViewModel.resetData()
                    findNavController().navigate(
                        ChallengeListFragmentDirections.challengeListToWelcome()
                    )
                }

            showAlert(builder)
        }
    }

    private fun observeList(listView: RecyclerView, message: TextView) {
        val listObserver = Observer<List<ChallengeListItem>> { newList ->
            if (!newList.isNullOrEmpty()) {
                listView.adapter = ChallengeListAdapter(newList)
                listView.visibility = View.VISIBLE
                message.visibility = View.GONE
            }
            else {
                listView.visibility = View.GONE
                message.visibility = View.VISIBLE
            }
        }

        cListViewModel.cList.observe(viewLifecycleOwner, listObserver)
    }

    private fun buildDialog(title: String, message: String): AlertDialog.Builder {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val appContext = activity?.applicationContext

        if (appContext != null) {
            builder.setCustomTitle(
                createDialogTitle(title)
            )
            builder
                .setMessage(message)
                .setNeutralButton(appContext.getString(R.string.no_label)) { _, _ -> }
        }

        return builder
    }

    private fun showAlert(builder: AlertDialog.Builder) {
        val alert = builder.create()
        alert.show()
        setDialogFont(alert)
    }

    private fun createDialogTitle(text: String): TextView {
        val appContext = activity?.applicationContext

        val paddingVal: Int = (resources.displayMetrics.density * 22f).toInt()
        val title = TextView(appContext)
        title.setPadding(paddingVal, paddingVal, paddingVal, 0)

        title.text = text
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)

        if (appContext != null) {
            title.setTextColor(ContextCompat.getColor(appContext, android.R.color.black))
            title.typeface = ResourcesCompat.getFont(appContext, R.font.asap)
        }

        return title
    }

    private fun setDialogFont(alert: AlertDialog) {
        val window: Window? = alert.window
        val appContext = activity?.applicationContext

        if (appContext != null) {
            val messageFont: Typeface? = ResourcesCompat.getFont(appContext, R.font.timeless)
            val buttonFont: Typeface? = ResourcesCompat.getFont(appContext, R.font.asap_bold)

            if (window != null) {
                window.findViewById<TextView>(android.R.id.message).typeface = messageFont
                window.findViewById<TextView>(android.R.id.button1).typeface = buttonFont

                val buttonNo: TextView = window.findViewById(android.R.id.button3)
                buttonNo.typeface = buttonFont
                buttonNo.setTextColor(ResourcesCompat.getColor(
                    resources, android.R.color.holo_red_light, null
                ))
            }
        }
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
            holder.bind(challengeItems[position], position == 0)
        }

        private inner class ChallengeHolder(view: View): RecyclerView.ViewHolder(view) {
            val challengeItem: CardView = view.findViewById(R.id.challenge_item)
            val title: TextView = view.findViewById(R.id.item_title)
            val category: TextView = view.findViewById(R.id.item_category)
            val lastCompleted: TextView = view.findViewById(R.id.item_last_completed)
            val totalCompleted: TextView = view.findViewById(R.id.item_total_completed)

            fun bind(item: ChallengeListItem, isFirst: Boolean) {
                title.text = item.title
                category.text = item.category
                lastCompleted.text = item.lastCompleted
                totalCompleted.text = item.totalCompleted.toString()

                challengeItem.setOnClickListener {
                    findNavController().navigate(
                        ChallengeListFragmentDirections.challengeListToReadMore(item.id)
                    )
                }

                if (isFirst) {
                    val params: ViewGroup.MarginLayoutParams =
                        challengeItem.layoutParams as ViewGroup.MarginLayoutParams

                    params.topMargin = (resources.displayMetrics.density * 10f).toInt()
                    challengeItem.layoutParams = params
                }
            }
        }
    }

}