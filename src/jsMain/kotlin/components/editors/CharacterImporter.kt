package components.editors

import com.benasher44.uuid.Uuid
import mui.icons.material.Cancel
import mui.icons.material.Save
import mui.material.Button
import mui.material.Dialog
import mui.material.DialogActions
import mui.material.DialogContent
import mui.material.DialogTitle
import mui.material.FormControlMargin
import mui.material.TextField
import org.codecranachan.roster.Character
import org.w3c.dom.HTMLInputElement
import react.FC
import react.Props
import react.ReactNode
import react.create
import react.dom.events.ChangeEvent
import react.dom.onChange
import react.useContext
import react.useState
import reducers.StoreContext
import reducers.addPlayerCharacter

private val characterLinkRegex = Regex("(?:https://)?(?:ddb.ac/characters/|www.dndbeyond.com/characters/)(\\d+)(?:/\\w+)?")
private fun parseSharedCharacterLink(link: String): Int? {
    return characterLinkRegex.find(link)?.run { groups[1]?.value?.toIntOrNull() }
}

external interface CharacterImporterProps : Props {
    var open: Boolean
    var onClose: () -> Unit
    var id: Uuid
}

val CharacterImporter = FC<CharacterImporterProps> { props ->
    val store = useContext(StoreContext)
    var characterLink by useState("")

    Dialog {
        open = props.open

        DialogTitle {
            +"Import Character"
        }
        DialogContent {
            TextField {
                margin = FormControlMargin.normal
                fullWidth = true
                label = ReactNode("Character Link")
                placeholder = "Paste a shareable link from dndbeyond.com"
                value = characterLink
                onChange = {
                    val e = it.unsafeCast<ChangeEvent<HTMLInputElement>>()
                    characterLink = e.target.value
                }
            }
        }
        DialogActions {
            Button {
                startIcon = Cancel.create()
                onClick = { _ -> props.onClose() }
                +"Cancel"
            }
            Button {
                startIcon = Save.create()
                onClick = { _ ->
                    val characterId = characterLink.run(::parseSharedCharacterLink)
                    if (characterId != null) store.dispatch(addPlayerCharacter(Character(dndBeyondId = characterId)))
                    characterLink = ""
                    props.onClose()
                }
                +"Import"
            }
        }
    }
}