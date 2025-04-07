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
import react.*
import reducers.StoreContext
import reducers.updateUserId

val App = FC<Props> {
    val store = use(StoreContext)!!
    val (isLoaded, setIsLoaded) = useState(store.state.identity.isLoaded)

    useEffectOnceWithCleanup {
        val unsubscribe = store.subscribe {
            setIsLoaded(store.state.identity.isLoaded)
        }
        store.dispatch(updateUserId())
        onCleanup(unsubscribe)
    }

    Container {
        maxWidth = "lg"
        Stack {
            direction = responsive(StackDirection.column)
            Stack {
                direction = responsive(StackDirection.row)
                Typography {
                    variant = TypographyVariant.h5
                    +"Crawl-Roster"
                }
                if (isLoaded) {
                    Identity { }
                } else {
                    CircularProgress { }
                }
            }
            if (isLoaded) {
                RosterWidget { }
            } else {
                CircularProgress { }
            }
        }
    }

}