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

package com.rpkit.chat.bukkit.speaker

import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.player.RPKPlayer

/**
 * Represents a chat channel speaker.
 *
 * @property player The player
 * @property chatChannel The chat channel the player is speaking in
 */
class RPKChatChannelSpeaker(
        override var id: Int = 0,
        val player: RPKPlayer,
        var chatChannel: RPKChatChannel
) : Entity