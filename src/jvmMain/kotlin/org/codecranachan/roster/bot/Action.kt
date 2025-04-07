package org.codecranachan.roster.bot

import com.benasher44.uuid.Uuid

enum class Action {
    RegisterPlayer,
    UnregisterPlayer,
    RegisterBeginner
}

class ActiveId(val action: Action, private vararg val params: Uuid) {

    companion object {
        fun fromCustomId(customId: String): ActiveId? {
            val parts = customId.split(":")
            return try {
                val action = Action.valueOf(parts[0])
                if (parts.size > 1) ActiveId(
                    action,
                    *(parts.subList(1, parts.size).map(Uuid::fromString).toTypedArray())
                ) else ActiveId(action)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    fun asCustomId(): String {
        return listOf(action.name, *params).joinToString(":")
    }

    fun getParam(index: Int) : Uuid? {
        if (index < params.size) {
            return params[index]
        } else {
            return null
        }
    }

}
