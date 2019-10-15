package me.samboycoding.sambotv6.commands

import me.samboycoding.sambotv6.getCommandData
import me.samboycoding.sambotv6.isModerator
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

class ModuleCommand : BaseCommand() {
    override fun getCommandWord(): String {
        return "module"
    }

    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        //They need to be a mod
        if (!author.isModerator())
            return msg.sendMissingPermissionMessage()

        val data = msg.getCommandData(config)

        //Still on a zero-string diet
        if (data.argsCount < 1) {
            msg.addReaction("❓").submit()
            return
        }

        val enabled: Boolean

        when (data.getArg(0, true)?.toLowerCase()) {
            "color", "colour", "colors", "colours" -> {
                enabled = !config.colorsEnabled
                config.colorsEnabled = enabled
            }
            "role", "roles" -> {
                enabled = !config.customRolesEnabled
                config.customRolesEnabled = enabled
            }
            else -> {
                msg.addReaction("❓").submit()
                return
            }
        }

        config.flushChanges()
        msg.addReaction(if(enabled) "✅" else "❌").submit()
    }
}