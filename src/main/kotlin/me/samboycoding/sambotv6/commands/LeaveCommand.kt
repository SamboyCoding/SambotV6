package me.samboycoding.sambotv6.commands

import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

class LeaveCommand : BaseCommand() {
    override fun getCommandWord(): String {
        return "leave"
    }

    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        //TODO: Make an audio manager and notify it that we left.
        guild.audioManager.closeAudioConnection()
    }
}