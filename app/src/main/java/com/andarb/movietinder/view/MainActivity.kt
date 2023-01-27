package com.andarb.movietinder.view

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