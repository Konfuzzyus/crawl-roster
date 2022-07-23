package components.editors

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import com.benasher44.uuid.uuidFrom
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
import org.codecranachan.roster.Character
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
    var importerIsOpen by useState(false)

    var playerId by useState(uuid4())
    var name by useState("Anonymous")
    var languages by useState(arrayOf(TableLanguage.English))
    var playTier by useState(0)
    var preferredCharacter by useState<Uuid?>(null)
    var characters by useState(emptyList<Character>())

    fun setPlayer(player: Player) {
        playerId = player.id
        name = player.details.name
        languages = player.details.languages.toTypedArray()
        playTier = player.details.playTier
        preferredCharacter = player.details.preferredCharacter
        characters = player.characters
    }

    useEffectOnce {
        val unsubscribe = store.subscribe {
            val p = store.state.ui.editorTarget
            if (p is Player) {
                isOpen = true
                setPlayer(p)
                characters = store.state.identity.player?.characters ?: emptyList()
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
                TextField {
                    margin = FormControlMargin.dense
                    fullWidth = true
                    label = ReactNode("Play Tier Preference")
                    select = true
                    value = playTier.toString()
                    onChange = {
                        val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                        playTier = e.target.value.toInt()
                    }
                    MenuItem {
                        key = "Tier_0"
                        value = "0"
                        +"Beginner"
                    }
                    (1..4).forEach {
                        MenuItem {
                            key = "Tier_$it"
                            value = "$it"
                            +"Tier $it"
                        }
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
                                key = it.short
                                value = it.unsafeCast<Nothing?>()
                                +"${it.flag} ${it.name}"
                            }
                        }
                    }
                }
                if (characters.isNotEmpty()) {
                    TextField {
                        margin = FormControlMargin.dense
                        required = false
                        fullWidth = true
                        label = ReactNode("Preferred Character")
                        select = true
                        value = preferredCharacter?.toString() ?: ""
                        onChange = {
                            val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                            preferredCharacter = uuidFrom(e.target.value)
                        }
                        characters.forEach {
                            MenuItem {
                                key = it.id.toString()
                                value = it.id.toString()
                                +"${it.name} - ${it.getClassDescription()}"
                            }
                        }
                    }
                }
                Button {
                    onClick = { importerIsOpen = true }
                    +"Add Character"
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
                                languages.asList(),
                                playTier,
                                preferredCharacter
                            )
                        )
                    )
                    store.dispatch(EditorClosed())
                }
                +"Save Changes"
            }
        }
    }

    CharacterImporter {
        open = importerIsOpen
        onClose = { importerIsOpen = false }
        id = playerId
    }

}
