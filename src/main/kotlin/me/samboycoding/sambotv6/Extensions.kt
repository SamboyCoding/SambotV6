package me.samboycoding.sambotv6

import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.entity.sequenceOf
import me.samboycoding.sambotv6.commands.CommandData
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import me.samboycoding.sambotv6.orm.tables.ChannelLocales
import me.samboycoding.sambotv6.orm.tables.CustomRoles
import me.samboycoding.sambotv6.orm.tables.GuildConfigurations
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.VoiceChannel


fun Message.getCommandData(config: GuildConfiguration): CommandData {
    val withoutPrefix = contentRaw.removePrefix(config.prefix)

    return CommandData(withoutPrefix, config.guild!!, this)
}

fun Member.hasAnyPermission(vararg permissions: Permission): Boolean =
    permissions.map { p -> hasPermission(p) }.contains(true)

fun Member.isModerator(): Boolean = hasAnyPermission(
    Permission.MANAGE_ROLES,
    Permission.KICK_MEMBERS,
    Permission.BAN_MEMBERS,
    Permission.ADMINISTRATOR,
    Permission.MANAGE_SERVER
)

fun VoiceChannel.join() {
    guild.audioManager.openAudioConnection(this)
}

val Database.guildConfigurations get() = this.sequenceOf(GuildConfigurations)
val Database.customRoles get() = this.sequenceOf(CustomRoles)
val Database.channelLocales get() = this.sequenceOf(ChannelLocales)