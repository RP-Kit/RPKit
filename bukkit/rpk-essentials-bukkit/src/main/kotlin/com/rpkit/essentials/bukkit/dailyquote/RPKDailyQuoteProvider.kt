package com.rpkit.essentials.bukkit.dailyquote

import com.rpkit.dailyquote.bukkit.dailyquote.RPKDailyQuoteProvider
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.ChatColor
import java.util.*


class RPKDailyQuoteProviderImpl(private val plugin: RPKEssentialsBukkit): RPKDailyQuoteProvider {

    override fun getDailyQuote(): String {
        var i = 0
        for (j in 0..Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - 1) {
            i++
            if (i > plugin.config.getStringList("daily-messages").size - 1) {
                i = 0
            }
        }
        return ChatColor.translateAlternateColorCodes('&', plugin.config.getStringList("daily-messages")[i])
    }

}