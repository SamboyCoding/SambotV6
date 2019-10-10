package me.samboycoding.sambotv6

import club.minnced.jda.reactor.ReactiveEventManager
import club.minnced.jda.reactor.on
import com.google.gson.GsonBuilder
import me.liuwj.ktorm.database.Database
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.ShutdownEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.simple.SimpleLoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.System.getenv

class SambotV6 {
    lateinit var token: String
    lateinit var jda: JDA
    lateinit var db: Database

    val locales: HashMap<String, HashMap<String, String>> = HashMap()

    fun init(token: String) {
        this.token = token

        val manager = ReactiveEventManager()

        manager.on<ReadyEvent>()
            .next()
            .subscribe(this::onReady)

        manager.on<MessageReceivedEvent>()
            .subscribe(CommandHandler::handleEvent)

        manager.on<ShutdownEvent>()
            .subscribe {botLogger.info("Shutting down...")}

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

    private fun onReady(event: ReadyEvent) {
        botLogger.info("Ready!")

        //Load locales
        val gson = GsonBuilder().setLenient().create()

        botLogger.info("Loading locales...")
        getResourceFiles("i18n/").forEach { fileName ->
            botLogger.info("Found lang file $fileName, reading...")

            getResourceAsStream("i18n/$fileName").use {
                val content = BufferedReader(InputStreamReader(it!!)).readText()

                @Suppress("UNCHECKED_CAST")
                val map: HashMap<String, String> =
                    gson.fromJson(content, HashMap::class.java) as HashMap<String, String>

                val localeName = fileName.removeSuffix(".json")
                locales[localeName] = map
                botLogger.info("Loaded ${map.size} translations for language $localeName")
            }

            val en = locales["en"]!!
            for (kvp in locales) {
                if(kvp.key == "en") continue

                val locale = kvp.value

                val missingKeys = arrayListOf<String>()
                for (string in en) {
                    if(!locale.containsKey(string.key))
                        missingKeys.add(string.key)
                }

                if(missingKeys.isNotEmpty()) {
                    botLogger.warn("Locale ${kvp.key} is missing ${missingKeys.size} translations:")

                    missingKeys.forEach { botLogger.warn("    -$it")}

                    botLogger.warn("")
                }
            }
        }

        botLogger.info("There are ${CommandHandler.commands.count()} commands.")
    }

    @Throws(IOException::class)
    fun getResourceFiles(path: String): List<String> = getResourceAsStream(path).use {
        return if (it == null) emptyList()
        else BufferedReader(InputStreamReader(it)).readLines()
    }

    private fun getResourceAsStream(resource: String): InputStream? =
        Thread.currentThread().contextClassLoader.getResourceAsStream(resource)
            ?: resource::class.java.getResourceAsStream(resource)

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