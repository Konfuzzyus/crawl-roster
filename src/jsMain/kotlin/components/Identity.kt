package components

import kotlinx.browser.window
import mui.icons.material.Login
import mui.icons.material.Logout
import mui.material.Avatar
import mui.material.Chip
import mui.material.ChipVariant
import react.*
import reducers.StoreContext

val Identity = FC<Props> {
    val store = useContext(StoreContext)
    val (profile, setProfile) = useState(store.state.identity.data)

    useEffect {
        val unsubscribe = store.subscribe { setProfile(store.state.identity.data) }
        cleanup(unsubscribe)
    }
    if (profile == null) {
        Chip {
            label = ReactNode("Login")
            variant = ChipVariant.outlined
            onClick = { window.location.replace("/auth/discord/login") }
            icon = Login.create()
        }
    } else {
        Chip {
            avatar = Avatar.create {
                alt = profile.name
                src = profile.profile?.avatarUrl
            }
            label = ReactNode(profile.name)
            variant = ChipVariant.outlined
            onDelete = { window.location.replace("/auth/logout") }
            deleteIcon = Logout.create()
        }
    }
}
