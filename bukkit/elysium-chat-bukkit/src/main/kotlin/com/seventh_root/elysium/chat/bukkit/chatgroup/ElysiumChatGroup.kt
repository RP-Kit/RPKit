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

package com.seventh_root.elysium.chat.bukkit.chatgroup

import com.seventh_root.elysium.core.database.TableRow
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import java.util.*

class ElysiumChatGroup(
        override var id: Int = 0,
        var name: String,
        players: List<ElysiumPlayer>,
        invited: List<ElysiumPlayer>
): TableRow {

    private val players0 = mutableListOf<ElysiumPlayer>()
    val players: List<ElysiumPlayer>
        get() = Collections.unmodifiableList(players0)
    private val invited0 = mutableListOf<ElysiumPlayer>()
    val invited: List<ElysiumPlayer>
        get() = Collections.unmodifiableList(invited0)

    init {
        players0.addAll(players)
        invited0.addAll(invited)
    }

    fun addPlayer(player: ElysiumPlayer) {
        throw UnsupportedOperationException()
    }

    fun removePlayer(player: ElysiumPlayer) {
        throw UnsupportedOperationException()
    }

    fun invite(player: ElysiumPlayer) {
        throw UnsupportedOperationException()
    }

    fun uninvite(player: ElysiumPlayer) {
        throw UnsupportedOperationException()
    }

    fun sendMessage(sender: ElysiumPlayer, message: String) {
        throw UnsupportedOperationException()
    }

}
