package me.samboycoding.sambotv6.orm.tables

import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.add
import me.liuwj.ktorm.entity.findOne
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.varchar
import me.samboycoding.sambotv6.orm.entities.CustomRole
import net.dv8tion.jda.api.entities.Role

object CustomRoles : Table<CustomRole>("CustomRoles") {
    val id by varchar("id").primaryKey().bindTo { it.tag } //VARCHAR(100) PRIMARY KEY
    val roleId by varchar("roleId").bindTo { it.roleId } //VARCHAR(32)
    val guild by varchar("guildId").references(GuildConfigurations) { it.guildConfig } //VARCHAR(32) FOREIGN KEY GuildConfigurations:GUILD_ID

    fun createForRole(role: Role, tag: String): CustomRole {
        val new = CustomRole {
            this.role = role
            this.tag = tag
            this.guildConfig = GuildConfigurations.findOne { it.id eq role.guild.id }
                ?: GuildConfigurations.makeDefault(role.guild)
        }

        add(new)

        return new
    }
}