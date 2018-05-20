/*
 * Copyright 2018 Ross Binden
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
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class RPKUnconsciousnessProviderImpl(private val plugin: RPKUnconsciousnessBukkit): RPKUnconsciousnessProvider {

    override fun isUnconscious(character: RPKCharacter): Boolean {
        val unconsciousStateTable = plugin.core.database.getTable(RPKUnconsciousStateTable::class)
        val unconsciousState = unconsciousStateTable.get(character)
        if (unconsciousState == null) {
            return false
        } else {
            return unconsciousState.deathTime + plugin.config.getLong("unconscious-time") > System.currentTimeMillis()
        }
    }

    override fun setUnconscious(character: RPKCharacter, unconscious: Boolean) {
        val unconsciousStateTable = plugin.core.database.getTable(RPKUnconsciousStateTable::class)
        var unconsciousState = unconsciousStateTable.get(character)
        if (unconsciousState != null) {
            if (unconscious) {
                unconsciousState.deathTime = System.currentTimeMillis()
                unconsciousStateTable.update(unconsciousState)
                plugin.server.getPlayer(character.minecraftProfile?.minecraftUUID).addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 0), true)
            } else {
                unconsciousStateTable.delete(unconsciousState)
            }
        } else {
            if (unconscious) {
                unconsciousState = RPKUnconsciousState(
                        character = character,
                        deathTime = System.currentTimeMillis()
                )
                unconsciousStateTable.insert(unconsciousState)
            }
        }
    }

}