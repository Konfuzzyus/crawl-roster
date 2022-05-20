package org.codecranachan.roster

@kotlinx.serialization.Serializable
data class UserIdentity(
    val id: String,
    val name: String,
    val pictureUrl: String?
)