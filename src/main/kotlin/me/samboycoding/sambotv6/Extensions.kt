package me.samboycoding.sambotv6

import me.samboycoding.sambotv6.commands.CommandData
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message


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
