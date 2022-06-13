package components.editors

import mui.icons.material.Cancel
import mui.icons.material.Save
import mui.material.Button
import mui.material.Chip
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.FormControl
import mui.material.FormControlMargin
import mui.material.InputLabel
import mui.material.MenuItem
import mui.material.Select
import mui.material.Stack
import mui.material.StackDirection
import mui.material.TextField
import mui.system.responsive
import org.codecranachan.roster.Player
import org.codecranachan.roster.PlayerDetails
import org.codecranachan.roster.TableLanguage
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.events.ChangeEvent
import react.dom.onChange
import react.key
import react.useContext
import react.useEffectOnce
import react.useState
import reducers.EditorClosed
import reducers.StoreContext
import reducers.updatePlayerDetails

val PlayerEditor = FC<Props> {
    val store = useContext(StoreContext)
    var isOpen by useState(false)

    var name by useState("Anonymous")
    var languages by useState(arrayOf(TableLanguage.English))

    fun setPlayer(player: Player) {
        name = player.details.name
        languages = player.details.languages.toTypedArray()
    }

    useEffectOnce {
        val unsubscribe = store.subscribe {
            val p = store.state.ui.editorTarget
            if (p is Player) {
                isOpen = true
                setPlayer(p)
            } else {
                isOpen = false
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
                        name = e.target.value
                    }
                }

                FormControl {
                    margin = FormControlMargin.dense
                    fullWidth = true
                    InputLabel {
                        id = "language-preference-label"
                        +"Language Preference"
                    }
                    Select {
                        labelId = "language-preference-label"
                        label = ReactNode("Language Preferences")
                        multiple = true
                        value = languages.unsafeCast<Nothing?>()
                        onChange = { ev, _ ->
                            val e = ev.unsafeCast<ChangeEvent<HTMLInputElement>>()
                            languages = e.target.value.unsafeCast<Array<TableLanguage>>()
                        }
                        renderValue = { selected ->
                            val sel = selected.unsafeCast<Array<TableLanguage>>()
                            ReactNode(
                                arrayOf(Stack.create {
                                    direction = responsive(StackDirection.column)
                                    spacing = responsive(1)
                                    sel.map { Chip { label = ReactNode("${it.flag} ${it.name}") } }
                                })
                            )
                        }
                        TableLanguage.values().forEach {
                            MenuItem {
                                key = it.name
                                value = it.unsafeCast<Nothing?>()
                                +"${it.flag} ${it.name}"
                            }
                        }
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
                    store.dispatch(
                        updatePlayerDetails(
                            PlayerDetails(
                                name,
                                languages.asList()
                            )
                        )
                    )
                    store.dispatch(EditorClosed())
                }
                +"Save Changes"
            }
        }
    }
}
