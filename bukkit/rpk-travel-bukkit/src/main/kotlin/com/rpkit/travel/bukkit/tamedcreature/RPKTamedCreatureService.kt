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

package com.rpkit.travel.bukkit.tamedcreature

import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.core.service.Service
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.travel.bukkit.database.table.RPKTamedCreatureTable
import org.bukkit.entity.LivingEntity
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level.SEVERE

class RPKTamedCreatureService(override val plugin: RPKTravelBukkit) : Service {

    private val creatureOwners = ConcurrentHashMap<UUID, RPKCharacterId>()

    init {
        plugin.database.getTable(RPKTamedCreatureTable::class.java).getOwners().exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to load tamed creatures", exception)
            throw exception
        }.thenAccept { owners ->
            creatureOwners.putAll(owners)
        }
        creatureOwners.filterKeys { entityId -> plugin.server.getEntity(entityId) == null }.forEach { (entityId, _) ->
            plugin.database.getTable(RPKTamedCreatureTable::class.java).setOwner(entityId, null)
            creatureOwners.remove(entityId)
        }
    }

    fun getOwner(entity: LivingEntity): RPKCharacterId? {
        return creatureOwners[entity.uniqueId]
    }

    fun setOwner(entity: LivingEntity, characterId: RPKCharacterId?): CompletableFuture<Void> = runAsync {
        plugin.database.getTable(RPKTamedCreatureTable::class.java).setOwner(entity.uniqueId, characterId).exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to set entity owner", exception)
            throw exception
        }.thenRun {
            if (characterId != null) {
                creatureOwners[entity.uniqueId] = characterId
            } else {
                creatureOwners.remove(entity.uniqueId)
            }
        }
    }

}