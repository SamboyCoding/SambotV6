package me.samboycoding.sambotv6.commands

import me.samboycoding.sambotv6.getCommandData
import me.samboycoding.sambotv6.isModerator
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

class ClearColorCommand : BaseCommand() {
    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        //Check that the feature's enabled
        if (!config.colorsEnabled)
            return msg.sendModuleDisabledMessage("custom colors")

        //Check we can manage roles
        if (!failIfBotMissingPerm(Permission.MANAGE_ROLES))
            return

        val command = msg.getCommandData(config)

        //Verify the user to act upon
        var target: Member? = author
        if (command.argsCount > 0)
            target = command.getUserArg(0, true)

        if (target == null)
            return channel.doSend(getString("clearColorBadUser", author.asMention, command.getArg(0, true)!!))

        //Check for moderatorhood if the user's not modifying themselves
        if (target != author && !author.isModerator())
            return msg.sendMissingPermissionMessage()

        //Remove any color roles
        val presentRoles = target.roles.filter { r -> r.name.startsWith("#") }

        if(presentRoles.isEmpty()) return channel.doSend(getString("clearColorNoColors", target.asMention))

        presentRoles.forEach { r -> guild.removeRoleFromMember(target, r).queue() }

        //Delete their message
        msg.delete().queue()

        //Send success
        channel.doSend(getString("clearColorSuccess", target.asMention))
    }

    override fun getCommandWords(): List<String> {
        return listOf("clearcolor", "clearcolour")
    }
}