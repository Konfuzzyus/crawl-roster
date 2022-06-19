import components.Identity
import components.RosterWidget
import mui.material.CircularProgress
import mui.material.Container
import mui.material.Grid
import mui.material.Stack
import mui.material.StackDirection
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import react.FC
import react.Props
import react.useContext
import react.useEffectOnce
import react.useState
import reducers.StoreContext
import reducers.updateUserId

val App = FC<Props> {
    val store = useContext(StoreContext)
    val (isLoaded, setIsLoaded) = useState(store.state.identity.isLoaded)

    useEffectOnce {
        val unsubscribe = store.subscribe {
            setIsLoaded(store.state.identity.isLoaded)
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
                    variant = TypographyVariant.h4
                    +"Crawl-Roster"
                }
            }
            Grid {
                item = true
                xs = 4

                if (isLoaded) {
                    Stack {
                        direction = responsive(StackDirection.column)
                        Identity { }
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