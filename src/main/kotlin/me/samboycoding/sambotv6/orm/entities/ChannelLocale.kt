package me.samboycoding.sambotv6.orm.entities

import me.liuwj.ktorm.entity.Entity
import me.samboycoding.sambotv6.SambotV6

interface ChannelLocale : Entity<ChannelLocale> {
    companion object : Entity.Factory<ChannelLocale>()

    var channelId: String
    var locale: String
    var guildConfig: GuildConfiguration

    var channel
        get() = SambotV6.instance.jda.getTextChannelById(channelId)
        set(value) {
            channelId = value?.id ?: ""
        }
}