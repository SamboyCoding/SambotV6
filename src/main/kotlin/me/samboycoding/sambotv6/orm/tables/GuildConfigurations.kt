package me.samboycoding.sambotv6.orm.tables

import me.liuwj.ktorm.entity.add
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.boolean
import me.liuwj.ktorm.schema.varchar
import me.samboycoding.sambotv6.SambotV6
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.entities.Guild

object GuildConfigurations : Table<GuildConfiguration>("GuildConfig") {
    val id by varchar("GUILD_ID").primaryKey().bindTo { it.id } //VARCHAR(32) PRIMARY KEY
    val colorsEnabled by boolean("COLORS_ENABLED").bindTo { it.colorsEnabled } //BOOLEAN
    val customRolesEnabled by boolean("CUSTOMROLES_ENABLED").bindTo { it.customRolesEnabled } //BOOLEAN
    val prefix by varchar("COMMAND_PREFIX").bindTo { it.prefix } //VARCHAR(100)
    val joinMessage by varchar("USERJOIN_MESSAGE").bindTo { it.joinMessage } //VARCHAR(500)
    val joinChannel by varchar("USERJOIN_CHANNEL_ID").bindTo { it.joinMessageChannelId } //VARCHAR(32)
    val leaveMessage by varchar("USERLEAVE_MESSAGE").bindTo { it.leaveMessage } //VARCHAR(500)
    val autoRoles by varchar("USERJOIN_ROLES").bindTo { it.joinRoleIds } //VARCHAR(2048)
    val locale by varchar("LOCALE").bindTo { it.locale } //VARCHAR(4)

    fun makeDefault(guild: Guild): GuildConfiguration {
        SambotV6.dbLogger.info("Creating default config for guild ${guild.name}/${guild.id}")

        return makeDefault(guild.id)
    }

    fun makeDefault(guildId: String): GuildConfiguration {
        val newCfg = GuildConfiguration {
            id = guildId
            colorsEnabled = false
            customRolesEnabled = false
            prefix = "?"
            joinMessage = ""
            joinChannel = null
            leaveMessage = ""
            joinRoles = null
            locale = "en"
        }

        add(newCfg)
        return newCfg
    }
}