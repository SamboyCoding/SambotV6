package me.samboycoding.sambotv6.commands

import me.samboycoding.sambotv6.SambotV6
import me.samboycoding.sambotv6.getCommandData
import me.samboycoding.sambotv6.isModerator
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

class SetPrefixCommand : BaseCommand() {
    override fun getCommandWord(): String {
        return "prefix"
    }

    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        //Check user has some sort of management perm
        if(!author.isModerator())
            return msg.sendMissingPermissionMessage()

        //Check args length
        val data = msg.getCommandData(config)
        if(data.argsCount < 1)
            return channel.doSend(getString("setPrefixMissingPrefix", author.asMention))

        //Set prefix
        val oldPrefix = config.prefix
        config.prefix = data.getArg(0)!!

        //Save in db
        SambotV6.botLogger.info("Updating prefix for guild $guild => ${config.prefix}")
        config.flushChanges()

        //Feedback
        msg.delete().queue()
        channel.doSend(getString("setPrefixSuccess", author.asMention, oldPrefix, config.prefix))
    }

}