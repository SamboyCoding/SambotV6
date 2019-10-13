package me.samboycoding.sambotv6.commands

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.Role

class CommandData(
    withoutPrefix: String,
    val guild: Guild,
    val message: Message
) {
    val commandExecuted = withoutPrefix.substringBefore(" ").toLowerCase()
    private val args = if (withoutPrefix.contains(" ")) withoutPrefix.substringAfter(" ").split(" ") else listOf()

    val argsCount
        get() = args.size

    fun getArg(idx: Int, remainder: Boolean = false): String? {
        if (argsCount <= idx) return null

        if (!remainder) return args[idx]

        return args.slice(idx until argsCount).joinToString(" ")
    }

    fun getIntArg(idx: Int) = getArg(idx)?.toIntOrNull() ?: -1

    fun getUserArg(idx: Int, remainder: Boolean): Member? {
        var raw = getArg(idx)?.toLowerCase() ?: return null
        if (raw.isBlank()) return null

        //Case 1 - a direct tag (<@!id>)
        val match = userMentionRegex.find(raw);
        if (match != null)
            return guild.getMemberById(match.groups[1]!!.value)

        //Case 2 - just an id
        if (raw.toLongOrNull() != null) {
            val mem = guild.getMemberById(raw)
            if (mem != null)
                return mem
        }

        //If we get to searching by username and remainder is set, we need to include the rest of the args
        if (remainder)
            raw = getArg(idx, remainder)?.toLowerCase() ?: return null

        //Case 3 - by username or nickname
        var members = guild.getMembersByEffectiveName(raw, true)
        if (members.isEmpty())
            members = guild.getMembersByName(raw, true)

        return members.singleOrNull() ?: guild.members.toList().singleOrNull {
            it.effectiveName.toLowerCase().startsWith(raw) //Case 4 - partial username
        }
    }

    fun getRoleArg(idx: Int, remainder: Boolean): Role? {
        var raw = getArg(idx)?.toLowerCase() ?: return null

        //Case 1 - a direct tag (<@&id>)
        val match = roleMentionRegex.find(raw);
        if (match != null)
            return guild.getRoleById(match.groups[1]!!.value)

        //Case 2 - just an id
        if (raw.toLongOrNull() != null) {
            val mem = guild.getRoleById(raw)
            if (mem != null)
                return mem
        }

        //If we get to searching by role name and remainder is set, we need to include the rest of the args
        if (remainder)
            raw = getArg(idx, remainder)?.toLowerCase() ?: return null

        //Case 3 - by name
        val roles = guild.getRolesByName(raw, true)

        return roles.singleOrNull() ?: guild.roles.singleOrNull {
            it.name.toLowerCase().startsWith(raw) // Case 4 - partial name
        }
    }

    companion object {
        val userMentionRegex = Message.MentionType.USER.pattern.pattern().toRegex()
        val roleMentionRegex = Message.MentionType.ROLE.pattern.pattern().toRegex()
    }
}