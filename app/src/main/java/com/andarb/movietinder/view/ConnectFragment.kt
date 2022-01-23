package com.andarb.movietinder.view

import android.Manifest
import android.app.AlertDialog
import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.andarb.movietinder.R
import com.andarb.movietinder.databinding.FragmentConnectBinding
import com.andarb.movietinder.model.Endpoint
import com.andarb.movietinder.model.repository.MovieRepository
import com.andarb.movietinder.util.markAsConnected
import com.andarb.movietinder.util.removeElement
import com.andarb.movietinder.view.adapters.EndpointAdapter
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Using 'Nearby API' advertises availability for connection.
 * Scans for nearby devices willing to connect.
 * Establishes connection and data exchange between connected devices.
 */
class ConnectFragment : Fragment() {
    private lateinit var repository: MovieRepository
    private lateinit var localMovieIds: List<Int>
    private lateinit var remoteMovieIds: List<Int>
    private val strategy = Strategy.P2P_POINT_TO_POINT
    private lateinit var connectionsClient: ConnectionsClient
    private lateinit var deviceName: String
    private val endpointClickListener: (Endpoint) -> Unit = { endpoint: Endpoint ->
        connectionsClient
            .requestConnection(deviceName, endpoint.id, connectionLifecycleCallback)
            .addOnSuccessListener { _: Void? ->
                // We successfully requested a connection. Now both sides
                // must accept before the connection is established.
            }
            .addOnFailureListener { e: java.lang.Exception? ->
                // Nearby Connections failed to request the connection.
            }

    }
    private val adapter = EndpointAdapter(endpointClickListener)
    private lateinit var binding: FragmentConnectBinding
    private lateinit var application: Application


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConnectBinding.inflate(inflater, container, false)

        setHasOptionsMenu(true)

        application = activity!!.application
        binding.recyclerviewConnect.layoutManager = LinearLayoutManager(application)
        binding.recyclerviewConnect.adapter = adapter

        // TODO Transfer to Viewmodel
        repository = MovieRepository(application)
        lifecycleScope.launch(Dispatchers.IO) {
            localMovieIds = repository.retrieveMovieIds()
            localMovieIds
        }

        checkPermissions()

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_connect, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                // TODO Stop then scan for devices again
                // Update the list of nearby devices or prompt for permissions
                if (this::connectionsClient.isInitialized) startDiscovery() else checkPermissions()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /** Confirm or request required permissions */
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Request necessary runtime permissions
            val requestPermissionLauncher =
                registerForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    // Confirm all permissions were granted
                    val permissionsGranted = !permissions.values.contains(false)

                    if (permissionsGranted) scanForDevices() else showPermissionsRationale()
                }

            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        } else {
            scanForDevices()
        }
    }

    /** Show the reason for requiring requested permissions */
    private fun showPermissionsRationale() {
        binding.tvErrorPermissions.visibility = View.VISIBLE
        val builder = AlertDialog.Builder(activity)

        with(builder)
        {
            setTitle(R.string.dialog_permissions_button_title)
            setMessage(R.string.dialog_permissions_button_message)
            setPositiveButton(
                R.string.dialog_permissions_button_settings
            ) { _, _ ->
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", application.packageName, null)
                )
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            setNegativeButton(R.string.dialog_permissions_button_cancel, null)
            show()
        }
    }

    /** Advertise and look for other devices that are advertising */
    private fun scanForDevices() {
        binding.tvErrorPermissions.visibility = View.INVISIBLE
        deviceName =
            (application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.name
        connectionsClient = Nearby.getConnectionsClient(application.applicationContext)

        startAdvertising()
        startDiscovery()
    }

    /** Lets other devices know you are available for connection */
    private fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startAdvertising(
            deviceName,
            application.packageName,
            connectionLifecycleCallback,
            advertisingOptions
        )
            .addOnSuccessListener { _: Void? ->
                // We're advertising!
            }
            .addOnFailureListener { e: Exception? ->
                // We were unable to start advertising.
            }
    }

    /** Looks for any devices available for connection */
    private fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
        connectionsClient.startDiscovery(
            application.packageName,
            endpointDiscoveryCallback,
            discoveryOptions
        )
            .addOnSuccessListener { _: Void? ->
            }
            .addOnFailureListener { e: java.lang.Exception? ->
                Log.d("ConnectFragment", e.toString())
            }
    }


    /** Adds or removes found/lost devices from the adapter list */
    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                adapter.items.add(Endpoint(endpointId, info.endpointName))
                adapter.notifyItemInserted(adapter.itemCount - 1)
            }

            override fun onEndpointLost(endpointId: String) {
                // A previously discovered endpoint has gone away.
                adapter.removeElement(endpointId)
            }
        }


    /** Establishes a connection to a remote device and sends of movie data */
    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                // Automatically accept the connection on both sides.
                connectionsClient.acceptConnection(endpointId, payloadCallback)
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        // We're connected! Can now start sending and receiving data.
                        adapter.markAsConnected(endpointId)
                        connectionsClient.stopDiscovery()
                        findNavController().navigate(R.id.selectionFragment)

                        // TODO Transfer to Selection Fragment
//                        val bytesPayload =
//                            Payload.fromBytes(localMovieIds.joinToString(",").toByteArray())
//                        connectionsClient.sendPayload(endpointId, bytesPayload)


                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        // The connection was rejected by one or both sides.
                    }
                    ConnectionsStatusCodes.STATUS_ERROR -> {
                        // The connection broke before it was able to be accepted.
                    }
                    ConnectionsStatusCodes.STATUS_ALREADY_CONNECTED_TO_ENDPOINT -> {
                        // The connection broke before it was able to be accepted.
                    }
                    else -> {
                        // Unknown status code
                    }
                }
            }

            override fun onDisconnected(endpointId: String) {
                // We've been disconnected from this endpoint. No more data can be sent or received
                adapter.removeElement(endpointId)
                startDiscovery()
            }
        }


    /** Handles received data from a remote device */
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // This always gets the full data of the payload. Will be null if it's not a BYTES
            // payload. You can check the payload type with payload.getType().

            // TODO Transfer to Selection Fragment
            val payloadBytes = payload.asBytes()

            if (payloadBytes != null) {
                remoteMovieIds = payloadBytes.decodeToString().split(",").map { it.toInt() }
                // TODO Utilize the matched movies
                val matchedMovies = localMovieIds.intersect(remoteMovieIds.toSet())
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Bytes payloads are sent as a single chunk, so you'll receive a SUCCESS update immediately
            // after the call to onPayloadReceived().
        }
    }

    override fun onStop() {
        super.onStop()

        // Stop resource-heavy discovery and clear up adapter
        if (this::connectionsClient.isInitialized) {
            connectionsClient.stopDiscovery()
            adapter.items.clear()
            adapter.notifyDataSetChanged()
        }
    }
}