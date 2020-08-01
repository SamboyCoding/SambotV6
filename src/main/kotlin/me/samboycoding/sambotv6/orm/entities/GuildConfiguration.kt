package me.samboycoding.sambotv6.orm.entities

import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.Entity
import me.liuwj.ktorm.entity.filter
import me.liuwj.ktorm.entity.toList
import me.samboycoding.sambotv6.SambotV6
import me.samboycoding.sambotv6.customRoles

interface GuildConfiguration : Entity<GuildConfiguration> {
    companion object : Entity.Factory<GuildConfiguration>()

    var id: String
    var colorsEnabled: Boolean
    var customRolesEnabled: Boolean
    var prefix: String
    var joinMessage: String
    var leaveMessage: String
    var joinMessageChannelId: String
    var joinRoleIds: String
    var locale: String

    val guild
        get() = SambotV6.instance.jda.getGuildById(id)

    var joinChannel
        get() = guild?.getTextChannelById(joinMessageChannelId)
        set(value) { joinMessageChannelId = value?.id ?: "-1" }

    var joinRoles
        get() = if (guild != null) joinRoleIds.split("|").mapNotNull { guild?.getRoleById(it) }.toList() else null
        set(value) {
            //Map it -> it.id and join with pipes
            joinRoleIds = value?.joinToString("|") { it.id } ?: ""
        }

    val customRoles
        get() = SambotV6.instance.db.customRoles.filter { it.guild eq id }.toList()
}