package me.samboycoding.sambotv6.commands

import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.findOne
import me.samboycoding.sambotv6.getCommandData
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import me.samboycoding.sambotv6.orm.tables.CustomRoles
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

class GetRoleCommand : BaseCommand() {
    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        //Needs to be enabled
        if (!config.customRolesEnabled)
            return msg.sendModuleDisabledMessage(getString("moduleNameCustomRoles"))

        //We need manage roles
        if(!failIfBotMissingPerm(Permission.MANAGE_ROLES))
            return

        val data = msg.getCommandData(config)

        //Need a role name
        if (data.argsCount < 1)
            return channel.doSend(getString("getRoleMissingRoleName", author.asMention))

        //Get role name - remainder
        val roleName = data.getArg(0)!!.toLowerCase()

        //Lookup in DB or bailout
        val customRole = CustomRoles.findOne { it.id eq roleName }
            ?: return channel.doSend(getString("getRoleUnknownRole", author.asMention, roleName))

        //Ensure the actual role exists or bailout
        if (customRole.role == null) return channel.doSend(getString("getRoleRoleDisappeared", author.asMention, roleName))

        if(!author.roles.contains(customRole.role)) {
            //Don't have it, add it
            guild.addRoleToMember(author, customRole.role!!).complete()
            channel.doSend(getString("getRoleAdded", author.asMention, customRole.role!!.name))
        } else {
            //Have it, remove.
            guild.removeRoleFromMember(author, customRole.role!!).complete()
            channel.doSend(getString("getRoleRemoved", author.asMention, customRole.role!!.name))
        }
    }

    override fun getCommandWords(): List<String> {
        return listOf("getrole", "role")
    }
}