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

import com.seventh_root.elysium.core.service.ServiceProvider
import org.bukkit.OfflinePlayer
import org.pircbotx.User


interface ElysiumPlayerProvider: ServiceProvider {
    fun getPlayer(id: Int): ElysiumPlayer?
    fun getPlayer(bukkitPlayer: OfflinePlayer): ElysiumPlayer
    fun getPlayer(ircUser: User): ElysiumPlayer
    fun addPlayer(player: ElysiumPlayer)
    fun removePlayer(player: ElysiumPlayer)
}