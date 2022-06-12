package components.editors

import mui.icons.material.Cancel
import mui.icons.material.Save
import mui.material.Button
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.FormControlMargin
import mui.material.Stack
import mui.material.StackDirection
import mui.material.TextField
import mui.system.responsive
import org.codecranachan.roster.Player
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.events.ChangeEvent
import react.dom.onChange
import react.useContext
import react.useEffectOnce
import react.useState
import reducers.EditorClosed
import reducers.StoreContext

val PlayerEditor = FC<Props> {
    val store = useContext(StoreContext)
    val (isOpen, setIsOpen) = useState(false)

    val (name, setName) = useState("Anonymous")

    fun setPlayer(player: Player) {
    }

    useEffectOnce {
        val unsubscribe = store.subscribe {
            val p = store.state.ui.editorTarget
            if (p is Player) {
                setIsOpen(true)
                setPlayer(p)
            } else {
                setIsOpen(false)
            }
        }
        cleanup(unsubscribe)
    }

    Dialog {
        open = isOpen
        onClose = { _, _ ->
            store.dispatch(EditorClosed())
        }

        DialogTitle {
            +"Account Settings"
        }

        DialogContent {
            Stack {
                direction = responsive(StackDirection.column)
                TextField {
                    margin = FormControlMargin.dense
                    fullWidth = true
                    label = ReactNode("Player Name")
                    placeholder = "Your name"
                    value = name
                    onChange = {
                        val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                        setName(e.target.value)
                    }
                }
            }
        }
        DialogActions {
            Button {
                startIcon = Cancel.create()
                onClick = { _ ->
                    store.dispatch(EditorClosed())
                }
                +"Cancel"
            }
            Button {
                startIcon = Save.create()
                onClick = { _ ->
                    store.dispatch(EditorClosed())
                }
                +"Save Changes"
            }
        }
    }
}
