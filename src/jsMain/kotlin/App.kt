import components.Account
import components.Identity
import components.RosterWidget
import mui.material.*
import mui.material.styles.TypographyVariant
import mui.system.responsive
import react.*
import reducers.StoreContext
import reducers.updateUserId

external interface AppProps : Props {
    var version: String
}

val App = FC<AppProps> { props ->
    val store = useContext(StoreContext)
    val (isLoaded, setIsLoaded) = useState(store.state.identity.isLoaded)
    val (userIdentity, setUserIdentity) = useState(store.state.identity.data)

    useEffectOnce {
        val unsubscribe = store.subscribe {
            setIsLoaded(store.state.identity.isLoaded)
            setUserIdentity(store.state.identity.data)
        }
        store.dispatch(updateUserId())
        cleanup(unsubscribe)
    }

    Container {
        maxWidth = "lg"
        Grid {
            container = true
            spacing = responsive(2)
            Grid {
                item = true
                xs = 8

                Typography {
                    variant = TypographyVariant.h5
                    +"Crawl-Roster ${props.version}"
                }
            }
            Grid {
                item = true
                xs = 4

                if (isLoaded) {
                    Stack {
                        direction = responsive(StackDirection.column)
                        Identity { }
                        Account {
                            profile = userIdentity?.profile
                        }
                    }
                } else {
                    CircularProgress { }
                }
            }
            Grid {
                item = true
                xs = 12
                if (isLoaded) {
                    RosterWidget { }
                } else {
                    CircularProgress { }
                }
            }
        }
    }

}