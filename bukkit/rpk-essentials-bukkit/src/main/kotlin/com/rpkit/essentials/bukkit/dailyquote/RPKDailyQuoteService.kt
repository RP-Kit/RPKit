/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.essentials.bukkit.dailyquote

import com.rpkit.dailyquote.bukkit.dailyquote.RPKDailyQuoteService
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.ChatColor
import java.util.*


class RPKDailyQuoteServiceImpl(override val plugin: RPKEssentialsBukkit) : RPKDailyQuoteService {

    override fun getDailyQuote(): String {
        var i = 0
        for (j in 0 until Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) {
            i++
            if (i > plugin.config.getStringList("daily-messages").size - 1) {
                i = 0
            }
        }
        return ChatColor.translateAlternateColorCodes('&', plugin.config.getStringList("daily-messages")[i])
    }

}