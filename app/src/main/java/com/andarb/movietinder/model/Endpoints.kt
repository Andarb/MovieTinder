package com.andarb.movietinder.model

/**
 * A list of 'Nearby' devices
 */
class Endpoints(
    var endpoints: MutableList<Endpoint>,
    var connectedId: String? = null,
    var connectedName: String? = null
)