package components.editors

import com.benasher44.uuid.Uuid
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import mui.icons.material.Cancel
import mui.icons.material.Save
import mui.material.Button
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.FormControlMargin
import mui.material.Stack
import mui.material.StackDirection
import mui.material.TextField
import mui.system.responsive
import org.codecranachan.roster.Event
import org.codecranachan.roster.EventDetails
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.events.ChangeEvent
import react.dom.html.InputType
import react.dom.onChange
import react.useContext
import react.useEffectOnce
import react.useState
import reducers.EditorClosed
import reducers.StoreContext
import reducers.updateEventDetails

val EventEditor = FC<Props> {
    val store = useContext(StoreContext)
    val (isOpen, setIsOpen) = useState(false)

    val (eventId, setEventId) = useState(null as Uuid?)
    val (eventDate, setEventDate) = useState(null as LocalDate?)
    val (eventTime, setEventTime) = useState(null as LocalTime?)
    val (eventLocation, setEventLocation) = useState("")

    fun updateEvent(event: Event) {
        setEventId(event.id)
        setEventDate(event.date)
        setEventTime(event.details.time)
        setEventLocation(event.details.location ?: "")
    }

    useEffectOnce {
        val unsubscribe = store.subscribe {
            val t = store.state.ui.editorTarget
            if (t is Event) {
                setIsOpen(true)
                updateEvent(t)
            } else {
                setIsOpen(false)
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
            +eventDate.toString()
        }

        DialogContent {
            Stack {
                direction = responsive(StackDirection.column)
                TextField {
                    margin = FormControlMargin.dense
                    required = false
                    fullWidth = true
                    label = ReactNode("Location")
                    placeholder = "Event location"
                    value = eventLocation
                    onChange = {
                        val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                        setEventLocation(e.target.value)
                    }
                }
                TextField {
                    margin = FormControlMargin.dense
                    required = false
                    fullWidth = true
                    label = ReactNode("Event Time")
                    value = eventTime?.toString()
                    type = InputType.time
                    onChange = {
                        val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                        if (e.target.value.isNullOrBlank()) {
                            setEventTime(null)
                        } else {
                            setEventTime(LocalTime.parse(e.target.value))
                        }
                    }
                }
            }
        }
        DialogActions {
            Button {
                startIcon = Cancel.create()
                onClick = { _ ->
                    store.dispatch(EditorClosed())
                }
                +"Cancel"
            }
            Button {
                startIcon = Save.create()
                onClick = { _ ->
                    store.dispatch(updateEventDetails(eventId!!, EventDetails(eventTime, eventLocation)))
                    store.dispatch(EditorClosed())
                }
                +"Save Changes"
            }
        }
    }
}
