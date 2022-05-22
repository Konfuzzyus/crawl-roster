package components

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayAt
import org.codecranachan.roster.Event
import org.codecranachan.roster.Guild
import org.codecranachan.roster.Player
import org.reduxkotlin.Store
import react.FC
import react.Props
import react.dom.events.ChangeEvent
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.input
import react.useState
import reducers.ApplicationState
import reducers.createEvent


external interface SubmitEventProps : Props {
    var store: Store<ApplicationState>
    var guild: Guild
}

val SubmitEvent = FC<SubmitEventProps> { props ->
    val (selectedDate, setSelectedDate) = useState(Clock.System.todayAt(TimeZone.currentSystemDefault()))

    form {
        onSubmit = {e ->
            e.preventDefault()
            props.store.dispatch(createEvent(Event(
                uuid4(),
                props.guild.id,
                selectedDate,
                listOf()
            )))
        }
        input {
            type = InputType.date
            value = selectedDate.toString()
            required = true
            onChange = {e -> setSelectedDate(LocalDate.parse(e.currentTarget.value))}
        }
        input {
            type = InputType.submit
            value = "Create Event"
        }
    }
}
