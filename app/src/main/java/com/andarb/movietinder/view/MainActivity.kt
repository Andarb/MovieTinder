package com.andarb.movietinder.view

import android.app.NotificationChannel
import android.app.NotificationManager
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

const val NOTIFICATION_ID = 753159

/**
 * Sets up bottom navigation and navigation controller
 */
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Notification channel
        val name = getString(R.string.notification_name)
        val descriptionText = getString(R.string.notification_description)
        val importance = NotificationManager.IMPORTANCE_LOW
        val mChannel = NotificationChannel(application.packageName, name, importance)
        mChannel.description = descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        // Setup NavigationController
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup ActionBar
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
}