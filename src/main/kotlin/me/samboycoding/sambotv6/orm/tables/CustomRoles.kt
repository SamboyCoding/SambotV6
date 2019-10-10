package me.samboycoding.sambotv6.orm.tables

import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.varchar
import me.samboycoding.sambotv6.orm.entities.CustomRole

object CustomRoles : Table<CustomRole>("CustomRoles") {
    val id by varchar("id").primaryKey().bindTo { it.slug } //VARCHAR(100) PRIMARY KEY
    val roleId by varchar("roleId").bindTo { it.roleId } //VARCHAR(32)
    val guild by varchar("guildId").references(GuildConfigurations) { it.guildConfig } //VARCHAR(32) FOREIGN KEY GuildConfigurations:GUILD_ID
}