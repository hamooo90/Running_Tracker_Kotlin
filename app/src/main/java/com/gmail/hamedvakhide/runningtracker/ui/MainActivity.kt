package com.gmail.hamedvakhide.runningtracker.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.gmail.hamedvakhide.runningtracker.R
import com.gmail.hamedvakhide.runningtracker.databinding.ActivityMainBinding
import com.gmail.hamedvakhide.runningtracker.ui.fragments.*
import com.gmail.hamedvakhide.runningtracker.util.Constants
import com.gmail.hamedvakhide.runningtracker.util.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    private lateinit var naviHostFragment : NavHostFragment

    override fun onBackPressed() {
        // Override default back press
        val currentFragment = findNavController(R.id.nav_host_fragment).currentDestination

        when(currentFragment?.label.toString()){
            "fragment_run" -> {
                finish()
            }
            "fragment_statistics" -> {
                naviHostFragment.findNavController().navigate(R.id.action_start_runFragment)

            }
            "fragment_settings" -> {
                naviHostFragment.findNavController().navigate(R.id.action_start_runFragment)
            }
            else -> { super.onBackPressed()}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.rootView
        setContentView(view)

        navigateToTrackingFragmentIfNeeded(intent)

        naviHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        binding.apply {
            setSupportActionBar(toolbar)

            bottomNavigationView.setupWithNavController(naviHostFragment.findNavController())

            // on reselect bottom navigation do nothing
            bottomNavigationView.setOnItemReselectedListener {  }

            naviHostFragment.findNavController()
                .addOnDestinationChangedListener { _, destination, _ ->
                    // don't show bottom navigation in 2 fragments
                    when(destination.id){
                        R.id.settingsFragment, R.id.runFragment, R.id.statisticsFragment ->
                            bottomNavigationView.visibility = View.VISIBLE
                        else ->
                            bottomNavigationView.visibility = View.GONE

                    }
                }
        }


    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?){
        val naviHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT)
            naviHostFragment.findNavController().navigate(R.id.action_start_trackingFragment)
    }
}