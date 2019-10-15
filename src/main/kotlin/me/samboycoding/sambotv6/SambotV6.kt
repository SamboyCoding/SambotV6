package me.samboycoding.sambotv6

import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import com.google.gson.GsonBuilder
import me.liuwj.ktorm.database.Database
import me.liuwj.ktorm.dsl.eq
import me.liuwj.ktorm.entity.findOne
import me.samboycoding.sambotv6.orm.tables.GuildConfigurations
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.reflections.Reflections
import org.reflections.scanners.ResourcesScanner
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.System.getenv
import java.util.*
import kotlin.system.exitProcess

class SambotV6 {
    lateinit var token: String
    lateinit var jda: JDA
    lateinit var db: Database

    val locales: TreeMap<String, TreeMap<String, String>> = TreeMap()

    private val joinLeaveMentionRegex = "(?<!<)@".toRegex()
    private val joinLeaveNameRegex = "(?<!<)%".toRegex()
    private val joinLeaveCountRegex = "(?<!<)\\*".toRegex()

    fun init(token: String) {
        this.token = token

        val manager = ReactiveEventManager()

        manager.on<ReadyEvent>()
            .next()
            .subscribe(this::onReady)

        manager.on<MessageReceivedEvent>()
            .subscribe(CommandHandler::handleEvent)

        manager.on<GuildMemberJoinEvent>()
            .subscribe { event ->
                val guild = event.guild
                val user = event.member

                //Get config
                val config = GuildConfigurations.findOne { it.id eq guild.id } ?: return@subscribe

                //Handle join roles
                config.joinRoles?.forEach { guild.addRoleToMember(user, it).submit() }

                //Handle join messages
                if (config.joinMessage.isNotEmpty())
                    config.joinChannel?.sendMessage(
                        config.joinMessage
                            .replace(joinLeaveMentionRegex, user.asMention)
                            .replace(joinLeaveNameRegex, user.effectiveName)
                            .replace(joinLeaveCountRegex, guild.members.size.toString())
                    )?.submit()
            }

        manager.on<GuildMemberLeaveEvent>()
            .subscribe {event ->
                val guild = event.guild
                val user = event.member

                //Get config
                val config = GuildConfigurations.findOne { it.id eq guild.id } ?: return@subscribe

                //Handle join messages
                if (config.leaveMessage.isNotEmpty())
                    config.joinChannel?.sendMessage(
                        config.leaveMessage
                            .replace(joinLeaveMentionRegex, user.asMention)
                            .replace(joinLeaveNameRegex, user.effectiveName)
                            .replace(joinLeaveCountRegex, guild.members.size.toString())
                    )?.submit()
            }

        manager.on<ShutdownEvent>()
            .subscribe { botLogger.info("Shutting down...") }

        botLogger.info("Logging in to discord...")
        jda = JDABuilder(token)
            .setEventManager(manager)
            .build()

        dbLogger.info("Connecting...")
        db = Database.connect(
            url = "jdbc:mysql://${getenv("DB_HOST") ?: ""}/sambotv6",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "sambotv6",
            password = getenv("DB_PASSWORD") ?: ""
        )
        dbLogger.info("Connected!")
    }

    private fun onReady(e: ReadyEvent) {
        botLogger.info("Ready! I'm in ${e.guildTotalCount} guilds, of which ${e.guildUnavailableCount} are not available")
        botLogger.info(jda.guilds.joinToString(",") { "\"${it.id}\"" })

        //Load locales
        val gson = GsonBuilder().setLenient().create()

        botLogger.info("Loading locales...")
        val reflections = Reflections("i18n", ResourcesScanner())
        reflections.getResources { true }.forEach { fileName ->
            botLogger.info("Found lang file $fileName, reading...")

            getResourceAsStream(fileName).use {
                val content = BufferedReader(InputStreamReader(it!!)).readText()

                @Suppress("UNCHECKED_CAST")
                val map: TreeMap<String, String> =
                    gson.fromJson(content, TreeMap::class.java) as TreeMap<String, String>

                val localeName = fileName.removePrefix("i18n/").removeSuffix(".json")
                locales[localeName] = map
                botLogger.info("Loaded ${map.size} translations for language $localeName")
            }
        }

        if(locales.isEmpty()) {
            botLogger.error("Failed to load locales")
            exitProcess(1)
        }

        val en = locales["en"]!!
        for (kvp in locales) {
            if (kvp.key == "en") continue

            val locale = kvp.value

            val missingKeys = en.keys.filter { !locale.containsKey(it) }

            if (missingKeys.isNotEmpty()) {
                botLogger.warn("Locale ${kvp.key} is missing ${missingKeys.size} translations:")

                missingKeys.forEach { missingKey -> botLogger.warn("    -$missingKey") }

                botLogger.warn("")
            }
        }

        botLogger.info("There are ${CommandHandler.commands.count()} commands.")
    }

    @Throws(IOException::class)
    fun getResourceFiles(path: String): List<String> = getResourceAsStream(path)?.use {
        return BufferedReader(InputStreamReader(it)).readLines()
    } ?: emptyList()

    private fun getResourceAsStream(resource: String): InputStream? =
        Thread.currentThread().contextClassLoader.getResourceAsStream(resource)
            ?: this::class.java.getResourceAsStream(resource)

    companion object {
        val botLogger: Logger = SimpleLoggerFactory().getLogger("Bot")
        internal val dbLogger: Logger = SimpleLoggerFactory().getLogger("Database")
        val instance = SambotV6()
    }
}

fun main() {
    val token = getenv("DISCORD_TOKEN") ?: throw AssertionError("Discord token is null")

    SambotV6.instance.init(token)
}