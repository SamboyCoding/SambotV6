package me.samboycoding.sambotv6.orm.tables

import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.add
import me.liuwj.ktorm.entity.find
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.varchar
import me.samboycoding.sambotv6.SambotV6
import me.samboycoding.sambotv6.channelLocales
import me.samboycoding.sambotv6.guildConfigurations
import me.samboycoding.sambotv6.orm.entities.ChannelLocale
import net.dv8tion.jda.api.entities.TextChannel

object ChannelLocales : Table<ChannelLocale>("ChannelLocales") {
    val id = varchar("id").primaryKey().bindTo { it.channelId } //VARCHAR(32) PRIMARY KEY
    val locale = varchar("locale").bindTo { it.locale } //VARCHAR(5)
    val guild = varchar("guildId").references(GuildConfigurations) { it.guildConfig } //VARCHAR(32) FOREIGN KEY GuildConfigurations:GUILD_ID

    fun createForChannel(channel: TextChannel, locale: String): ChannelLocale {
        val new = ChannelLocale {
            this.channel = channel
            this.locale = locale
            this.guildConfig = SambotV6.instance.db.guildConfigurations.find { it.id eq channel.guild.id }
                ?: GuildConfigurations.makeDefault(channel.guild)
        }

        SambotV6.instance.db.channelLocales.add(new)

        return new
    }
}