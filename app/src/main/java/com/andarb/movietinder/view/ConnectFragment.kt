package com.andarb.movietinder.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.FragmentConnectBinding
import com.andarb.movietinder.model.Endpoint
import com.andarb.movietinder.model.remote.NearbyClient
import com.andarb.movietinder.view.adapters.EndpointAdapter
import com.andarb.movietinder.viewmodel.MainViewModel
import kotlin.random.Random


/**
 * Using 'Nearby API' advertises availability for connection.
 * Scans for nearby devices willing to connect.
 * Establishes connection and data exchange between connected devices.
 */
class ConnectFragment : Fragment() {
    private lateinit var nearbyClient: NearbyClient
    private lateinit var binding: FragmentConnectBinding
    private lateinit var application: Application
    private lateinit var adapter: EndpointAdapter
    private lateinit var preferences: SharedPreferences
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val endpointClickListener: (Endpoint) -> Unit = { endpoint: Endpoint ->
        showProgressbar(getString(R.string.progressbar_connecting))
        nearbyClient.connect(endpoint.id)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        application = requireActivity().application
        preferences = PreferenceManager.getDefaultSharedPreferences(application)
        binding = FragmentConnectBinding.inflate(inflater, container, false)

        adapter = EndpointAdapter(endpointClickListener)
        binding.tvErrorPermissions.visibility = View.INVISIBLE
        binding.recyclerviewConnect.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewConnect.adapter = adapter
        binding.tvDeviceName.setOnClickListener { findNavController().navigate(R.id.action_connectFragmentNav_to_settingsFragmentNav) }

        createMenu()

        sharedViewModel.nearbyDevices.observe(viewLifecycleOwner) { nearbyDevices ->
            binding.progressbarEndpoint.visibility = View.GONE
            adapter.items = nearbyDevices.endpoints.toMutableList()

            if (!nearbyDevices.connectedId.isNullOrBlank()) {
                Toast.makeText(
                    application,
                    getString(R.string.toast_connected_endpoint, nearbyDevices.connectedName),
                    Toast.LENGTH_SHORT
                ).show()
                // Once connected to a 'Nearby' device proceed to movie selection
                findNavController().navigate(R.id.action_connectFragment_to_selectionFragment)
            }
        }

        checkPermissions()

        return binding.root
    }


    /** Confirm or request required permissions */
    private fun checkPermissions() {
        // Request necessary runtime permissions
        var permissionArray: Array<String>
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Confirm all permissions were granted
            val permissionsGranted = !permissions.values.contains(false)

            if (permissionsGranted) scanForDevices() else showPermissionsRationale()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionArray = arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            if (Build.VERSION.SDK_INT >= 33) permissionArray += Manifest.permission.NEARBY_WIFI_DEVICES
        } else {
            permissionArray = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        requestPermissionLauncher.launch(permissionArray)
    }

    /** Show the reason for requiring requested permissions */
    private fun showPermissionsRationale() {
        binding.tvErrorPermissions.visibility = View.VISIBLE

        AlertDialog.Builder(activity).setTitle(R.string.dialog_permissions_title)
            .setMessage(R.string.dialog_permissions_message).setPositiveButton(
                R.string.dialog_permissions_button_settings
            ) { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", application.packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }.setNegativeButton(R.string.dialog_cancel, null).show()
    }

    /** Advertise and look for other devices that are advertising */
    @SuppressLint("MissingPermission")
    private fun scanForDevices() {
        showProgressbar(getString(R.string.progressbar_searching))
        nearbyClient = sharedViewModel.nearbyClient

        nearbyClient.apply {
            val defaultName =
                getString(R.string.default_device_name) + Random.nextInt(100, 999).toString()
            deviceName = preferences.getString(
                getString(R.string.preferences_device_name_key),
                defaultName
            ).toString()

            if (deviceName == defaultName) with(preferences.edit()) {
                putString(getString(R.string.preferences_device_name_key), defaultName)
                apply()
            }
            binding.tvDeviceName.text = deviceName

            connections.stopAdvertising()
            startAdvertising()
            startDiscovery()
        }
    }

    /** Clears all connections and found clients */
    private fun resetConnection() {
        adapter.items = mutableListOf()
        sharedViewModel.nearbyDevices.value?.connectedId = null
        sharedViewModel.nearbyDevices.value?.endpoints?.clear()

        if (::nearbyClient.isInitialized) nearbyClient.apply {
            showProgressbar(getString(R.string.progressbar_searching))
            connections.stopAdvertising()
            connections.stopDiscovery()
            connections.stopAllEndpoints()
            startAdvertising()
            startDiscovery()
        }
    }

    private fun showProgressbar(text: String) {
        binding.progressbarEndpointText.text = text
        binding.progressbarEndpoint.visibility = View.VISIBLE
    }

    override fun onStart() {
        // Clear the old list of found/connected devices and start discovery for new ones
        adapter.items = mutableListOf()

        sharedViewModel.apply {
            nearbyDevices.value?.endpoints?.clear()
            nearbyDevices.value?.connectedId = null
            nearbyClient.connections.stopAllEndpoints()
        }

        if (::nearbyClient.isInitialized) scanForDevices()

        super.onStart()
    }

    override fun onStop() {
        super.onStop()

        // Stop resource-heavy discovery/advertising
        if (::nearbyClient.isInitialized) {
            nearbyClient.connections.stopDiscovery()
            nearbyClient.connections.stopAdvertising()
        }
    }

    private fun createMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_connect, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_refresh -> {
                        val refreshIcon: View = requireActivity().findViewById(R.id.action_refresh)
                        val rotation =
                            AnimationUtils.loadAnimation(application, R.anim.rotation)

                        refreshIcon.startAnimation(rotation)
                        resetConnection()
                        true
                    }

                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_connectFragmentNav_to_settingsFragmentNav)
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}