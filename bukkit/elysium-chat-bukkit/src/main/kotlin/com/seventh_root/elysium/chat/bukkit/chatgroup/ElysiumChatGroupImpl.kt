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

import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import java.util.*

class ElysiumChatGroupImpl(
        override var id: Int = 0,
        override var name: String,
        players: List<ElysiumPlayer>,
        invited: List<ElysiumPlayer>
): ElysiumChatGroup {

    private val players0 = mutableListOf<ElysiumPlayer>()
    override val players: List<ElysiumPlayer>
        get() = Collections.unmodifiableList(players0)
    private val invited0 = mutableListOf<ElysiumPlayer>()
    override val invited: List<ElysiumPlayer>
        get() = Collections.unmodifiableList(invited0)

    init {
        players0.addAll(players)
        invited0.addAll(invited)
    }

    override fun addPlayer(player: ElysiumPlayer) {
        throw UnsupportedOperationException()
    }

    override fun removePlayer(player: ElysiumPlayer) {
        throw UnsupportedOperationException()
    }

    override fun invite(player: ElysiumPlayer) {
        throw UnsupportedOperationException()
    }

    override fun uninvite(player: ElysiumPlayer) {
        throw UnsupportedOperationException()
    }

    override fun sendMessage(sender: ElysiumPlayer, message: String) {
        throw UnsupportedOperationException()
    }

}
