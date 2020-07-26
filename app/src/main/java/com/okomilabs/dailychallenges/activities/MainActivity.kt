package com.okomilabs.dailychallenges.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.*
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
                .setTestDeviceIds(listOf("1805DBC7E2E5F4F767CC5BBD50879F8B"))
                .build()
        )

        val navController = findNavController(R.id.nav_host_fragment)
        val drawer: DrawerLayout = findViewById(R.id.drawer)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.challenge_fragment,
                R.id.help_fragment,
                R.id.about_fragment,
                R.id.credits_fragment
            ),
            drawer
        )

        findViewById<NavigationView>(R.id.nav_view).setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.nav_host_fragment)) ||
                super.onOptionsItemSelected(item)
    }

    /**
     * Back button functionality in action bar
     */
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    /**
     * Drop down menu functionality in action bar
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.drop_menu, menu)
        return true
    }

}
