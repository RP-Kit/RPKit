/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.rolling.bukkit.turnorder

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.rolling.bukkit.RPKRollingBukkit
import org.bukkit.scoreboard.Scoreboard

class RPKTurnOrderService(override val plugin: RPKRollingBukkit) : Service {

    private val turnOrders = mutableMapOf<String, RPKTurnOrder>()
    private var emptyScoreboard: Scoreboard? = plugin.server.scoreboardManager?.newScoreboard

    fun addTurnOrder(turnOrder: RPKTurnOrder) {
        turnOrders[turnOrder.name] = turnOrder
    }

    fun removeTurnOrder(turnOrder: RPKTurnOrder) {
        turnOrders.remove(turnOrder.name)
    }

    fun getTurnOrder(name: String): RPKTurnOrder? {
        return turnOrders[name]
    }

    fun getActiveTurnOrder(minecraftProfile: RPKMinecraftProfile): RPKTurnOrder? {
        return turnOrders.values.singleOrNull { it.isActiveFor(minecraftProfile) }
    }

    fun hideTurnOrder(minecraftProfile: RPKMinecraftProfile) {
        val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID) ?: return
        if (emptyScoreboard == null) emptyScoreboard = plugin.server.scoreboardManager?.newScoreboard
        val finalEmptyScoreboard = emptyScoreboard ?: return
        bukkitPlayer.scoreboard = finalEmptyScoreboard
    }

}