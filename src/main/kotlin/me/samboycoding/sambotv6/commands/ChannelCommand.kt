package me.samboycoding.sambotv6.commands

import me.samboycoding.sambotv6.getCommandData
import me.samboycoding.sambotv6.isModerator
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

class ChannelCommand : BaseCommand() {
    override fun getCommandWord(): String {
        return "channel"
    }

    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        //They need to be a mod
        if (!author.isModerator())
            return msg.sendMissingPermissionMessage()

        val data = msg.getCommandData(config)

        //Going for zero-strings here because i told the translators they were done >->
        if(data.argsCount < 1) {
            msg.addReaction("❓").submit()
            return
        }

        when(data.getArg(0, true)?.toLowerCase()) {
            "greeting" -> {
                config.joinChannel = channel
                config.flushChanges()
                msg.addReaction("✅").submit()
            }
            else -> {
                msg.addReaction("❓").submit()
            }
        }

    }
}