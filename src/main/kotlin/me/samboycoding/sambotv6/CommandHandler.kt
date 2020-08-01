package me.samboycoding.sambotv6

import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.find
import me.samboycoding.sambotv6.commands.*
import me.samboycoding.sambotv6.orm.tables.GuildConfigurations
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

object CommandHandler {
    val commands = arrayListOf(
        //Prefix
        SetPrefixCommand(),
        //Channel, module
        ChannelCommand(),
        ModuleCommand(),
        //Color
        SetColorCommand(),
        ClearColorCommand(),
        CleanColorsCommand(),
        //Util
        ClearCommand(),
        //Music
        JoinCommand(),
        LeaveCommand(),
        //Lang
        ServerLangCommand(),
        ChannelLangCommand(),
        //Custom Roles
        GetRoleCommand(),
        AddCustomRoleCommand()
    )

    fun handleEvent(event: MessageReceivedEvent) {
        val msg = event.message
        val guild = msg.guild
        val config = SambotV6.instance.db.guildConfigurations.find { it.id eq guild.id } ?: GuildConfigurations.makeDefault(guild)

        if(!msg.contentRaw.startsWith(config.prefix)) return

        val data = msg.getCommandData(config)

        SambotV6.botLogger.info("Received command ${data.commandExecuted}")

        //Find command
        val cmd = commands.find { it.getCommandWords().contains(data.commandExecuted) }

        //Execute if non-null
        try {
            cmd?.doExecute(msg, msg.member!!, guild, msg.textChannel, config)
        } catch (e: Exception) {
            SambotV6.botLogger.error("Exception executing command:", e)
            try {
                msg.channel.sendMessage(cmd!!.getString("exceptionExecutingCommand")).submit()
            } catch (e: Exception) {
                //Ignore, possible outage or something
            }
        }
    }
}