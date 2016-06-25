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

package com.seventh_root.elysium.players.bukkit.player

import com.seventh_root.elysium.core.database.Table
import com.seventh_root.elysium.players.bukkit.ElysiumPlayersBukkit
import com.seventh_root.elysium.players.bukkit.database.table.ElysiumPlayerTable
import org.bukkit.OfflinePlayer
import org.pircbotx.User

class ElysiumPlayerProviderImpl(private val plugin: ElysiumPlayersBukkit): ElysiumPlayerProvider {

    override fun getPlayer(id: Int): ElysiumPlayer? {
        return plugin.core.database.getTable(ElysiumPlayer::class.java)!![id]
    }

    override fun getPlayer(bukkitPlayer: OfflinePlayer): ElysiumPlayer {
        val table = plugin.core.database.getTable(ElysiumPlayer::class.java)
        var player = (table as ElysiumPlayerTable).get(bukkitPlayer)
        if (player == null) {
            player = ElysiumPlayerImpl(
                    name = bukkitPlayer.name,
                    bukkitPlayer = bukkitPlayer
            )
            addPlayer(player)
        }
        return player
    }

    override fun getPlayer(ircUser: User): ElysiumPlayer {
        val table = plugin.core.database.getTable(ElysiumPlayer::class.java)
        var player = (table as ElysiumPlayerTable).get(ircUser)
        if (player == null) {
            player = ElysiumPlayerImpl(
                    name = ircUser.nick,
                    ircNick = ircUser.nick
            )
            addPlayer(player)
        }
        return player
    }

    override fun addPlayer(player: ElysiumPlayer) {
        val bukkitPlayerTable: Table<ElysiumPlayer> = plugin.core.database.getTable(ElysiumPlayer::class.java) as Table<ElysiumPlayer>
        bukkitPlayerTable.insert(player)
    }

    override fun removePlayer(player: ElysiumPlayer) {
        plugin.core.database.getTable(ElysiumPlayer::class.java)!!.delete(player)
    }

}
