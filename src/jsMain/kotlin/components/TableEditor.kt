package components

import com.benasher44.uuid.Uuid
import mui.icons.material.Cancel
import mui.icons.material.Save
import mui.material.*
import mui.system.responsive
import org.codecranachan.roster.Table
import org.codecranachan.roster.TableDetails
import org.codecranachan.roster.TableLanguage
import org.w3c.dom.HTMLInputElement
import react.*
import react.dom.events.ChangeEvent
import react.dom.html.InputType
import react.dom.onChange
import reducers.EditorClosed
import reducers.StoreContext
import reducers.updateTableDetails

object Boundaries {
    const val MIN_PLAYERS = 2
    const val MAX_PLAYERS = 12
    const val MIN_LEVEL = 1
    const val MAX_LEVEL = 20
}

val TableEditor = FC<Props> {
    val store = useContext(StoreContext)
    val (isOpen, setIsOpen) = useState(false)

    val (tableId, setTableId) = useState(null as Uuid?)
    val (tableName, setTableName) = useState("Unknown Table")
    val (title, setTitle) = useState("")
    val (description, setDescription) = useState("")
    val (designation, setDesignation) = useState("")
    val (language, setLanguage) = useState(TableLanguage.SwissGerman)
    val (minPlayers, setMinPlayers) = useState(3)
    val (maxPlayers, setMaxPlayers) = useState(7)
    val (minLevel, setMinLevel) = useState(1)
    val (maxLevel, setMaxLevel) = useState(4)

    fun updateTable(table: Table) {
        setTableId(table.id)
        setTableName(table.getName())
        setTitle(table.details.adventureTitle ?: "")
        setDescription(table.details.adventureDescription ?: "")
        setDesignation(table.details.moduleDesignation ?: "")
        setLanguage(table.details.language)
        setMinPlayers(table.details.playerRange.first)
        setMaxPlayers(table.details.playerRange.last)
        setMinLevel(table.details.levelRange.first)
        setMaxLevel(table.details.levelRange.last)
    }

    useEffectOnce {
        val unsubscribe = store.subscribe {
            val t = store.state.ui.editorTarget
            if (t is Table) {
                setIsOpen(true)
                updateTable(t)
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
            +tableName
        }

        DialogContent {
            DialogContentText {
                +"Edit the details of your hosted table below."
            }
            Stack {
                direction = responsive(StackDirection.column)
                TextField {
                    margin = FormControlMargin.dense
                    fullWidth = true
                    label = ReactNode("Adventure Title")
                    placeholder = "Title of the adventure"
                    value = title
                    onChange = {
                        val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                        setTitle(e.target.value)
                    }
                }
                TextField {
                    margin = FormControlMargin.dense
                    fullWidth = true
                    label = ReactNode("Adventure Description")
                    multiline = true
                    value = description
                    placeholder = "Describe the adventure"
                    onChange = {
                        val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                        setDescription(e.target.value)
                    }
                }
                TextField {
                    margin = FormControlMargin.dense
                    fullWidth = true
                    label = ReactNode("AL Designation")
                    placeholder = "Official AL module designation"
                    value = designation
                    onChange = {
                        val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                        setDesignation(e.target.value)
                    }
                    helperText = ReactNode("Leave empty for homebrew adventures")
                }
                TextField {
                    margin = FormControlMargin.dense
                    fullWidth = true
                    label = ReactNode("Language")
                    select = true
                    value = language.name
                    onChange = {
                        val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                        setLanguage(TableLanguage.valueOf(e.target.value))
                    }
                    TableLanguage.values().forEach {
                        MenuItem {
                            key = it.name
                            value = it.name
                            +"${it.flag} ${it.name}"
                        }
                    }
                }
                Stack {
                    direction = responsive(StackDirection.row)
                    TextField {
                        margin = FormControlMargin.dense
                        label = ReactNode("Min Players")
                        value = minPlayers
                        type = InputType.number
                        onChange = {
                            val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                            val v = minOf(maxOf(e.target.value.toInt(), Boundaries.MIN_PLAYERS), maxPlayers)
                            setMinPlayers(v)
                        }
                    }
                    TextField {
                        margin = FormControlMargin.dense
                        label = ReactNode("Max Players")
                        value = maxPlayers
                        type = InputType.number
                        onChange = {
                            val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                            val v = minOf(maxOf(e.target.value.toInt(), minPlayers), Boundaries.MAX_PLAYERS)
                            setMaxPlayers(v)
                        }
                    }
                }
                Stack {
                    direction = responsive(StackDirection.row)
                    TextField {
                        margin = FormControlMargin.dense
                        label = ReactNode("Min Level")
                        value = minLevel
                        type = InputType.number
                        onChange = {
                            val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                            val v = minOf(maxOf(e.target.value.toInt(), Boundaries.MIN_LEVEL), maxLevel)
                            setMinLevel(v)
                        }
                    }
                    TextField {
                        margin = FormControlMargin.dense
                        label = ReactNode("Max Level")
                        value = maxLevel
                        type = InputType.number
                        onChange = {
                            val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                            val v = minOf(maxOf(e.target.value.toInt(), minLevel), Boundaries.MAX_LEVEL)
                            setMaxLevel(v)
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
                        updateTableDetails(
                            tableId!!,
                            TableDetails(
                                title,
                                description,
                                designation,
                                language,
                                minPlayers..maxPlayers,
                                minLevel..maxLevel
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
