/*
 * Copyright 2016 Ross Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.core.bukkit.util

import org.bukkit.ChatColor

import java.awt.Color

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

/**
 * Gets the closest Bukkit chat color to the color.
 */
fun Color.closestChatColor(): ChatColor {
    var minDistSquared = java.lang.Double.MAX_VALUE
    var closest: ChatColor? = null
    for (chatColor in ChatColor.values()) {
        val chatColorColor = chatColor.toColor()
        if (chatColorColor != null) {
            val distSquared = Math.pow((red - chatColorColor.red).toDouble(), 2.0) + Math.pow((blue - chatColorColor.blue).toDouble(), 2.0) + Math.pow((green - chatColorColor.green).toDouble(), 2.0)
            if (distSquared < minDistSquared) {
                minDistSquared = distSquared
                closest = chatColor
            }
        }
    }
    return closest!!
}

/**
 * Gets the color of this chat color.
 */
fun ChatColor.toColor(): Color? {
    when (this) {
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
