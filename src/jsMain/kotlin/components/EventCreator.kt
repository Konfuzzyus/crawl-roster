package components

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayAt
import mui.icons.material.AddCircle
import mui.material.Button
import mui.material.Chip
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.Divider
import mui.material.Input
import mui.material.InputLabel
import org.codecranachan.roster.Event
import org.codecranachan.roster.Guild
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.useContext
import react.useState
import reducers.StoreContext
import reducers.createEvent


external interface SubmitEventProps : Props {
    var guild: Guild
}

val SubmitEvent = FC<SubmitEventProps> { props ->
    val store = useContext(StoreContext)
    val (isOpen, setIsOpen) = useState(false)
    val (selectedDate, setSelectedDate) = useState(Clock.System.todayAt(TimeZone.currentSystemDefault()))

    Divider {
        Chip {
            icon = AddCircle.create()
            label = ReactNode("Add Event")
            onClick = { setIsOpen(true) }
        }
    }
    Dialog {
        open = isOpen
        onClose = { _, _ -> setIsOpen(false) }
        DialogTitle {
            +"Create new event"
        }
        DialogContent {
            InputLabel {
                +"Event Date"
            }
            Input {
                autoFocus = true
                type = "date"
                value = selectedDate.toString()
                required = true
                onChange = {
                    val t = it.target as HTMLInputElement
                    setSelectedDate(LocalDate.parse(t.value))
                }
            }
        }
        DialogActions {
            Button {
                onClick = {
                    store.dispatch(
                        createEvent(
                            Event(
                                uuid4(),
                                props.guild.id,
                                selectedDate
                            )
                        )
                    )
                    setIsOpen(false)
                }
                +"Add"
            }
        }
    }
}
