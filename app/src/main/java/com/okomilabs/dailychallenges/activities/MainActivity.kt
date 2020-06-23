package com.okomilabs.dailychallenges.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.okomilabs.dailychallenges.R

class MainActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
//Back button function
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
