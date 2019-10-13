package me.samboycoding.sambotv6.orm.entities

import me.liuwj.ktorm.entity.Entity

interface CustomRole : Entity<CustomRole> {
    companion object : Entity.Factory<CustomRole>()

    var tag: String
    var roleId: String
    var guildConfig: GuildConfiguration

    var role
        get() = guildConfig.guild?.getRoleById(roleId)
        set(value) {
            roleId = value?.id ?: ""
        }
}