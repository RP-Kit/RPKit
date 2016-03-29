package com.seventh_root.elysium.core.bukkit.util

import org.bukkit.ChatColor

import java.awt.Color

object ChatColorUtils {

    private val BLACK = Color(0, 0, 0)
    private val DARK_BLUE = Color(0, 0, 170)
    private val DARK_GREEN = Color(0, 170, 0)
    private val DARK_AQUA = Color(0, 170, 170)
    private val DARK_RED = Color(170, 0, 0)
    private val DARK_PURPLE = Color(170, 0, 170)
    private val GOLD = Color(255, 170, 0)
    private val GRAY = Color(170, 170, 170)
    private val DARK_GRAY = Color(85, 85, 85)
    private val BLUE = Color(85, 85, 255)
    private val GREEN = Color(85, 255, 85)
    private val AQUA = Color(85, 255, 255)
    private val RED = Color(255, 85, 85)
    private val LIGHT_PURPLE = Color(255, 85, 255)
    private val YELLOW = Color(255, 255, 85)
    private val WHITE = Color(255, 255, 255)

    fun closestChatColorToColor(color: Color): ChatColor {
        var minDistSquared = java.lang.Double.MAX_VALUE
        var closest: ChatColor? = null
        for (chatColor in ChatColor.values()) {
            val chatColorColor = colorFromChatColor(chatColor)
            if (chatColorColor != null) {
                val distSquared = Math.pow((color.red - chatColorColor.red).toDouble(), 2.0) + Math.pow((color.blue - chatColorColor.blue).toDouble(), 2.0) + Math.pow((color.green - chatColorColor.green).toDouble(), 2.0)
                if (distSquared < minDistSquared) {
                    minDistSquared = distSquared
                    closest = chatColor
                }
            }
        }
        return closest!!
    }

    fun colorFromChatColor(chatColor: ChatColor): Color? {
        when (chatColor) {
            ChatColor.BLACK -> return BLACK
            ChatColor.DARK_BLUE -> return DARK_BLUE
            ChatColor.DARK_GREEN -> return DARK_GREEN
            ChatColor.DARK_AQUA -> return DARK_AQUA
            ChatColor.DARK_RED -> return DARK_RED
            ChatColor.DARK_PURPLE -> return DARK_PURPLE
            ChatColor.GOLD -> return GOLD
            ChatColor.GRAY -> return GRAY
            ChatColor.DARK_GRAY -> return DARK_GRAY
            ChatColor.BLUE -> return BLUE
            ChatColor.GREEN -> return GREEN
            ChatColor.AQUA -> return AQUA
            ChatColor.RED -> return RED
            ChatColor.LIGHT_PURPLE -> return LIGHT_PURPLE
            ChatColor.YELLOW -> return YELLOW
            ChatColor.WHITE -> return WHITE
            ChatColor.MAGIC, ChatColor.BOLD, ChatColor.STRIKETHROUGH, ChatColor.UNDERLINE, ChatColor.ITALIC, ChatColor.RESET -> return null
            else -> return null
        }
    }

}
