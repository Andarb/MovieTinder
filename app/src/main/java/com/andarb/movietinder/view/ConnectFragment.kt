package com.andarb.movietinder.view

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.EditText
import android.widget.NumberPicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.FragmentConnectBinding
import com.andarb.movietinder.model.Endpoint
import com.andarb.movietinder.model.remote.NearbyClient
import com.andarb.movietinder.model.repository.UserPreferences
import com.andarb.movietinder.view.adapters.EndpointAdapter
import com.andarb.movietinder.viewmodel.MainViewModel
import kotlinx.coroutines.launch


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
    private var deviceNameHint: String = ""
    private var movieCountHint: Int = 0
    private val sharedViewModel: MainViewModel by activityViewModels()
    private val endpointClickListener: (Endpoint) -> Unit = { endpoint: Endpoint ->
        nearbyClient.connect(endpoint.id)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        application = requireActivity().application
        binding = FragmentConnectBinding.inflate(inflater, container, false)

        adapter = EndpointAdapter(endpointClickListener)
        binding.tvErrorPermissions.visibility = View.INVISIBLE
        binding.recyclerviewConnect.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewConnect.adapter = adapter
        binding.tvDeviceName.setOnClickListener { changeDeviceName() }
        binding.tvMovieCount.setOnClickListener { changeMovieCount() }

        createMenu()

        sharedViewModel.nearbyDevices.observe(viewLifecycleOwner) { nearbyDevices ->
            adapter.items = nearbyDevices.endpoints.toMutableList()
            if (!nearbyDevices.connectedId.isNullOrBlank()) {
                // Once connected to a 'Nearby' device proceed to movie selection
                findNavController().navigate(R.id.action_connectFragment_to_selectionFragment)
            }
        }

        checkPermissions()

        return binding.root
    }


    /** Confirm or request required permissions */
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Request necessary runtime permissions
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                // Confirm all permissions were granted
                val permissionsGranted = !permissions.values.contains(false)

                if (permissionsGranted) scanForDevices() else showPermissionsRationale()
            }

            var permissionArray = arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            if (Build.VERSION.SDK_INT >= 33) permissionArray += Manifest.permission.NEARBY_WIFI_DEVICES

            requestPermissionLauncher.launch(permissionArray)
        } else {
            scanForDevices()
        }
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
        nearbyClient = sharedViewModel.nearbyClient

        lifecycleScope.launch {
            sharedViewModel.userPreferencesFlow.collect { preferences: UserPreferences ->
                nearbyClient.apply {
                    deviceName = preferences.deviceName
                    connections.stopAdvertising()
                    startAdvertising()
                }

                binding.tvDeviceName.text = nearbyClient.deviceName
                deviceNameHint = nearbyClient.deviceName

                movieCountHint = preferences.movieCount
                binding.tvMovieCount.text = movieCountHint.toString()
            }
        }

        nearbyClient.startDiscovery()
    }

    // Prompts to change the name of this device as seen by others
    private fun changeDeviceName() {
        val userInput = EditText(activity)
        userInput.hint = deviceNameHint

        AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_device_name_title)
            .setView(userInput)
            .setPositiveButton(R.string.dialog_confirm) { dialog, whichButton ->
                val name = userInput.text.toString()
                binding.tvDeviceName.text = name
                sharedViewModel.setDeviceName(name)
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, whichButton -> }
            .show()
    }

    // Prompts to change the number of movies to choose from
    private fun changeMovieCount() {
        val userInput = NumberPicker(activity)
        userInput.apply {
            wrapSelectorWheel = true
            minValue = 2
            maxValue = 20
            value = movieCountHint
        }

        AlertDialog.Builder(activity)
            .setTitle(R.string.dialog_movie_count_title)
            .setView(userInput)
            .setPositiveButton(R.string.dialog_confirm) { dialog, whichButton ->
                val count = userInput.value
                binding.tvMovieCount.text = count.toString()
                sharedViewModel.setMovieCount(count)
            }
            .setNegativeButton(R.string.dialog_cancel) { dialog, whichButton -> }
            .show()
    }

    override fun onStart() {
        // Clear the old list of found/connected devices and start discovery for new ones
        sharedViewModel.nearbyDevices.value?.endpoints?.clear()
        sharedViewModel.nearbyDevices.value?.connectedId = null
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
                        sharedViewModel.nearbyDevices.value?.endpoints = mutableListOf()
                        if (::nearbyClient.isInitialized) {
                            nearbyClient.startAdvertising()
                            nearbyClient.startDiscovery()
                        }
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}