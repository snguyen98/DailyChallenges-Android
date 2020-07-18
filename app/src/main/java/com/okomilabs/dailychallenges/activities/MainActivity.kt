package com.okomilabs.dailychallenges.activities

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.okomilabs.dailychallenges.R

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
