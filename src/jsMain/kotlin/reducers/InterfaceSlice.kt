package reducers

import org.codecranachan.roster.GuildRoster
import org.codecranachan.roster.core.Event
import org.codecranachan.roster.core.Player
import org.codecranachan.roster.core.Table
import org.reduxkotlin.Reducer

data class InterfaceState(
    val editorTarget: Any? = null
)

data class ServerEditorOpened(val guildRoster: GuildRoster)
data class PlayerEditorOpened(val player: Player)
data class TableEditorOpened(val table: Table)
data class EventEditorOpened(val event: Event)
class EditorClosed

val interfaceReducer: Reducer<ApplicationState> = { s: ApplicationState, a: Any ->
    val old = s.ui
    val new = when (a) {
        is EditorClosed -> old.copy(editorTarget = null)
        is TableEditorOpened -> old.copy(editorTarget = a.table)
        is PlayerEditorOpened -> old.copy(editorTarget = a.player)
        is ServerEditorOpened -> old.copy(editorTarget = a.guildRoster)
        is EventEditorOpened -> old.copy(editorTarget = a.event)
        else -> old
    }
    s.copy(ui = new)
}
