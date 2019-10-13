package me.samboycoding.sambotv6.commands

import me.samboycoding.sambotv6.SambotV6
import me.samboycoding.sambotv6.getCommandData
import me.samboycoding.sambotv6.isModerator
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

class ServerLangCommand : BaseCommand() {
    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        //They have to be a mod
        if (!author.isModerator())
            return msg.sendMissingPermissionMessage()

        val data = msg.getCommandData(config)

        val currentLocale = config.locale

        //We need a locale
        if (data.argsCount < 1) {
            val locales =
                SambotV6.instance.locales.keys.joinToString("\n") { "${if (currentLocale == it) '*' else ' '}   $it: ${SambotV6.instance.locales[it]!!["name"]} (${SambotV6.instance.locales[it]!!["englishName"]})" }

            msg.delete().submit()
            return channel.doSend(getString("setLocaleMissingLocale", author.asMention, locales))
        }

        val locale = data.getArg(0)!!.toLowerCase()

        if (!SambotV6.instance.locales.containsKey(locale)) {
            msg.delete().submit()
            return channel.doSend(getString("setLocaleUnknownLocale", author.asMention, locale))
        }

        //Set server-wide language
        config.locale = locale
        config.flushChanges()

        //update the config so we use the correct lang for the string below
        super.config = config

        msg.delete().submit()
        channel.doSend(getString("languageUpdated", author.asMention, currentLocale, locale))
    }

    override fun getCommandWords(): List<String> {
        return listOf("lang", "serverlang", "locale", "serverlocale")
    }
}