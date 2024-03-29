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

package com.rpkit.monsters.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.location.RPKBlockLocation
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.database.create
import com.rpkit.monsters.bukkit.database.jooq.Tables.RPKIT_MONSTER_SPAWN_AREA
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnArea
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaId
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaImpl
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaMonster
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKMonsterSpawnAreaTable(private val database: Database, private val plugin: RPKMonstersBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_monster_spawn_area.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-monsters-bukkit.rpkit_monster_spawn_area.id",
            Int::class.javaObjectType,
            RPKMonsterSpawnArea::class.java,
            plugin.config.getLong("caching.rpkit_monster_spawn_area.id.size")
        )
    } else {
        null
    }

    private val allSpawnAreas = mutableSetOf<RPKMonsterSpawnArea>()
    private var allFetched = false

    fun insert(entity: RPKMonsterSpawnArea): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_MONSTER_SPAWN_AREA,
                    RPKIT_MONSTER_SPAWN_AREA.WORLD,
                    RPKIT_MONSTER_SPAWN_AREA.MIN_X,
                    RPKIT_MONSTER_SPAWN_AREA.MIN_Y,
                    RPKIT_MONSTER_SPAWN_AREA.MIN_Z,
                    RPKIT_MONSTER_SPAWN_AREA.MAX_X,
                    RPKIT_MONSTER_SPAWN_AREA.MAX_Y,
                    RPKIT_MONSTER_SPAWN_AREA.MAX_Z
                )
                .values(
                    entity.minPoint.world,
                    entity.minPoint.x,
                    entity.minPoint.y,
                    entity.minPoint.z,
                    entity.maxPoint.x,
                    entity.maxPoint.y,
                    entity.maxPoint.z
                )
                .execute()
            val id = database.create.lastID().toInt()
            entity.id = RPKMonsterSpawnAreaId(id)
            cache?.set(id, entity)
            if (!allSpawnAreas.contains(entity)) {
                allSpawnAreas.add(entity)
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert monster spawn area", exception)
            throw exception
        }
    }

    fun update(entity: RPKMonsterSpawnArea): CompletableFuture<Void> {
        val id = entity.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
            .update(RPKIT_MONSTER_SPAWN_AREA)
            .set(RPKIT_MONSTER_SPAWN_AREA.WORLD, entity.minPoint.world)
            .set(RPKIT_MONSTER_SPAWN_AREA.MIN_X, entity.minPoint.x)
            .set(RPKIT_MONSTER_SPAWN_AREA.MIN_Y, entity.minPoint.y)
            .set(RPKIT_MONSTER_SPAWN_AREA.MIN_Z, entity.minPoint.z)
            .set(RPKIT_MONSTER_SPAWN_AREA.MAX_X, entity.maxPoint.x)
            .set(RPKIT_MONSTER_SPAWN_AREA.MAX_Y, entity.maxPoint.y)
            .set(RPKIT_MONSTER_SPAWN_AREA.MAX_Z, entity.maxPoint.z)
            .where(RPKIT_MONSTER_SPAWN_AREA.ID.eq(id.value))
            .execute()
            cache?.set(id.value, entity)
            if (!allSpawnAreas.contains(entity)) {
                allSpawnAreas.add(entity)
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update monster spawn area", exception)
            throw exception
        }
    }

    operator fun get(id: RPKMonsterSpawnAreaId): CompletableFuture<out RPKMonsterSpawnArea?> {
        if (cache?.containsKey(id.value) == true) {
            return CompletableFuture.completedFuture(cache[id.value])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create.select(
                RPKIT_MONSTER_SPAWN_AREA.WORLD,
                RPKIT_MONSTER_SPAWN_AREA.MIN_X,
                RPKIT_MONSTER_SPAWN_AREA.MIN_Y,
                RPKIT_MONSTER_SPAWN_AREA.MIN_Z,
                RPKIT_MONSTER_SPAWN_AREA.MAX_X,
                RPKIT_MONSTER_SPAWN_AREA.MAX_Y,
                RPKIT_MONSTER_SPAWN_AREA.MAX_Z
            )
                .from(RPKIT_MONSTER_SPAWN_AREA)
                .where(RPKIT_MONSTER_SPAWN_AREA.ID.eq(id.value))
                .fetchOne() ?: return@supplyAsync null
            val allowedMonsters = database.getTable(RPKMonsterSpawnAreaMonsterTable::class.java).get(id).join()
            val minLevels = allowedMonsters.associate { monsterSpawnAreaMonster ->
                monsterSpawnAreaMonster.entityType to monsterSpawnAreaMonster.minLevel
            }.toMutableMap()
            val maxLevels = allowedMonsters.associate { monsterSpawnAreaMonster ->
                monsterSpawnAreaMonster.entityType to monsterSpawnAreaMonster.maxLevel
            }.toMutableMap()
            val monsterSpawnArea = RPKMonsterSpawnAreaImpl(
                plugin,
                id,
                RPKBlockLocation(
                    result[RPKIT_MONSTER_SPAWN_AREA.WORLD],
                    result[RPKIT_MONSTER_SPAWN_AREA.MIN_X],
                    result[RPKIT_MONSTER_SPAWN_AREA.MIN_Y],
                    result[RPKIT_MONSTER_SPAWN_AREA.MIN_Z]
                ),
                RPKBlockLocation(
                    result[RPKIT_MONSTER_SPAWN_AREA.WORLD],
                    result[RPKIT_MONSTER_SPAWN_AREA.MAX_X],
                    result[RPKIT_MONSTER_SPAWN_AREA.MAX_Y],
                    result[RPKIT_MONSTER_SPAWN_AREA.MAX_Z]
                ),
                allowedMonsters.mapTo(mutableSetOf(), RPKMonsterSpawnAreaMonster::entityType),
                minLevels,
                maxLevels
            )
            cache?.set(id.value, monsterSpawnArea)
            if (!allSpawnAreas.contains(monsterSpawnArea)) {
                allSpawnAreas.add(monsterSpawnArea)
            }
            return@supplyAsync monsterSpawnArea
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get monster spawn area", exception)
            throw exception
        }
    }

    fun getAll(): CompletableFuture<List<RPKMonsterSpawnArea>> {
        if (allFetched) return CompletableFuture.completedFuture(allSpawnAreas.toList())
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_MONSTER_SPAWN_AREA.ID)
                .from(RPKIT_MONSTER_SPAWN_AREA)
                .fetch()
            val spawnAreaFutures = results.mapNotNull { get(RPKMonsterSpawnAreaId(it[RPKIT_MONSTER_SPAWN_AREA.ID])) }
            CompletableFuture.allOf(*spawnAreaFutures.toTypedArray()).join()
            allSpawnAreas.addAll(spawnAreaFutures.mapNotNull(CompletableFuture<out RPKMonsterSpawnArea?>::join))
            allFetched = true
            return@supplyAsync allSpawnAreas.toList()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get all monster spawn areas", exception)
            throw exception
        }
    }

    fun delete(entity: RPKMonsterSpawnArea): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_MONSTER_SPAWN_AREA)
                .where(RPKIT_MONSTER_SPAWN_AREA.ID.eq(entity.id?.value))
                .execute()
            entity.id?.let { database.getTable(RPKMonsterSpawnAreaMonsterTable::class.java).delete(it).join() }
            allSpawnAreas.remove(entity)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete monster spawn area", exception)
            throw exception
        }
    }
}