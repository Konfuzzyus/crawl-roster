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
import org.codecranachan.roster.query.ResolvedTable
import react.FC
import react.Props
import react.ReactNode
import react.useContext
import theme.ThemeContext

external interface PlaySessionOccupancyIndicatorProps : Props {
    var data: ResolvedTable
}

val PlayTableIndicator = FC<PlaySessionOccupancyIndicatorProps> { props ->
    val theme by useContext(ThemeContext)
    Badge {
        anchorOrigin = object : BadgeOrigin {
            override var horizontal = BadgeOriginHorizontal.left
            override var vertical = BadgeOriginVertical.bottom
        }
        variant = BadgeVariant.standard
        color = BadgeColor.default
        badgeContent = props.data.table?.let { ReactNode(it.details.language.flag) }
        overlap = BadgeOverlap.circular

        Tooltip {
            title = ReactNode(props.data.name)
            Box {
                sx {
                    position = Position.relative
                    display = Display.inlineFlex
                }
                Avatar {
                    sx {
                        opacity = number(.1)
                    }
                    src = props.data.dungeonMaster?.avatarUrl
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
                    value = props.data.occupancyPercent
                    color = CircularProgressColor.info
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
                    +props.data.occupancyFraction
                }
            }
        }
    }
}
