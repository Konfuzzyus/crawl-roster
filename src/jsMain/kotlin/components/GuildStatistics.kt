package components

import mui.icons.material.Person
import mui.material.Badge
import mui.material.Avatar
import mui.material.BadgeColor
import mui.material.Card
import mui.material.CardContent
import mui.material.CircularProgress
import mui.material.CircularProgressColor
import mui.material.CircularProgressVariant
import mui.material.Grid
import mui.material.GridProps
import mui.material.SvgIconColor
import mui.material.SvgIconSize
import mui.material.Typography
import mui.material.styles.TypographyVariant
import mui.system.responsive
import mui.system.sx
import react.FC
import react.Props
import react.ReactNode
import react.use
import react.useEffectOnceWithCleanup
import react.useState
import reducers.StoreContext
import web.cssom.ColorProperty
import web.cssom.Position
import web.cssom.px

inline var GridProps.xs: Int
    get() = TODO("Prop is write-only!")
    set(value) {
        asDynamic().xs = value
    }

val GuildStatistics = FC<Props> {
    val store = use(StoreContext)!!
    var stats by useState(store.state.calendar.stats)
    var identity by useState(store.state.identity.player)

    useEffectOnceWithCleanup {
        val unsubscribeCalendar = store.subscribe { stats = store.state.calendar.stats }
        val unsubscribeIdentity = store.subscribe { identity = store.state.identity.player }
        onCleanup {
            unsubscribeCalendar()
            unsubscribeIdentity()
        }
    }

    Grid {
        container = true
        spacing = responsive(2)
        Grid {
            item = true
            xs = 3
            StatCard {
                title = "total events hosted"
                count = stats.eventStats.eventCount
            }
        }
        Grid {
            item = true
            xs = 3
            StatCard {
                title = "total tables hosted"
                count = stats.eventStats.tablesHosted
            }
        }
        Grid {
            item = true
            xs = 3
            StatCard {
                title = "total players entertained"
                count = stats.eventStats.distinctPlayers
            }
        }
        Grid {
            item = true
            xs = 3
            StatCard {
                title = "total seats filled"
                count = stats.eventStats.seatsFilled
            }
        }
        stats.dmStats.forEach { dmStat ->
            val isLoggedIn =  identity?.player?.id == dmStat.dungeonMaster.id
            Grid {
                item = true
                xs = 1
                Badge {
                    badgeContent = ReactNode("DM")
                    color = if (isLoggedIn) BadgeColor.warning else BadgeColor.primary
                    Avatar {
                        src = dmStat.dungeonMaster.avatarUrl
                        +(dmStat.dungeonMaster.details.name?.take(1) ?: "A")
                    }
                    if (isLoggedIn) {
                        CircularProgress {
                            size = 48.px
                            sx {
                                position = Position.absolute
                                left = (-4).px
                                top = (-4).px
                            }
                            color = CircularProgressColor.warning
                            thickness = 4
                            variant = CircularProgressVariant.indeterminate
                            value = 100
                        }
                    }
                }
            }
            Grid {
                item = true
                xs = 2
                Card {
                    elevation = 0
                    Typography {
                        variant = TypographyVariant.h5
                        +dmStat.dungeonMaster.discordHandle
                    }
                    Typography {
                        variant = TypographyVariant.body2
                        +dmStat.dungeonMaster.details.name
                    }
                }
            }
            Grid {
                item = true
                xs = 3
                StatCard {
                    title = "tables hosted"
                    count = dmStat.tablesHosted
                }
            }
            Grid {
                item = true
                xs = 3
                StatCard {
                    title = "players entertained"
                    count = dmStat.distinctPlayers
                }
            }
            Grid {
                item = true
                xs = 3
                StatCard {
                    title = "seats filled"
                    count = dmStat.seatsFilled
                }
            }
        }
    }
}

external interface StatCardProps : Props {
    var title: String
    var count: Number
}

val StatCard = FC<StatCardProps> { props ->
    Card {
        elevation = 3
        CardContent {
            Typography {
                variant = TypographyVariant.h5
                +props.count.toString()
            }
            Typography {
                variant = TypographyVariant.body2
                +props.title
            }
        }
    }
}