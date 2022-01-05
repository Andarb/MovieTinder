package com.andarb.movietinder.model

import com.andarb.movietinder.util.DiffutilComparison

/**
 * Information about a discovered nearby device
 */
data class Endpoint(override val id: String, val name: String, var isConnected: Boolean = false) :
    DiffutilComparison
