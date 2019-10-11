package me.samboycoding.sambotv6.commands

import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.findOne
import me.samboycoding.sambotv6.SambotV6
import me.samboycoding.sambotv6.getCommandData
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import me.samboycoding.sambotv6.orm.tables.ChannelLocales
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*

abstract class BaseCommand {
    private lateinit var context: CommandData
    lateinit var config: GuildConfiguration

    protected open fun getCommandWord(): String {
        throw NotImplementedError("Command must either override getCommandWord or override getCommandWords to not call getCommandWord.")
    }

    open fun getCommandWords(): List<String> {
        return arrayListOf(getCommandWord())
    }

    protected abstract fun execute(
        msg: Message,
        author: Member,
        guild: Guild,
        channel: TextChannel,
        config: GuildConfiguration
    )

    fun doExecute(msg: Message, author: Member, guild: Guild, channel: TextChannel, config: GuildConfiguration) {
        synchronized(this) {
            context = msg.getCommandData(config)
            this.config = config
            execute(msg, author, guild, channel, config)
        }
    }

    fun getString(key: String): String {
        //Channel override if exists else guild config locale
        val localeId = ChannelLocales.findOne { it.id eq context.message.channel.id }?.locale ?: config.locale

        return SambotV6.instance.locales[localeId]?.get(key)
            ?: SambotV6.instance.locales["en"]!![key]
            ?: throw NotImplementedError("Could not find string $key in default or fallback locale!")
    }

    protected fun getString(key: String, vararg objects: Any): String {
        val unformatted = getString(key)
        return unformatted.format(*objects)
    }

    protected fun Message.sendModuleDisabledMessage(module: String) {
        channel.doSend(getString("moduleDisabled", author.asMention, module))
//        channel.sendMessage("⛔ Sorry, ${author.asMention}, but the `$module` module is disabled in this server, so you can't do that! ⛔")
    }

    protected fun Message.sendMissingPermissionMessage() {
        channel.doSend(getString("missingPermission", author.asMention))
    }

    protected fun failIfBotMissingPerm(perm: Permission): Boolean {
        if (!config.guild!!.selfMember.hasPermission(perm)) {
            context.message.sendMisconfigurationMessage(perm.name)
            return false
        }

        return true
    }


    private fun Message.sendMisconfigurationMessage(missing: String) {
        channel.doSend(getString("botMissingPermission", missing))
    }

    protected fun MessageChannel.doSend(message: String) {
        sendMessage(message).queue()
    }
}