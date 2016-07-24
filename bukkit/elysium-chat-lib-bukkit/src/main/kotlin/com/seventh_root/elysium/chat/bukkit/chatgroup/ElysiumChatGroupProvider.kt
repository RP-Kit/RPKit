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

import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer


interface ElysiumChatGroupProvider: ServiceProvider {
    fun getChatGroup(id: Int): ElysiumChatGroup?
    fun getChatGroup(name: String): ElysiumChatGroup?
    fun addChatGroup(chatGroup: ElysiumChatGroup)
    fun removeChatGroup(chatGroup: ElysiumChatGroup)
    fun updateChatGroup(chatGroup: ElysiumChatGroup)
    fun getLastUsedChatGroup(player: ElysiumPlayer): ElysiumChatGroup?
    fun setLastUsedChatGroup(player: ElysiumPlayer, chatGroup: ElysiumChatGroup)
}