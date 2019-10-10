package me.samboycoding.sambotv6.orm.entities

import me.liuwj.ktorm.entity.Entity

interface CustomRole : Entity<CustomRole> {
    companion object : Entity.Factory<CustomRole>()

    val slug: String
    val roleId: String
    val guildConfig: GuildConfiguration

    val role
        get() = guildConfig.guild?.getRoleById(roleId)
}