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

package com.rpkit.monsters.bukkit.monsterspawnarea

import com.rpkit.core.location.RPKBlockLocation
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.database.table.RPKMonsterSpawnAreaMonsterTable
import com.rpkit.monsters.bukkit.database.table.RPKMonsterSpawnAreaTable
import org.bukkit.entity.EntityType
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKMonsterSpawnAreaImpl(
    private val plugin: RPKMonstersBukkit,
    override var id: RPKMonsterSpawnAreaId? = null,
    override val minPoint: RPKBlockLocation,
    override val maxPoint: RPKBlockLocation,
    override val allowedMonsters: MutableSet<EntityType>,
    private val minLevels: MutableMap<EntityType, Int>,
    private val maxLevels: MutableMap<EntityType, Int>
) : RPKMonsterSpawnArea {

    constructor(
        plugin: RPKMonstersBukkit,
        id: RPKMonsterSpawnAreaId? = null,
        minPoint: RPKBlockLocation,
        maxPoint: RPKBlockLocation
    ): this(plugin, id, minPoint, maxPoint, mutableSetOf(), mutableMapOf(), mutableMapOf())

    @Synchronized
    override fun getMinLevel(entityType: EntityType): Int {
        if (!allowedMonsters.contains(entityType)) return 0
        return minLevels[entityType] ?: 1
    }

    @Synchronized
    override fun getMaxLevel(entityType: EntityType): Int {
        if (!allowedMonsters.contains(entityType)) return 0
        return maxLevels[entityType] ?: 1
    }

    override fun addMonster(entityType: EntityType, minLevel: Int, maxLevel: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            if (id == null) plugin.database.getTable(RPKMonsterSpawnAreaTable::class.java).insert(this).join()
        }.thenRunAsync {
            val finalId = id
            if (finalId != null) {
                plugin.database.getTable(RPKMonsterSpawnAreaMonsterTable::class.java).insert(
                    finalId, RPKMonsterSpawnAreaMonster(
                        entityType = entityType,
                        minLevel = minLevel,
                        maxLevel = maxLevel
                    )
                ).join()
                synchronized(this) {
                    allowedMonsters.add(entityType)
                    minLevels[entityType] = minLevel
                    maxLevels[entityType] = maxLevel
                }
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to add monster to monster spawn area", exception)
            throw exception
        }
    }

    override fun removeMonster(entityType: EntityType): CompletableFuture<Void> {
        val finalId = id ?: return CompletableFuture.completedFuture(null)
        val monsterSpawnAreaMonsterTable = plugin.database.getTable(RPKMonsterSpawnAreaMonsterTable::class.java)
        return monsterSpawnAreaMonsterTable.get(finalId).thenAccept { monsters ->
            monsters
                .filter { monster -> monster.entityType == entityType }
                .forEach {
                    monsterSpawnAreaMonsterTable.delete(finalId, it)
                    synchronized(this) {
                        allowedMonsters.remove(entityType)
                        minLevels.remove(entityType)
                        maxLevels.remove(entityType)
                    }
                }
        }
    }

}