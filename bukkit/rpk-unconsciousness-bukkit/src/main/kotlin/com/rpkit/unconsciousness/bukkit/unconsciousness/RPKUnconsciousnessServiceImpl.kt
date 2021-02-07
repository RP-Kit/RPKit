/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.unconsciousness.bukkit.unconsciousness

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.unconsciousness.bukkit.RPKUnconsciousnessBukkit
import com.rpkit.unconsciousness.bukkit.database.table.RPKUnconsciousStateTable
import com.rpkit.unconsciousness.bukkit.event.unconsciousness.RPKBukkitUnconsciousnessStateChangeEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit


class RPKUnconsciousnessServiceImpl(override val plugin: RPKUnconsciousnessBukkit) : RPKUnconsciousnessService {

    override fun isUnconscious(character: RPKCharacter): Boolean {
        val unconsciousStateTable = plugin.database.getTable(RPKUnconsciousStateTable::class.java)
        val unconsciousState = unconsciousStateTable.get(character)
        return if (unconsciousState == null) {
            false
        } else {
            unconsciousState.deathTime.plus(plugin.config.getLong("unconscious-time"), ChronoUnit.MILLIS) > LocalDateTime.now()
        }
    }

    override fun setUnconscious(character: RPKCharacter, unconscious: Boolean) {
        val event = RPKBukkitUnconsciousnessStateChangeEvent(character, unconscious)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val unconsciousStateTable = plugin.database.getTable(RPKUnconsciousStateTable::class.java)
        var unconsciousState = unconsciousStateTable.get(event.character)
        if (unconsciousState != null) {
            if (event.isUnconscious) {
                unconsciousState.deathTime = LocalDateTime.now()
                unconsciousStateTable.update(unconsciousState)
                val minecraftUUID = event.character.minecraftProfile?.minecraftUUID ?: return
                plugin.server.getPlayer(minecraftUUID)?.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0))
            } else {
                unconsciousStateTable.delete(unconsciousState)
            }
        } else {
            if (event.isUnconscious) {
                unconsciousState = RPKUnconsciousState(
                        character = event.character,
                        deathTime = LocalDateTime.now()
                )
                unconsciousStateTable.insert(unconsciousState)
            }
        }
    }

}