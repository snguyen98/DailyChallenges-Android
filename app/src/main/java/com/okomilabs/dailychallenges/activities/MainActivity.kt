package com.okomilabs.dailychallenges.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.NavHostFragment
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

        val navHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

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
            RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(applicationContext.getString(R.string.sang_oppo)))
                .build()
        )
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
