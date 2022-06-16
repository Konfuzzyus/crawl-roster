package components.events

import mui.material.Badge
import mui.material.BadgeOverlap
import org.codecranachan.roster.PlaySession
import react.FC
import react.Props

external interface PlaySessionOccupancyIndicatorProps : Props {
    var occupancy: PlaySession
}

val PlaySessionOccupancyIndicator = FC<PlaySessionOccupancyIndicatorProps> { props ->
    val table = props.occupancy.table

    Badge {
        badgeContent = react.ReactNode(table.details.language.flag)
        overlap = BadgeOverlap.circular
        mui.material.CircularProgress {
            variant = mui.material.CircularProgressVariant.determinate
            when (props.occupancy.getState()) {
                org.codecranachan.roster.TableState.Full -> {
                    color = mui.material.CircularProgressColor.warning
                    value = 100
                }
                org.codecranachan.roster.TableState.Ready -> {
                    color = mui.material.CircularProgressColor.success
                    value = 75
                }
                org.codecranachan.roster.TableState.Understrength -> {
                    color = mui.material.CircularProgressColor.error
                    value = 25
                }
                org.codecranachan.roster.TableState.Empty -> {
                    color = mui.material.CircularProgressColor.inherit
                    value = 100
                }
            }
        }
    }
}
