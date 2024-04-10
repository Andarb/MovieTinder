package com.andarb.movietinder.model.remote

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import com.andarb.movietinder.R
import com.andarb.movietinder.model.Endpoint
import com.andarb.movietinder.model.Endpoints
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.model.local.NotificationManagement
import com.andarb.movietinder.util.addElement
import com.andarb.movietinder.util.markConnected
import com.andarb.movietinder.util.removeElement
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.serialization.json.Json

/**
 * Handles advertising, discovery and connecting between 'Nearby' devices.
 */
class NearbyClient(
    private val application: Application,
    private val nearbyMovieIDs: MutableLiveData<List<Int>>,
    private val nearbyMovies: MutableLiveData<List<Movie>>,
    private var endpoints: MutableLiveData<Endpoints>
) {
    private val strategy = Strategy.P2P_POINT_TO_POINT
    val connections = Nearby.getConnectionsClient(application.applicationContext)
    var localDeviceName = application.getString(R.string.error_insufficient_permissions_devicename)
    private val disconnectedToast: Toast = Toast.makeText(
        application,
        "",
        Toast.LENGTH_LONG
    )


    /** Establishes a connection to an endpoint */
    fun connect(endpointId: String) {
        connections
            .requestConnection(localDeviceName, endpointId, connectionLifecycleCallback)
            .addOnSuccessListener { _: Void? ->
                // We successfully requested a connection. Now both sides
                // must accept before the connection is established.
            }
            .addOnFailureListener { e: java.lang.Exception? ->
                // Nearby Connections failed to request the connection.
            }
    }

    /** Lets other devices know you are available for connection */
    fun startAdvertising() {
        val advertisingOptions = AdvertisingOptions.Builder().setStrategy(strategy).build()
        connections.startAdvertising(
            localDeviceName,
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
    fun startDiscovery() {
        val discoveryOptions = DiscoveryOptions.Builder().setStrategy(strategy).build()
        connections.startDiscovery(
            application.packageName,
            endpointDiscoveryCallback,
            discoveryOptions
        )
            .addOnSuccessListener { _: Void? ->
            }
            .addOnFailureListener { e: java.lang.Exception? ->
            }
    }

    /** Adds or removes found/lost devices from the adapter list */
    private val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                endpoints.addElement(Endpoint(endpointId, info.endpointName))
            }

            override fun onEndpointLost(endpointId: String) {
                // A previously discovered endpoint has gone away.
                endpoints.removeElement(endpointId)
            }
        }


    /** Establishes a connection to a remote device and sends of movie data */
    private val connectionLifecycleCallback: ConnectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
                // Automatically accept the connection on both sides.
                RemoteEndpoint.hasInitiatedConnection = connectionInfo.isIncomingConnection
                connections.acceptConnection(endpointId, payloadCallback)
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                when (result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        // We're connected! Can now start sending and receiving data.
                        endpoints.markConnected(endpointId)
                        connections.stopDiscovery()
                        connections.stopAdvertising()
                        disconnectedToast.setText(
                            application.getString(
                                R.string.toast_disconnected_endpoint,
                                RemoteEndpoint.deviceName
                            )
                        )
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
                disconnectedToast.show()
                RemoteEndpoint.reset()

                val notificationManagement = NotificationManagement(application)
                notificationManagement.cancel()
            }
        }


    /** Handles received data from a remote device */
    private val payloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // This always gets the full data of the payload. Will be null if it's not a BYTES
            // payload. You can check the payload type with payload.getType().
            val payloadBytes = payload.asBytes()

            if (payloadBytes != null) {
                val stringPayload: String = payloadBytes.decodeToString()

                when (stringPayload[0]) {
                    'i' -> {  // remote device sent their chosen movies
                        nearbyMovieIDs.value =
                            stringPayload.substring(1).split(",").map { it.toInt() }

                        Toast.makeText(
                            application,
                            application.getString(
                                R.string.toast_selection_received,
                                RemoteEndpoint.deviceName
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    'm' -> nearbyMovies.value =
                        Json.decodeFromString(stringPayload.substring(1)) // host shared downloaded movies
                    else -> nearbyMovieIDs.value = emptyList()
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // Bytes payloads are sent as a single chunk, so you'll receive a SUCCESS update immediately
            // after the call to onPayloadReceived().
        }
    }
}

/**
 * Keeps Endpoint connection state and relevant information
 */
object RemoteEndpoint {
    var deviceName = ""
    var deviceId = ""
    var isConnected = false
    var hasInitiatedConnection = false
    var hasReceivedMatches = false

    fun reset() {
        deviceName = ""
        deviceId = ""
        isConnected = false
        hasInitiatedConnection = false
    }
}