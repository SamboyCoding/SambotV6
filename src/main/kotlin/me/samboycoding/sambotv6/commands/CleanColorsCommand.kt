package me.samboycoding.sambotv6.commands

import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*

class CleanColorsCommand : BaseCommand() {
    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        //Check that the feature's enabled
        if (!config.colorsEnabled)
            return msg.sendModuleDisabledMessage("custom colors")

        //Check we can manage roles
        if (!failIfBotMissingPerm(Permission.MANAGE_ROLES))
            return

        //Count the number of roles initially in the server
        val initialCount = guild.roles.size

        //Find all color roles
        val probableColorRoles =
            msg.guild.roles.filter { it.name.length == 7 && it.name.startsWith("#") && it.colorRaw != Role.DEFAULT_COLOR_RAW }

        //Find those with no users assigned
        val unused = probableColorRoles.filter { it.guild.getMembersWithRoles(it).isEmpty() }

        if (unused.isEmpty())
            return channel.doSend(getString("cleanColorsNoUnused", author.asMention))

        //Take the first 50 (or all if n <= 50)
        val toRemove = unused.take(50)

        channel.doSend(getString("cleanColorsInProgress"))

        //Remove all
        toRemove.forEach { it.delete().reason("Colour role cleanup").complete() }

        //If we removed all, we're done
        if(toRemove.size == unused.size)
            return channel.doSend(getString("cleanColorsComplete", author.asMention, initialCount, unused.size))

        //Otherwise, more to remove
        channel.doSend(getString("cleanColorsHasMore", author.asMention, initialCount, unused.size))
    }

    override fun getCommandWords(): List<String> {
        return listOf("cleancolors", "cleancolours")
    }
}