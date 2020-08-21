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

    override fun onDestroy() {
        activity?.viewModelStore?.clear()
        super.onDestroy()
    }


    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////// Observing Functions ///////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Observes changes to the challenge list in the view model and if so, shows and updates the
     * recycler view, otherwise shows the message instead
     *
     * @param listView The recycler view to contain the list of challenges
     * @param message The text view notifying the user that they have no completed challenges
     */
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


    ////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////// Reset Button Functionality ////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Sets the listener for the reset data button
     *
     * @param reset The reset data button
     */
    private fun resetButtonFunctionality(reset: Button) {
        reset.setOnClickListener {
            showResetDialog1()
        }
    }

    /**
     * Displays the first dialog asking if the user wants to reset their data
     */
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

    /**
     * Displays the second dialog confirming that the user wants to reset all their data
     */
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


    ////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// Dialog Helper Functions //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Displays the alert and changes the font of the text inside
     *
     * @param builder The dialog builder to be used to create the alert
     */
    private fun showAlert(builder: AlertDialog.Builder) {
        val alert = builder.create()
        alert.show()
        setDialogFont(alert)
    }

    /**
     * Creates and returns the initial dialog with the title and message
     *
     * @param title The title of the dialog
     * @param message The message displayed by the dialog
     * @return The initial dialog
     */
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

    /**
     * Creates the custom text view for the title in the dialog boxes
     *
     * @param text The title text of the dialog
     * @return The text view to be passes into the dialog builder
     */
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

    /**
     * Sets the fonts and text colour of the message and buttons
     *
     * @param alert The alert dialog containing the message and buttons
     */
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


    ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////// Inner  Classes //////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Class which manages the items to be displayed in the challenge list recycler view
     *
     * @param challengeItems The list of items to be displayed
     */
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

        /**
         * Class that holds and binds the information to each challenge item in the recycler view
         *
         * @param view The challenge item view
         */
        private inner class ChallengeHolder(view: View): RecyclerView.ViewHolder(view) {
            val challengeItem: CardView = view.findViewById(R.id.challenge_item)
            val title: TextView = view.findViewById(R.id.item_title)
            val category: TextView = view.findViewById(R.id.item_category)
            val lastCompleted: TextView = view.findViewById(R.id.item_last_completed)
            val totalCompleted: TextView = view.findViewById(R.id.item_total_completed)

            /**
             * Assigns the text to text views, sets appropriate margins and sets a listener to
             * navigate to the read more page of the appropriate challenge
             *
             * @param item The data class containing the challenge item information
             * @param isFirst Boolean stating if the item is the first in the list
             */
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