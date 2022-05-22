package org.codecranachan.roster

import kotlinx.serialization.Serializable

@Serializable
data class Identity(
    val name: String,
    val profile: Player?
)
