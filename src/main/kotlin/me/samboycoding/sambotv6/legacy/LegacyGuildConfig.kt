package me.samboycoding.sambotv6.legacy

class LegacyGuildConfig {
    lateinit var guild: String
    lateinit var colors: Colors
    lateinit var customroles: CustomRoles
    lateinit var general: General
    lateinit var userjoin: UserJoin

    class Colors {
        var enabled: Boolean = false
    }

    class CustomRoles {
        var roleList: Map<String, String> = HashMap()
    }

    class General {
        var prefix: String = ""
    }

    class UserJoin {
        var autoRoles = arrayOf("")
        var channelId: String = ""
        var greeting: String = ""
        var goodbye: String = ""
    }
}