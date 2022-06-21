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

import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.rolling.bukkit.RPKRollingBukkit
import org.bukkit.scoreboard.DisplaySlot.SIDEBAR
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard

class RPKTurnOrder(
    private val plugin: RPKRollingBukkit,
    val name: String,
    val entries: ArrayDeque<String>
) {

    private val scoreboard: Scoreboard
    private val objective: Objective

    init {
        val scoreboardManager = plugin.server.scoreboardManager
        scoreboard = scoreboardManager?.newScoreboard ?: throw IllegalStateException("Cannot create a turn order before the first world has loaded")
        objective = scoreboard.registerNewObjective("Turn order", "dummy", "Turn order")
        objective.displaySlot = SIDEBAR
        renderScoreboard()
    }

    fun advance() {
        entries.addLast(entries.removeFirst())
        renderScoreboard()
    }

    fun add(entry: String) {
        entries.addLast(entry)
        renderScoreboard()
    }

    fun remove(entry: String) {
        entries.remove(entry)
        renderScoreboard()
    }

    fun show(minecraftProfile: RPKMinecraftProfile) {
        val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID) ?: return
        bukkitPlayer.scoreboard = scoreboard
    }

    fun isActiveFor(minecraftProfile: RPKMinecraftProfile): Boolean {
        val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID) ?: return false
        return bukkitPlayer.scoreboard == scoreboard
    }

    private fun renderScoreboard() {
        scoreboard.entries.forEach { entry ->
            scoreboard.resetScores(entry)
        }
        if (entries.isEmpty()) return
        val formattedEntries = listOf(entries.first() + " \u2190") + entries.drop(1)
        formattedEntries.forEachIndexed { index, entry ->
            scoreboard.getScores(entry).forEach { score -> score.score = entries.size - index }
        }
    }
}