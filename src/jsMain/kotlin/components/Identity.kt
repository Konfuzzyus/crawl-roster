package components

import kotlinx.browser.window
import mui.icons.material.Login
import mui.icons.material.Logout
import mui.icons.material.Settings
import mui.material.Avatar
import mui.material.Chip
import mui.material.ChipVariant
import mui.material.ListItemIcon
import mui.material.ListItemText
import mui.material.Menu
import mui.material.MenuItem
import org.w3c.dom.Element
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.events.MouseEventHandler
import react.useContext
import react.useEffect
import react.useState
import reducers.PlayerEditorOpened
import reducers.StoreContext
import reducers.UserLoggedOut

val Identity = FC<Props> {
    val store = useContext(StoreContext)
    val (profile, setProfile) = useState(store.state.identity.data)
    var anchor by useState<Element>()

    useEffect {
        val unsubscribe = store.subscribe { setProfile(store.state.identity.data) }
        cleanup(unsubscribe)
    }

    val handleClose: MouseEventHandler<*> = { anchor = null }

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
                alt = profile.name
                src = profile.profile?.avatarUrl
            }
            label = ReactNode(profile.name)
            variant = ChipVariant.outlined
            onClick = { anchor = it.currentTarget }
        }
        Menu {
            open = anchor != null
            if (anchor != null) {
                anchorEl = { anchor as Element }
            }
            onClose = handleClose

            MenuItem {
                onClick = {
                    store.dispatch(PlayerEditorOpened(profile.profile!!))
                    handleClose(it)
                }
                ListItemIcon { Settings {} }
                ListItemText { +"Profile" }
            }
            MenuItem {
                onClick = {
                    store.dispatch(UserLoggedOut())
                    handleClose(it)
                }
                ListItemIcon { Logout {} }
                ListItemText { +"Logout" }
            }
        }
    }
}
