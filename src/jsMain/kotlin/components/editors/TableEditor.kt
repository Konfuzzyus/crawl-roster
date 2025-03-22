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
import org.codecranachan.roster.core.Audience
import org.codecranachan.roster.core.Table
import org.codecranachan.roster.core.TableLanguage
import web.html.HTMLInputElement
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.events.ChangeEvent
import react.dom.onChange
import react.use
import react.useEffectOnceWithCleanup
import react.useState
import reducers.EditorClosed
import reducers.StoreContext
import reducers.updateTableDetails
import web.html.InputType

object Boundaries {
    const val MIN_PLAYERS = 2
    const val MAX_PLAYERS = 12
    const val MIN_LEVEL = 1
    const val MAX_LEVEL = 20
}

val TableEditor = FC<Props> {
    val store = use(StoreContext)!!
    val (isOpen, setIsOpen) = useState(false)

    val (eventId, setEventId) = useState(null as Uuid?)
    val (dmId, setDmId) = useState(null as Uuid?)
    val (title, setTitle) = useState("")
    val (description, setDescription) = useState("")
    val (designation, setDesignation) = useState("")
    val (language, setLanguage) = useState(TableLanguage.SwissGerman)
    val (minPlayers, setMinPlayers) = useState("3")
    val (maxPlayers, setMaxPlayers) = useState("7")
    val (minLevel, setMinLevel) = useState("1")
    val (maxLevel, setMaxLevel) = useState("4")
    val (gameSystem, setGameSystem) = useState("")
    val (audience, setAudience) = useState(Audience.Regular)

    fun updateTable(table: Table) {
        setEventId(table.eventId)
        setDmId(table.dungeonMasterId)
        setTitle(table.details.adventureTitle ?: "")
        setDescription(table.details.adventureDescription ?: "")
        setDesignation(table.details.moduleDesignation ?: "")
        setLanguage(table.details.language)
        setMinPlayers(table.details.playerRange.first.toString())
        setMaxPlayers(table.details.playerRange.last.toString())
        setMinLevel(table.details.levelRange.first.toString())
        setMaxLevel(table.details.levelRange.last.toString())
        setGameSystem(table.details.gameSystem ?: "")
        setAudience(table.details.audience)
    }

    useEffectOnceWithCleanup {
        val unsubscribe = store.subscribe {
            val t = store.state.ui.editorTarget
            if (t is Table) {
                setIsOpen(true)
                updateTable(t)
            } else {
                setIsOpen(false)
            }
        }
        onCleanup(unsubscribe)
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
                    label = ReactNode("Game System")
                    value = gameSystem
                    placeholder = "The game system used"
                    onChange = {
                        val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                        setGameSystem(e.target.value)
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
                    TableLanguage.entries.forEach {
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
                            setMinPlayers(e.target.value)
                        }
                    }
                    TextField {
                        margin = FormControlMargin.dense
                        label = ReactNode("Max Players")
                        value = maxPlayers
                        type = InputType.number
                        onChange = {
                            val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                            setMaxPlayers(e.target.value)
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
                            setMinLevel(e.target.value)
                        }
                    }
                    TextField {
                        margin = FormControlMargin.dense
                        label = ReactNode("Max Level")
                        value = maxLevel
                        type = InputType.number
                        onChange = {
                            val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                            setMaxLevel(e.target.value)
                        }
                    }
                }
                TextField {
                    margin = FormControlMargin.dense
                    label = ReactNode("Audience")
                    value = audience.name
                    select = true
                    onChange = {
                        val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                        setAudience(Audience.valueOf(e.target.value))
                    }
                    Audience.entries.forEach {
                        MenuItem {
                            key = it.name
                            value = it.name
                            +it.name
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
                    val truncatedMaxPlayers = toIntInRange(maxPlayers, Boundaries.MIN_PLAYERS..Boundaries.MAX_PLAYERS)
                    val truncatedMinPlayers = toIntInRange(minPlayers, Boundaries.MIN_PLAYERS..truncatedMaxPlayers)
                    val truncatedMaxLevel = toIntInRange(maxLevel, Boundaries.MIN_LEVEL..Boundaries.MAX_LEVEL)
                    val truncatedMinLevel = toIntInRange(minLevel, Boundaries.MIN_LEVEL..truncatedMaxLevel)

                    store.dispatch(
                        updateTableDetails(
                            eventId!!,
                            dmId!!,
                            Table.Details(
                                title,
                                description,
                                designation,
                                language,
                                truncatedMinPlayers..truncatedMaxPlayers,
                                truncatedMinLevel..truncatedMaxLevel,
                                audience,
                                gameSystem
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
