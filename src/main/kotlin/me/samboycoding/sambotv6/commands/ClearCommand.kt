package me.samboycoding.sambotv6.commands

import me.samboycoding.sambotv6.getCommandData
import me.samboycoding.sambotv6.isModerator
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import java.time.OffsetDateTime
import kotlin.math.min

class ClearCommand : BaseCommand() {
    private val MAX_MESSAGES = 500
    private val dateDelegate = { msg: Message -> msg.timeCreated.plusDays(14) >= OffsetDateTime.now() }

    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        //We need manage messages
        if (!failIfBotMissingPerm(Permission.MESSAGE_MANAGE))
            return

        //They need to be a mod
        if (!author.isModerator())
            return msg.sendMissingPermissionMessage()

        //Get command data
        val data = msg.getCommandData(config)

        //Command can be used 3 ways.
        //No args - clean as much as we can
        //User arg - clean as much as we can from that user
        //Number arg - clean x messages


        //Work out which predicate to use
        val usr = data.getUserArg(0, true)
        val count = data.getIntArg(0)
        val delegate: (Message) -> Boolean = when {
            //No args -> accept all
            data.argsCount == 0 -> { _ -> true }

            //User -> accept those authored by them
            usr != null -> { test -> test.member == usr }

            //Count -> accept first count
            count > 0 -> {
                var numDone = 0
                {
                    numDone++
                    numDone <= count
                }
            }

            //Otherwise (string) send a syntax message
            else -> return channel.doSend(getString("clearCommandSyntax", author.asMention, MAX_MESSAGES))
        }

        //Load message history
        var remaining = MAX_MESSAGES - 1
        val history: MessageHistory = channel.getHistoryBefore(msg, 1).complete()
        val msgs = history.retrievedHistory.toMutableList()
        while (remaining > 0) {
            val curr = msgs.size
            msgs.addAll(history.retrievePast(min(100, remaining)).complete())

            if(curr - msgs.size < remaining)
                //Reached the end of history
                remaining = 0

            remaining -= 100
        }

        //Filter based on our delegate and the requirement that the message be < 2 weeks old
        //Also take the first MAX_MESSAGES
        val filtered = msgs.filter { dateDelegate.invoke(it) && delegate.invoke(it) }.take(MAX_MESSAGES)

        //Delete in chunks
        filtered.chunked(100).forEach { chunk ->
            if(chunk.size == 1)
                chunk.single().delete()
            else
                channel.deleteMessages(chunk).complete()
        }

        //Reply
        channel.doSend(getString("clearCommandComplete", author.asMention, filtered.size))

        //Delete the command
        msg.delete().submit()
    }

    override fun getCommandWords(): List<String> {
        return listOf("clear", "nuke", "clean")
    }
}