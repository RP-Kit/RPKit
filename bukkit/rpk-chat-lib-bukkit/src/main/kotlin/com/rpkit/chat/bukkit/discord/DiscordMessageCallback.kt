package com.rpkit.chat.bukkit.discord

fun interface DiscordMessageCallback {
    operator fun invoke(discordMessage: DiscordMessage)
}