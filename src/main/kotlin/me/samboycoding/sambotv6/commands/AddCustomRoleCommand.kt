package me.samboycoding.sambotv6.commands

import me.liuwj.ktorm.dsl.and
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.findOne
import me.samboycoding.sambotv6.getCommandData
import me.samboycoding.sambotv6.isModerator
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import me.samboycoding.sambotv6.orm.tables.CustomRoles
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

class AddCustomRoleCommand : BaseCommand() {
    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        //The feature needs to be enabled
        if (!config.customRolesEnabled)
            return msg.sendModuleDisabledMessage(getString("moduleNameCustomRoles"))

        //We need manage roles
        if (!failIfBotMissingPerm(Permission.MANAGE_ROLES))
            return

        //They need to be a mod
        if (!author.isModerator())
            return msg.sendMissingPermissionMessage()

        val data = msg.getCommandData(config)

        //Need at least a tag and a name
        if(data.argsCount < 2) {
            msg.delete().submit()
            return channel.doSend(getString("addCustomRoleMissingArgs", author.asMention))
        }

        //Get the tag (can't have spaces [that's why / because] it's first)
        val tag = data.getArg(0)!!

        //Check the tag isn't taken
        if(CustomRoles.findOne { (it.guild eq guild.id) and (it.id eq tag) } != null) {
            msg.delete().submit()
            return channel.doSend(getString("addCustomRoleTagTaken", author.asMention, tag))
        }

        //Check to see if they've @'d a role to use
        var role = data.getRoleArg(1, true)

        //If not, create one using the remainder arg idx 1
        if(role == null) {
            val roleName = data.getArg(1, true)
            role = guild.createRole()
                .setHoisted(false)
                .setMentionable(true)
                .setPermissions(0)
                .setName(roleName)
                .complete()
        }

        //Insert into the database with the role id
        CustomRoles.createForRole(role!!, tag)

        //Feedback to user
        msg.delete().submit()
        channel.doSend(getString("addCustomRoleSuccess", author.asMention, role.name, config.prefix, tag))
    }

    override fun getCommandWords(): List<String> {
        return listOf("addrole", "addcustomrole")
    }

}