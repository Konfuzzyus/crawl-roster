package components

import kotlinx.browser.window
import mui.icons.material.BugReport
import mui.icons.material.Login
import mui.icons.material.Logout
import mui.icons.material.ManageAccounts
import mui.icons.material.SmartToy
import mui.material.Avatar
import mui.material.Chip
import mui.material.ChipVariant
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Menu
import mui.material.MenuItem
import web.dom.Element
import web.html.HTMLDivElement
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.events.MouseEvent
import react.dom.events.MouseEventHandler
import react.use
import react.useEffectWithCleanup
import react.useState
import reducers.PlayerEditorOpened
import reducers.StoreContext
import reducers.UserLoggedOut

val Identity = FC<Props> {
    val store = use(StoreContext)!!
    val (profile, setProfile) = useState(store.state.identity.player)
    val (botCoordinates, setBotCoordinates) = useState(store.state.server.settings.botCoordinates)
    var anchor by useState<Element>()

    useEffectWithCleanup {
        val unsubscribe = store.subscribe {
            setProfile(store.state.identity.player)
            setBotCoordinates(store.state.server.settings.botCoordinates)
        }
        onCleanup(unsubscribe)
    }

    val handleClose = { anchor = null }
    val handleLogout: MouseEventHandler<*> = {
        store.dispatch(UserLoggedOut())
        handleClose()
    }

    if (profile == null) {
        Chip {
            id = "identity-chip"
            label = ReactNode("Login with Discord")
            variant = ChipVariant.outlined
            onClick = { window.location.replace("/auth/discord/login") }
            icon = Login.create()
        }
    } else {
        Chip {
            id = "identity-chip"
            avatar = Avatar.create {
                src = profile.player.avatarUrl
                +(profile.player.details.name ?: "A")
            }
            label = ReactNode(profile.player.discordHandle)
            variant = ChipVariant.outlined
            onClick = { anchor = it.currentTarget }
            onDelete = {
                val a = it.unsafeCast<MouseEvent<HTMLDivElement, *>>()
                anchor = a.currentTarget
            }
            deleteIcon = mui.icons.material.Menu.create()
        }
        Menu {
            open = anchor != null
            if (anchor != null) {
                anchorEl = { anchor as Element }
            }
            onClose = handleClose

            MenuItem {
                onClick = {
                    store.dispatch(PlayerEditorOpened(profile.player))
                    handleClose()
                }
                ListItemIcon { ManageAccounts {} }
                ListItemText { +"Profile" }
            }
            if (botCoordinates != null) {
                MenuItem {
                    onClick = {
                        window.open(botCoordinates.getInviteLink(), "_blank")
                    }
                    ListItemIcon { SmartToy {} }
                    ListItemText { +"Invite Crawl Butler" }
                }
            }
            MenuItem {
                onClick = {
                    window.open("https://github.com/CodeCranachan/crawl-roster/issues", "_blank")
                }
                ListItemIcon { BugReport {} }
                ListItemText { +"Report a Bug" }
            }
            MenuItem {
                onClick = handleLogout
                ListItemIcon { Logout {} }
                ListItemText { +"Logout" }
            }
        }
    }
}
