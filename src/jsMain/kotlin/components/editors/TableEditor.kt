package components.editors

import com.benasher44.uuid.Uuid
import mui.icons.material.Cancel
import mui.icons.material.Save
import mui.material.Button
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.FormControlMargin
import mui.material.MenuItem
import mui.material.Stack
import mui.material.StackDirection
import mui.material.TextField
import mui.system.responsive
import org.codecranachan.roster.core.Table
import org.codecranachan.roster.core.TableLanguage
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.events.ChangeEvent
import react.dom.html.InputType
import react.dom.onChange
import react.key
import react.useContext
import react.useEffectOnce
import react.useState
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

    val (eventId, setEventId) = useState(null as Uuid?)
    val (dmId, setDmId) = useState(null as Uuid?)
    val (title, setTitle) = useState("")
    val (description, setDescription) = useState("")
    val (designation, setDesignation) = useState("")
    val (language, setLanguage) = useState(TableLanguage.SwissGerman)
    val (minPlayers, setMinPlayers) = useState(3)
    val (maxPlayers, setMaxPlayers) = useState(7)
    val (minLevel, setMinLevel) = useState(1)
    val (maxLevel, setMaxLevel) = useState(4)

    fun updateTable(table: Table) {
        setEventId(table.eventId)
        setDmId(table.dungeonMasterId)
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
            +"Your table"
        }

        DialogContent {
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
                            val v = toIntInRange(e.target.value, Boundaries.MIN_PLAYERS..maxPlayers)
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
                            val v = toIntInRange(e.target.value, minPlayers..Boundaries.MAX_PLAYERS)
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
                            val v = toIntInRange(e.target.value, Boundaries.MIN_LEVEL..maxLevel)
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
                            val v = toIntInRange(e.target.value, minLevel..Boundaries.MAX_LEVEL)
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
                            eventId!!,
                            dmId!!,
                            Table.Details(
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

private fun toIntInRange(
    value: String,
    range: IntRange
) = minOf(maxOf(value.toIntOrNull() ?: 1, range.first), range.last)
