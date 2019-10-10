package me.samboycoding.sambotv6.commands

import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

class JoinCommand : BaseCommand() {
    override fun getCommandWord(): String {
        return "join"
    }

    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        if (!failIfBotMissingPerm(Permission.VOICE_CONNECT) || !failIfBotMissingPerm(Permission.VOICE_SPEAK))
            return


    }
}