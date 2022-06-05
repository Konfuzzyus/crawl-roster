package components

import csstype.HtmlAttributes
import mui.material.*
import mui.system.responsive
import org.codecranachan.roster.Table
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.ReactNode
import react.dom.html.InputHTMLAttributes
import react.dom.html.InputType
import react.dom.onChange
import react.useState

external interface TableEditorProps : Props {
    var table: Table
}

val TableEditor = FC<TableEditorProps> { props ->
    val (isOpen, setIsOpen) = useState(false)
    val (moduleName, setModuleName) = useState("")
    val (playerLimit, setPlayerLimit) = useState(6)
    val (levelRange, setLevelRange) = useState(arrayOf(1, 20))

    Stack {
        spacing = responsive(2)
        TextField {
            label = ReactNode("Module")
            value = moduleName
            onChange = {
                val t = it.target as HTMLInputElement
                setModuleName(t.value)
            }
        }
        TextField {
            label = ReactNode("Player Limit")
            value = playerLimit
            type = InputType.number
            onChange = {
                val t = it.target as HTMLInputElement
                setPlayerLimit(t.value.toInt())
            }
        }
        Slider {
            min = 1
            max = 20
            value = levelRange
            onChange = { _, value, _ ->
                setLevelRange(value as Array<Int>)
            }
        }

    }
}
