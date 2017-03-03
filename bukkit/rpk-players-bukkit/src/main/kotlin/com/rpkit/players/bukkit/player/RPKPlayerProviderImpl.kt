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

package com.rpkit.players.bukkit.player

import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKPlayerTable
import org.bukkit.OfflinePlayer
import org.pircbotx.User
import java.net.InetAddress

/**
 * Player provider implementation.
 */
class RPKPlayerProviderImpl(private val plugin: RPKPlayersBukkit): RPKPlayerProvider {

    override fun getPlayer(id: Int): RPKPlayer? {
        return plugin.core.database.getTable(RPKPlayerTable::class)[id]
    }

    override fun getPlayer(name: String): RPKPlayer? {
        return plugin.core.database.getTable(RPKPlayerTable::class).get(name)
    }

    override fun getPlayer(bukkitPlayer: OfflinePlayer): RPKPlayer {
        val table = plugin.core.database.getTable(RPKPlayerTable::class)
        var player = table.get(bukkitPlayer)
        if (player == null) {
            player = RPKPlayerImpl(
                    name = bukkitPlayer.name,
                    minecraftUUID = bukkitPlayer.uniqueId,
                    lastKnownIP = bukkitPlayer.player?.address?.address?.hostAddress
            )
            addPlayer(player)
        }
        return player
    }

    override fun getPlayer(ircUser: User): RPKPlayer {
        val table = plugin.core.database.getTable(RPKPlayerTable::class)
        var player = table.get(ircUser)
        if (player == null) {
            player = RPKPlayerImpl(
                    name = ircUser.nick,
                    ircNick = ircUser.nick
            )
            addPlayer(player)
        }
        return player
    }

    override fun getPlayer(lastKnownIP: InetAddress): RPKPlayer? {
        return plugin.core.database.getTable(RPKPlayerTable::class).get(lastKnownIP)
    }

    override fun addPlayer(player: RPKPlayer) {
        val table: Table<RPKPlayer> = plugin.core.database.getTable(RPKPlayerTable::class)
        table.insert(player)
    }

    override fun updatePlayer(player: RPKPlayer) {
        val table = plugin.core.database.getTable(RPKPlayerTable::class)
        table.update(player)
    }

    override fun removePlayer(player: RPKPlayer) {
        plugin.core.database.getTable(RPKPlayerTable::class).delete(player)
    }

}
