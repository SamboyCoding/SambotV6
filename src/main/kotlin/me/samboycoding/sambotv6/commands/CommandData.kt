package me.samboycoding.sambotv6.commands

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message

class CommandData(
    withoutPrefix: String,
    val guild: Guild,
    val message: Message
) {
    val commandExecuted = withoutPrefix.substringBefore(" ").toLowerCase()
    private val args = if(withoutPrefix.contains(" ")) withoutPrefix.substringAfter(" ").split(" ") else listOf()

    val argsCount
        get() = args.size

    fun getArg(idx: Int, remainder: Boolean = false): String? {
        if (argsCount <= idx) return null

        if(!remainder) return args[idx]

        return args.slice(idx until argsCount).joinToString(" ")
    }

    fun getIntArg(idx: Int) = getArg(idx)?.toIntOrNull() ?: -1

    fun getUserArg(idx: Int, remainder: Boolean): Member? {
        var raw = getArg(idx)?.toLowerCase() ?: return null
        if (raw.isBlank()) return null

        //Case 1 - a direct tag (@id)
        val match = userMentionRegex.find(raw);
        if (match != null)
            return guild.getMemberById(match.groups[1]!!.value)

        //Case 2 - just an id
        if(raw.toLongOrNull() != null) {
            val mem = guild.getMemberById(raw)
            if (mem != null)
                return mem
        }

        //If we get to searching by username and remainder is set, we need to include the rest of the args
        if(remainder)
            raw = getArg(idx, remainder)?.toLowerCase() ?: return null

        //Case 3 - by username or nickname
        var members = guild.getMembersByEffectiveName(raw, true)
        if(members.isEmpty())
            members = guild.getMembersByName(raw, true)

        if(members.isNotEmpty())
            return members[0]

        //Case 4 - partial username
        //If this finds no matches it returns null, which is fine, as this is the last case
        return guild.members.toList()
            .find { it.effectiveName.toLowerCase().startsWith(raw) }
    }

    companion object {
        val userMentionRegex = Message.MentionType.USER.pattern.pattern().toRegex()
    }
}