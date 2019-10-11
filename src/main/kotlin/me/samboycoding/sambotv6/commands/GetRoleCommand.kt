package me.samboycoding.sambotv6.commands

import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.findOne
import me.samboycoding.sambotv6.getCommandData
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import me.samboycoding.sambotv6.orm.tables.CustomRoles
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

class GetRoleCommand : BaseCommand() {
    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        if (!config.customRolesEnabled)
            return msg.sendModuleDisabledMessage(getString("moduleNameCustomRoles"))

        val data = msg.getCommandData(config)

        if (data.argsCount < 1)
            return channel.doSend(getString("getRoleMissingRoleName", author.asMention))

        val roleName = data.getArg(0, true)!!.toLowerCase()

        val customRole = CustomRoles.findOne { it.id eq roleName }
            ?: return channel.doSend(getString("getRoleUnknownRole", author.asMention, roleName))

        if (customRole.role == null) return channel.doSend(getString("getRoleRoleDisappeared", author.asMention, roleName))

        if(!author.roles.contains(customRole.role)) {
            guild.addRoleToMember(author, customRole.role!!).complete()
            channel.doSend(getString("getRoleAdded", author.asMention, customRole.role!!.name))
        } else {
            guild.removeRoleFromMember(author, customRole.role!!).complete()
            channel.doSend(getString("getRoleRemoved", author.asMention, customRole.role!!.name))
        }
    }

    override fun getCommandWords(): List<String> {
        return listOf("getrole", "role")
    }
}