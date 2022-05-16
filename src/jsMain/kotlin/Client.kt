import kotlinx.browser.document
import me.konfuzzyus.roster.PlayerListing
import react.create
import react.dom.client.createRoot
import react.dom.render

fun main() {
    val container = document.createElement("div")
    document.body!!.appendChild(container)

    val welcome = Welcome.create {
        name = "Kotlin/JS"
        players = PlayerListing(listOf())
    }

    val root = createRoot(container)
    root.render(welcome)
}