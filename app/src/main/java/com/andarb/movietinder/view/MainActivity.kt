package com.andarb.movietinder.view

import android.content.BroadcastReceiver
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.ActivityMainBinding
import com.andarb.movietinder.model.local.ConnectionBroadcastReceiver
import com.andarb.movietinder.model.local.NotificationManagement
import com.andarb.movietinder.model.remote.RemoteEndpoint


/**
 * Sets up bottom navigation, navigation controller and notifications
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private val broadcastReceiver: BroadcastReceiver = ConnectionBroadcastReceiver()
    private lateinit var notificationManagement: NotificationManagement

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Notification channel
        notificationManagement = NotificationManagement(application)
        notificationManagement.setupChannel()

        // Setup NavigationController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup Top App Bar
        setSupportActionBar(binding.actionBar)

        // Setup Bottom App Bar
        appBarConfiguration =
            AppBarConfiguration(
                setOf(
                    R.id.connectFragmentNav,
                    R.id.matchesFragmentNav,
                    R.id.historyFragmentNav
                )
            )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Setup BottomNavigation
        with(binding) {
            bottomNavigation.setupWithNavController(navController)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.selectionFragmentNav -> bottomNavigation.visibility = View.GONE
                    else -> bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()

        if (RemoteEndpoint.isConnected) {
            notificationManagement.send()
        }
    }

    override fun onStop() {
        super.onStop()

        if (RemoteEndpoint.isConnected) {
            notificationManagement.cancel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}