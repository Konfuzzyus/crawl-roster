package org.codecranachan.roster.core

enum class TableLanguage(val short: String, val flag: String) {
    SwissGerman("gsw", "\uD83C\uDDE8\uD83C\uDDED"),
    English("eng", "\uD83C\uDDEC\uD83C\uDDE7"),
    German("deu", "\uD83C\uDDE9\uD83C\uDDEA"),
    French("fra", "\uD83C\uDDEB\uD83C\uDDF7"),
    Italian("ita", "\uD83C\uDDEE\uD83C\uDDF9"),
    Romansh("roh", "\uD83C\uDDE8\uD83C\uDDED");

    companion object {
        private val shortMap = values().associateBy { it.short }
        fun ofShort(short: String): TableLanguage = shortMap[short] ?: throw IllegalArgumentException(short)
    }
}