package com.okomilabs.dailychallenges.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.material.navigation.NavigationView
import com.okomilabs.dailychallenges.R

class MainActivity: AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MobileAds.initialize(this)

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(applicationContext.getString(R.string.sang_oppo)))
                .build()
        )

        val navController = findNavController(R.id.nav_host_fragment)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        val drawer: NavigationView = findViewById(R.id.nav_view)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.welcome_fragment,
                R.id.challenge_fragment,
                R.id.challenge_list_fragment,
                R.id.help_fragment,
                R.id.about_fragment
            ),
            drawerLayout
        )

        drawer.setupWithNavController(navController)
        toolbar.setupWithNavController(navController, appBarConfiguration)

        drawer.setNavigationItemSelectedListener {
            drawerLayout.closeDrawer(GravityCompat.START)

            if (it.itemId != navController.currentDestination?.id) {
                navController.navigate(it.itemId)
                true
            }
            else {
                false
            }
        }
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

}
