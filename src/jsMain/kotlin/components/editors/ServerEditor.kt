package components.editors

import api.fetchDiscordAccountInfo
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import mui.icons.material.AddLink
import mui.icons.material.Close
import mui.icons.material.Link
import mui.icons.material.LinkOff
import mui.material.Button
import mui.material.CircularProgress
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.ListItem
import mui.material.ListItemButton
import mui.material.ListItemIcon
import mui.material.ListItemText
import org.codecranachan.roster.DiscordGuild
import org.codecranachan.roster.DiscordUserInfo
import org.codecranachan.roster.Guild
import org.codecranachan.roster.Server
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useContext
import react.useEffectOnce
import react.useState
import reducers.EditorClosed
import reducers.StoreContext
import reducers.linkGuild

external interface GuildListEntryProps : Props {
    var guildLimit: Int
    var attunedGuilds: List<Guild>
    var guild: DiscordGuild
}


val GuildListEntry = FC<GuildListEntryProps> { props ->
    val store = useContext(StoreContext)
    val isLinked = props.attunedGuilds.any { props.guild.id == it.discordId }
    val isLinkable = props.attunedGuilds.size < props.guildLimit &&
            (props.guild.isAdmin() || props.guild.owner) && !isLinked

    ListItem {
        ListItemButton {
            when {
                isLinkable -> {
                    ListItemIcon { AddLink() }
                    disabled = false
                    onClick = {
                        store.dispatch(linkGuild(Guild(uuid4(), props.guild.name, props.guild.id)))
                        store.dispatch(EditorClosed())
                    }
                }
                isLinked -> {
                    ListItemIcon { Link() }
                    disabled = true
                }
                else -> {
                    ListItemIcon { LinkOff() }
                    disabled = true
                }
            }

            ListItemText {
                primary = ReactNode(props.guild.name)
                secondary = ReactNode(
                    if (isLinkable) "Click to attune this guild"
                    else when {
                        props.guild.owner -> "Owner"
                        props.guild.isAdmin() -> "Admin"
                        else -> ""
                    }
                )
            }
        }
    }
}

val ServerEditor = FC<Props> {
    val store = useContext(StoreContext)
    var isOpen by useState(false)

    var settings by useState(Server())
    var accountInfo by useState<DiscordUserInfo?>(null)

    useEffectOnce {
        MainScope().launch {
            accountInfo = fetchDiscordAccountInfo()
        }
        val unsubscribe = store.subscribe {
            val s = store.state.ui.editorTarget
            if (s is Server) {
                isOpen = true
                settings = s
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
            +"Server Settings"
        }

        DialogContent {
            val info = accountInfo
            if (info == null) {
                CircularProgress {}
            } else {
                mui.material.List {
                    info.guilds.forEach {
                        GuildListEntry {
                            guildLimit = settings.guildLimit
                            attunedGuilds = settings.guilds
                            guild = it
                        }
                    }
                }
            }
            DialogActions {
                Button {
                    startIcon = Close.create()
                    onClick = { _ ->
                        store.dispatch(EditorClosed())
                    }
                    +"Close"
                }
            }
        }
    }
}