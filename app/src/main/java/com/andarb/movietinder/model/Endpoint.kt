package com.andarb.movietinder.model

import com.andarb.movietinder.util.DiffutilComparison

/**
 * Information about a discovered 'Nearby' device
 */
data class Endpoint(override val id: String, val name: String) : DiffutilComparison
