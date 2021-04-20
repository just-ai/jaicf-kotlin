package com.justai.jaicf.channel.viber.sdk.api

import com.justai.jaicf.channel.viber.sdk.message.Button
import com.justai.jaicf.channel.viber.sdk.message.Keyboard
import com.justai.jaicf.channel.viber.sdk.message.Location

abstract class ViberButton {
    abstract val actionBody: String
    open val actionType: ActionType = ActionType.REPLY
    abstract val text: String
    open val redirectUrl: String? = null
    open val silent: Boolean = true
    open val imageUrl: String? = null
    open var style: Style? = null
    open val location: Location? = null
    open val columns: Int? = null
    open val rows: Int? = null

    data class Style(
        val backgroundColor: String? = "#FFFFFF",
        val textSize: Size = Size.MEDIUM,
        val textVerticalAlign: Align = Align.MIDDLE,
        val textHorizontalAlign: Align = Align.MIDDLE
    )
}

data class NoActionButton @JvmOverloads constructor(
    override val text: String,
    override var style: Style? = null,
    override val imageUrl: String? = null,
    override val columns: Int? = null,
    override val rows: Int? = null
) : ViberButton() {
    override val actionBody = text
    override val actionType = ActionType.NONE
}

abstract class FunctionalButton : ViberButton()

data class ReplyButton @JvmOverloads constructor(
    override val text: String,
    val callbackData: String = text,
    override val silent: Boolean = true,
    override var style: Style? = null,
    override val imageUrl: String? = null,
    override val columns: Int? = null,
    override val rows: Int? = null
) : FunctionalButton() {
    override val actionBody = callbackData
    override val actionType = ActionType.REPLY
}

/**
 * @link https://stackoverflow.com/questions/58392554/viber-keeps-putting-clicked-button-url-into-conversation
 */
data class RedirectButton @JvmOverloads constructor(
    override val text: String,
    override val redirectUrl: String,
    override val silent: Boolean = true,
    override var style: Style? = null,
    override val imageUrl: String? = null,
    override val columns: Int? = null,
    override val rows: Int? = null
) : FunctionalButton() {
    override val actionBody = redirectUrl
    override val actionType = ActionType.OPEN_URL
}

data class OpenMapButton @JvmOverloads constructor(
    override val text: String,
    override val location: Location,
    override var style: Style? = null,
    override val columns: Int? = null,
    override val rows: Int? = null
) : FunctionalButton() {
    override val actionBody = "open map"
    override val actionType = ActionType.OPEN_MAP
}

enum class Align(internal val view: String) {
    LEFT("left"), MIDDLE("middle"), RIGHT("right")
}

enum class Size(internal val view: String) {
    SMALL("small"), MEDIUM("medium"), LARGE("large")
}

enum class ActionType(internal val view: String) {
    NONE("none"),
    REPLY("reply"),
    OPEN_URL("open-url"),
    OPEN_MAP("open-map")
}

fun List<FunctionalButton>.toKeyboard() = Keyboard(map { it.toButton() })

fun ViberButton.toButton() = Button(
    text = text,
    actionBody = actionBody,
    actionType = actionType.view,
    image = imageUrl,
    silent = silent,
    backgroundColor = style?.backgroundColor,
    textSize = style?.textSize?.view,
    textVerticalAlign = style?.textVerticalAlign?.view,
    textHorizontalAlign = style?.textHorizontalAlign?.view,
    map = location,
    columns = columns,
    rows = rows
)
