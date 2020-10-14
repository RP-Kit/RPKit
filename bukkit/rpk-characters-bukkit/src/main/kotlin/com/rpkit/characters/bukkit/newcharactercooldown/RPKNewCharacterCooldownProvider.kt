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

package com.rpkit.characters.bukkit.newcharactercooldown

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.database.table.RPKNewCharacterCooldownTable
import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfile
import java.time.Duration
import java.time.LocalDateTime


class RPKNewCharacterCooldownService(override val plugin: RPKCharactersBukkit) : Service {

    fun getNewCharacterCooldown(profile: RPKProfile): Duration = plugin.database
            .getTable(RPKNewCharacterCooldownTable::class)
            .get(profile)
            ?.let { Duration.between(LocalDateTime.now(), it.cooldownExpiryTime) }
            ?: Duration.ZERO

    fun setNewCharacterCooldown(profile: RPKProfile, cooldown: Duration) {
        val newCharacterCooldownTable = plugin.database.getTable(RPKNewCharacterCooldownTable::class)
        var newCharacterCooldown = newCharacterCooldownTable.get(profile)
        if (newCharacterCooldown == null) {
            newCharacterCooldown = RPKNewCharacterCooldown(profile = profile, cooldownExpiryTime = LocalDateTime.now().plus(cooldown))
            newCharacterCooldownTable.insert(newCharacterCooldown)
        } else {
            newCharacterCooldown.cooldownExpiryTime = LocalDateTime.now().plus(cooldown)
            newCharacterCooldownTable.update(newCharacterCooldown)
        }
    }

}