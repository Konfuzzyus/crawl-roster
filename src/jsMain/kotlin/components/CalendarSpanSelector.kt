package components

import kotlinx.datetime.LocalDate
import mui.material.FormControlMargin
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
        value = range.first?.toString()
        type = InputType.date
        onChange = {
            val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
            store.dispatch(selectCalendarRange(LocalDate.parse(e.target.value), range.second))
        }
    }
    TextField {
        margin = FormControlMargin.dense
        label = ReactNode("before")
        value = range.second?.toString()
        type = InputType.date
        onChange = {
            val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
            store.dispatch(selectCalendarRange(range.first, LocalDate.parse(e.target.value)))
        }
    }
}
