package org.codecranachan.roster

@kotlinx.serialization.Serializable
data class UserIdentity (
    val name: String,
    val picture: String?,
    val given_name: String?,
    val locale: String?
        )