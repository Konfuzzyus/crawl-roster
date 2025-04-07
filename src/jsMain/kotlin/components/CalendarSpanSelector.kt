package components

import js.objects.jso
import kotlinx.datetime.LocalDate
import mui.material.FormControlMargin
import mui.material.InputLabel
import mui.material.InputLabelOwnProps
import mui.material.InputLabelProps
import mui.material.TextField
import react.FC
import react.Props
import react.ReactNode
import react.dom.events.ChangeEvent
import react.dom.onChange
import react.use
import react.useEffectOnceWithCleanup
import react.useState
import reducers.StoreContext
import reducers.selectCalendarRange
import web.html.HTMLInputElement
import web.html.InputType

val CalendarSpanSelector = FC<Props> {
    val store = use(StoreContext)!!
    var range by useState(store.state.calendar.selectedDateRange)

    useEffectOnceWithCleanup {
        val unsubscribe = store.subscribe {
            range = store.state.calendar.selectedDateRange
        }
        onCleanup(unsubscribe)
    }

    TextField {
        margin = FormControlMargin.dense
        label = ReactNode("after")
        value = range.first?.toString() ?: ""
        type = InputType.date
        InputLabelProps = jso {
            shrink = true
        }
        onChange = {
            val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
            val date = if (e.target.value.isBlank()) null else LocalDate.parse(e.target.value)
            store.dispatch(selectCalendarRange(date, range.second))
        }
    }
    TextField {
        margin = FormControlMargin.dense
        label = ReactNode("before")
        value = range.second?.toString() ?: ""
        type = InputType.date
        InputLabelProps = jso {
            shrink = true
        }
        onChange = {
            val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
            val date = if (e.target.value.isBlank()) null else LocalDate.parse(e.target.value)
            store.dispatch(selectCalendarRange(range.first, date))
        }
    }
}
