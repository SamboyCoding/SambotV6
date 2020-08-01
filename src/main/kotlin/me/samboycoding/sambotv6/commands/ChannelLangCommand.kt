package me.samboycoding.sambotv6.commands

import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.find
import me.samboycoding.sambotv6.SambotV6
import me.samboycoding.sambotv6.channelLocales
import me.samboycoding.sambotv6.getCommandData
import me.samboycoding.sambotv6.isModerator
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import me.samboycoding.sambotv6.orm.tables.ChannelLocales
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel

class ChannelLangCommand : BaseCommand() {
    override fun execute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        //They have to be a mod
        if (!author.isModerator())
            return msg.sendMissingPermissionMessage()

        val data = msg.getCommandData(config)

        val existingOverride = SambotV6.instance.db.channelLocales.find { it.id eq channel.id }

        val currentLocale = existingOverride?.locale ?: config.locale

        //We need a locale
        if (data.argsCount < 1) {
            val locales =
                SambotV6.instance.locales.keys.joinToString("\n") { "${if (currentLocale == it) '*' else ' '}   $it: ${SambotV6.instance.locales[it]!!["name"]} (${SambotV6.instance.locales[it]!!["englishName"]})" }
            msg.delete().submit()
            return channel.doSend(getString("setCLocaleMissingLocale", author.asMention, locales))
        }

        val locale = data.getArg(0)!!.toLowerCase()

        if (!SambotV6.instance.locales.containsKey(locale)) {
            msg.delete().submit()
            return channel.doSend(getString("setLocaleUnknownLocale", author.asMention, locale))
        }

        //Are we setting back to server-wide?
        if(existingOverride != null && locale == config.locale) {
            //If so, just delete the override
            existingOverride.delete()
        } else {
            //Set channel override language
            val override = existingOverride
                ?: ChannelLocales.createForChannel(channel, locale)

            override.locale = locale
            override.flushChanges()
        }

        msg.delete().submit()
        channel.doSend(getString("cLanguageUpdated", author.asMention, currentLocale, locale))
    }

    override fun getCommandWords(): List<String> {
        return listOf("channellang", "clang", "channellocale", "clocale")
    }
}