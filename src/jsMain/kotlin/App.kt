import react.FC
import react.Props
import react.dom.html.ReactHTML.div

external interface AppProps : Props {
    var version: String
}

val App = FC<AppProps> { props ->
    div {
        +"Test?"
    }
}