package me.samboycoding.sambotv6.legacy

import com.google.gson.GsonBuilder
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.findOne
import me.samboycoding.sambotv6.orm.tables.CustomRoles
import me.samboycoding.sambotv6.orm.tables.GuildConfigurations
import java.nio.file.Files
import java.nio.file.Path

fun main(args: Array<String>) {
    val path = args[0]
    val content = Files.readString(Path.of(path))
    val csvArray = content.split("\n").map { it.split(',', limit = 2) }

    println("read content")

    val db = Database.connect(
        url = "jdbc:mysql://${System.getenv("DB_HOST") ?: ""}/sambotv6",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "sambotv6",
        password = System.getenv("DB_PASSWORD") ?: ""
    )

    val gson = GsonBuilder().create()

    for (row in csvArray) {
        val guildId = row[0].removePrefix("\"").removeSuffix("\"")
        val json = row[1].removePrefix("\"").removeSuffix("\"\r").replace("\"\"", "\"")

        println("Processing guild $guildId: $json")

        val legacy = gson.fromJson<LegacyGuildConfig>(json, LegacyGuildConfig::class.java)


        val cfg = GuildConfigurations.findOne { it.id eq guildId } ?: GuildConfigurations.makeDefault(guildId)

        cfg.prefix = legacy.general.prefix

        cfg.colorsEnabled = legacy.colors.enabled

        cfg.customRolesEnabled = legacy.customroles.roleList.isNotEmpty()

        cfg.joinMessageChannelId = legacy.userjoin.channelId
        cfg.joinMessage = legacy.userjoin.greeting
        cfg.leaveMessage = legacy.userjoin.goodbye
        cfg.joinRoleIds = legacy.userjoin.autoRoles.joinToString("|")

        //Create custom roles
        legacy.customroles.roleList.entries.forEach { CustomRoles.createForRole(it.value, it.key, cfg) }

        cfg.flushChanges()
    }
}