package me.samboycoding.sambotv6.orm.tables

import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.add
import me.liuwj.ktorm.entity.find
import me.liuwj.ktorm.schema.Table
import me.liuwj.ktorm.schema.int
import me.liuwj.ktorm.schema.varchar
import me.samboycoding.sambotv6.SambotV6
import me.samboycoding.sambotv6.customRoles
import me.samboycoding.sambotv6.guildConfigurations
import me.samboycoding.sambotv6.orm.entities.CustomRole
import me.samboycoding.sambotv6.orm.entities.GuildConfiguration
import net.dv8tion.jda.api.entities.Role

object CustomRoles : Table<CustomRole>("CustomRoles") {
    val actualId = int("idReal").primaryKey()
    val id = varchar("id").bindTo { it.tag } //VARCHAR(100)
    val roleId = varchar("roleId").bindTo { it.roleId } //VARCHAR(32)
    val guild = varchar("guildId").references(GuildConfigurations) { it.guildConfig } //VARCHAR(32) FOREIGN KEY GuildConfigurations:GUILD_ID

    fun createForRole(role: Role, tag: String): CustomRole {
        val new = CustomRole {
            this.role = role
            this.tag = tag
            this.guildConfig = SambotV6.instance.db.guildConfigurations.find { it.id eq role.guild.id }
                ?: GuildConfigurations.makeDefault(role.guild)
        }

        SambotV6.instance.db.customRoles.add(new)

        return new
    }

    fun createForRole(roleId: String, tag: String, config: GuildConfiguration): CustomRole {
        val new = CustomRole {
            this.roleId = roleId
            this.tag = tag
            this.guildConfig = config
        }

        SambotV6.instance.db.customRoles.add(new)

        return new
    }
}