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
            AppBarConfiguration(setOf(R.id.connectFragment, R.id.historyFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Setup BottomNavigation
        with(binding) {
            bottomNavigation.setupWithNavController(navController)

            bottomNavigation.setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.bottom_navigation_connect -> {
                        navController.navigate(R.id.connectFragment)
                        true
                    }
                    R.id.bottom_navigation_matches -> {
                        navController.navigate(R.id.historyFragment)
                        true
                    }
                    R.id.bottom_navigation_history -> {
                        navController.navigate(R.id.historyFragment)
                        true
                    }
                    else -> false
                }
            }

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.selectionFragment -> bottomNavigation.visibility = View.GONE
                    else -> bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}