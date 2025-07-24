package com.justai.jaicf.examples.llm.channel

import com.justai.jaicf.api.BotApi
import com.justai.jaicf.api.BotRequestController
import com.justai.jaicf.api.QueryBotRequest
import com.justai.jaicf.channel.BotChannel
import com.justai.jaicf.context.RequestContext
import com.justai.jaicf.logging.SayReaction
import com.justai.jaicf.reactions.Reactions
import com.justai.jaicf.reactions.StreamReactions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jline.reader.LineReaderBuilder
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.utils.AttributedString
import org.jline.utils.AttributedStyle
import java.util.*
import java.util.stream.Stream

private val promptStyle = AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN)
private val promptString = AttributedString("> ", promptStyle).toAnsi()
private val cancelledString = AttributedString("cancelled", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED)).toAnsi()
private val cancelString = AttributedString("Press ENTER to cancel request", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW))

class ConsoleChannel(
    override val botApi: BotApi
) : BotChannel {
    private var clientId: String = UUID.randomUUID().toString()
    private val terminal = TerminalBuilder.builder().system(true).build()
    private val reader = LineReaderBuilder.builder().terminal(terminal).build()

    fun prompt() {
        terminal.writer().write(promptString)
        terminal.writer().flush()
    }

    fun run(startMessage: String? = null) = runBlocking(Dispatchers.IO) {
        if (!startMessage.isNullOrBlank()) {
            terminal.writer().println(AttributedString("> $startMessage", promptStyle).toAnsi())
        } else {
            prompt()
        }

        var cancelShown = false
        var input = startMessage
        val reactions = ConsoleReactions(terminal)
        val controller = BotRequestController {
            if (it is CancellationException) {
                reader.printAbove(cancelledString)
            }
            prompt()
        }

        while (isActive) {
            if (input.isNullOrBlank()) {
                input = reader.readLine()
                if (!cancelShown && !input.isNullOrBlank()) {
                    cancelShown = true
                    terminal.writer().println(cancelString.toAnsi())
                }
            }

            if (controller.isActive && input.isNullOrBlank()) {
                controller.cancelAndJoin()
            } else if (!controller.isActive && !input.isNullOrBlank()) {
                val request = QueryBotRequest(clientId, input)
                launch {
                    botApi.process(request, reactions, RequestContext.DEFAULT, requestController = controller)
                }
            } else {
                prompt()
            }

            input = null
        }
    }
}

class ConsoleReactions(
    private val terminal: Terminal
) : StreamReactions, Reactions() {
    override fun say(text: String) = SayReaction.create(text).also {
        terminal.writer().println(text)
        terminal.writer().flush()
    }

    override fun say(stream: Stream<String>): SayReaction {
        var text = ""
        stream.forEach {
            text += it
            terminal.writer().print(it)
            terminal.writer().flush()
        }
        terminal.writer().println()
        return SayReaction.create(text)
    }
}