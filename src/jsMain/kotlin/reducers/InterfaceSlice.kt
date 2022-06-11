package reducers

import org.codecranachan.roster.Table
import org.reduxkotlin.Reducer

data class InterfaceState(
    val editorTarget: Any? = null
)

data class TableEditorOpened(val table: Table)
class EditorClosed

val interfaceReducer: Reducer<ApplicationState> = { s: ApplicationState, a: Any ->
    val old = s.ui
    val new = when (a) {
        is EditorClosed -> old.copy(editorTarget = null)
        is TableEditorOpened -> old.copy(editorTarget = a.table)
        else -> old
    }
    s.copy(ui = new)
}
