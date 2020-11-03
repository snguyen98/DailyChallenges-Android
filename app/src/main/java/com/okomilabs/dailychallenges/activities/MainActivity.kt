package com.okomilabs.dailychallenges.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE
import com.google.android.gms.ads.RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE
import com.google.android.material.navigation.NavigationView
import com.okomilabs.dailychallenges.R
import com.okomilabs.dailychallenges.helpers.ReminderReceiver
import java.util.*

class MainActivity: AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer)
        val drawer: NavigationView = findViewById(R.id.nav_view)

        // Sets up drawer and home fragments with app bar
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.welcome_fragment,
                R.id.challenge_fragment,
                R.id.challenge_list_fragment,
                R.id.help_fragment,
                R.id.about_fragment,
                R.id.first_launch_fragment
            ),
            drawerLayout
        )

        toolbar.setupWithNavController(navController, appBarConfiguration)
        drawer.setupWithNavController(navController)

        // Sets up drawer functionality
        drawer.setNavigationItemSelectedListener {
            if (it.itemId != navController.currentDestination?.id) {
                navController.navigate(it.itemId)
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }
            else {
                drawerLayout.closeDrawer(GravityCompat.START)
                false
            }
        }

        // Changes toolbar depending on fragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.first_launch_fragment ||
                destination.id == R.id.welcome_fragment
            ) {
                toolbar.visibility = View.GONE
            }
            else {
                toolbar.visibility = View.VISIBLE
            }
        }

        // AdMob setup
        MobileAds.initialize(this)

        MobileAds.setRequestConfiguration(
            MobileAds.getRequestConfiguration()
                .toBuilder()
                .setTestDeviceIds(
                    listOf(
                        getString(R.string.sang_oppo),
                        getString(R.string.ned_motorola),
                        getString(R.string.saif_pixel),
                        getString(R.string.ross_huawei)
                    )
                )
                .setTagForChildDirectedTreatment(TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setTagForUnderAgeOfConsent(TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
                .setMaxAdContentRating("G")
                .build()
        )

        setReminder()
    }

    /**
     * Closes drawer if open when back button is pressed otherwise normal back functionality
     */
    override fun onBackPressed() {
        val drawer: DrawerLayout = findViewById(R.id.drawer)

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun setReminder() {
        val calendar: Calendar = Calendar.getInstance()

        calendar.set(Calendar.HOUR, 19)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 3)

        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
            ContextWrapper(applicationContext),
            0,
            Intent(ContextWrapper(applicationContext), ReminderReceiver::class.java),
            0
        )

        alarmManager.cancel(pendingIntent)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent
        )
    }

}
