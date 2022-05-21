package org.codecranachan.roster

import kotlinx.serialization.Serializable

@Serializable
data class Identity(
    val name: String,
    val profile: Player?,
    val eventRegistrations: List<Event>,
    val hostedTables: List<PlayTable>
)
