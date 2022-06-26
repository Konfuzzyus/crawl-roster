package components.events

import csstype.AlignItems
import csstype.ColorProperty
import csstype.Display
import csstype.JustifyContent
import csstype.Position
import csstype.number
import csstype.px
import mui.material.Avatar
import mui.material.Badge
import mui.material.BadgeColor
import mui.material.BadgeOrigin
import mui.material.BadgeOriginHorizontal
import mui.material.BadgeOriginVertical
import mui.material.BadgeOverlap
import mui.material.BadgeVariant
import mui.material.Box
import mui.material.CircularProgress
import mui.material.CircularProgressColor
import mui.material.CircularProgressVariant
import mui.material.Tooltip
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.sx
import org.codecranachan.roster.PlaySession
import react.FC
import react.Props
import react.ReactNode
import react.useContext
import theme.ThemeContext

external interface PlaySessionOccupancyIndicatorProps : Props {
    var session: PlaySession
}

val PlayTableIndicator = FC<PlaySessionOccupancyIndicatorProps> { props ->
    val theme by useContext(ThemeContext)
    val table = props.session.table
    Badge {
        anchorOrigin = object : BadgeOrigin {
            override var horizontal = BadgeOriginHorizontal.left
            override var vertical = BadgeOriginVertical.bottom
        }
        variant = BadgeVariant.standard
        color = BadgeColor.default
        badgeContent = react.ReactNode(table.details.language.flag)
        overlap = BadgeOverlap.circular

        Tooltip {
            title = ReactNode(table.getName())
            Box {
                sx {
                    position = Position.relative
                    display = Display.inlineFlex
                }
                Avatar {
                    sx {
                        opacity = number(.1)
                    }
                    src = table.dungeonMaster.avatarUrl
                }
                CircularProgress {
                    sx {
                        position = Position.absolute
                        left = 0.px
                        color = theme.palette.grey[800].unsafeCast<ColorProperty>()
                    }
                    thickness = 4
                    variant = CircularProgressVariant.determinate
                    value = 100
                }
                CircularProgress {
                    sx {
                        position = Position.absolute
                        left = 0.px
                    }
                    thickness = 4
                    variant = CircularProgressVariant.determinate
                    value = props.session.occupancyPercentage()
                    color = when (props.session.getState()) {
                        org.codecranachan.roster.TableState.Full -> CircularProgressColor.info
                        org.codecranachan.roster.TableState.Ready -> CircularProgressColor.success
                        else -> CircularProgressColor.warning
                    }
                }
                Typography {
                    sx {
                        top = 0.px
                        bottom = 0.px
                        left = 0.px
                        right = 0.px
                        position = Position.absolute
                        display = Display.flex
                        alignItems = AlignItems.center
                        justifyContent = JustifyContent.center
                    }
                    variant = TypographyVariant.caption
                    +"${props.session.players.size}/${props.session.table.details.playerRange.last}"
                }
            }
        }
    }
}
