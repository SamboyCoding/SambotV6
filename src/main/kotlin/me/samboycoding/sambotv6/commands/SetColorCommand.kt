package me.samboycoding.sambotv6.commands

import me.samboycoding.sambotv6.getCommandData
import me.samboycoding.sambotv6.isModerator
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.exceptions.HierarchyException
import net.dv8tion.jda.api.requests.ErrorResponse
import java.awt.Color
import java.util.concurrent.ExecutionException

class SetColorCommand : BaseCommand() {
    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        //Check that the feature's enabled
        if (!config.colorsEnabled)
            return msg.sendModuleDisabledMessage(getString("moduleNameCustomColors"))

        //Check we can manage roles
        if (!failIfBotMissingPerm(Permission.MANAGE_ROLES))
            return

        val command = msg.getCommandData(config)

        //Check argument count
        if (command.argsCount < 1)
            return channel.doSend(getString("setColorMissingColor", author.asMention))

        //Verify the color the user wants
        val colorArg = command.getArg(0)!!
        val match = colorRegex.find(colorArg)
            ?: return channel.doSend(getString("setColorBadColor", author.asMention, colorArg))

        //Verify the user to act upon
        var target: Member? = author
        if (command.argsCount > 1)
            target = command.getUserArg(1, true)

        if (target == null)
            return channel.doSend(getString("setColorBadUser", author.asMention, command.getArg(1, true)!!))

        //Check for moderatorhood if the user's not modifying themselves
        if (target != author && !author.isModerator())
            return msg.sendMissingPermissionMessage()


        //Get the name of the role with a leading hash
        var roleName = match.value.toUpperCase()
        if (!roleName.startsWith("#")) roleName = "#$roleName"

        println("Looking for a role with name $roleName")

        //Check if the role already exists
        var role = guild.roles.find { r -> r.name == roleName }

        if (role == null) {
            println("No role found, creating a new one.")
            //Need to create
            try {
                role = guild.createRole()
                        .setColor(matchToColor(match))
                        .setName(roleName)
                        .setMentionable(false)
                        .setHoisted(false)
                        .setPermissions(0)
                        .submit()
                        .get()
            } catch(ere: ExecutionException) {
                if(ere.cause is ErrorResponseException) {
                    val message = when ((ere.cause as ErrorResponseException).errorResponse) {
                        ErrorResponse.MAX_ROLES_PER_GUILD -> getString("setColorNoRolesLeft", msg.author.asMention)
                        else -> getString("exceptionExecutingCommand")
                    }
                }
                return //Do not attempt to continue
            }
        } else
            println("Existing colour found, using role with ID ${role.id}")

        //Remove any pre-existing roles
        val presentRoles = target.roles.filter { r -> r.name.startsWith("#") }
        presentRoles.forEach { r ->
            try {
                guild.removeRoleFromMember(target, r).queue()
            } catch(e: HierarchyException) {
                channel.doSend(getString("setColorHierarchy", author.asMention, r.name))
                return@execute
            }
        }

        //Give the user the role
        guild.addRoleToMember(target, role!!).queue()

        //Delete their message
        msg.delete().queue()

        //Send success
        channel.doSend(getString("setColorSuccess", target.asMention, roleName))
    }

    private fun matchToColor(match: MatchResult): Color {
        val r = match.groups[1]!!.value.toInt(16)
        val g = match.groups[2]!!.value.toInt(16)
        val b = match.groups[3]!!.value.toInt(16)

        return Color(r, g, b)
    }

    override fun getCommandWords(): List<String> {
        return listOf("setcolor", "setcolour", "colorme", "colourme", "color", "colour")
    }

    companion object {
        val colorRegex = "#?([\\da-fA-F]{2})([\\da-fA-F]{2})([\\da-fA-F]{2})".toRegex()
    }
}