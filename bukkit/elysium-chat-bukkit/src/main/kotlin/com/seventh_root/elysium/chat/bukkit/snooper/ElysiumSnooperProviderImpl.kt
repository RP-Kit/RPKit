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

package com.seventh_root.elysium.chat.bukkit.snooper

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.database.table.ElysiumSnooperTable
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer

/**
 * Snooper provider implementation.
 */
class ElysiumSnooperProviderImpl(private val plugin: ElysiumChatBukkit): ElysiumSnooperProvider {

    override val snoopers: List<ElysiumPlayer>
        get() = plugin.core.database.getTable(ElysiumSnooperTable::class).getAll().map { snooper -> snooper.player }

    override fun addSnooper(player: ElysiumPlayer) {
        plugin.core.database.getTable(ElysiumSnooperTable::class).insert(ElysiumSnooper(player = player))
    }

    override fun removeSnooper(player: ElysiumPlayer) {
        val snooperTable = plugin.core.database.getTable(ElysiumSnooperTable::class)
        val snooper = snooperTable.get(player)
        if (snooper != null) {
            snooperTable.delete(snooper)
        }
    }

    override fun isSnooping(player: ElysiumPlayer): Boolean {
        val snooperTable = plugin.core.database.getTable(ElysiumSnooperTable::class)
        return snooperTable.get(player) != null
    }

}