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

import com.rpkit.core.caching.RPKCacheConfiguration
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.database.create
import com.rpkit.monsters.bukkit.database.jooq.Tables.RPKIT_MONSTER_SPAWN_AREA_MONSTER
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaId
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaMonster
import org.bukkit.entity.EntityType
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKMonsterSpawnAreaMonsterTable(private val database: Database, private val plugin: RPKMonstersBukkit) : Table {

    private val areaCache = if (plugin.config.getBoolean("caching.rpkit_monster_spawn_area_monster.monster_spawn_area_id.enabled")) {
        database.cacheManager.createCache(RPKCacheConfiguration<Int, MutableList<RPKMonsterSpawnAreaMonster>>(
            "rpk-monsters-bukkit.rpkit_monster_spawn_area_monster.monster_spawn_area_id",
            plugin.config.getLong("caching.rpkit_monster_spawn_area_monster.monster_spawn_area_id.size")
        ))
    } else {
        null
    }

    fun insert(monsterSpawnAreaId: RPKMonsterSpawnAreaId, entity: RPKMonsterSpawnAreaMonster): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_MONSTER_SPAWN_AREA_MONSTER,
                    RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID,
                    RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE,
                    RPKIT_MONSTER_SPAWN_AREA_MONSTER.MIN_LEVEL,
                    RPKIT_MONSTER_SPAWN_AREA_MONSTER.MAX_LEVEL
                )
                .values(
                    monsterSpawnAreaId.value,
                    entity.entityType.toString(),
                    entity.minLevel,
                    entity.maxLevel
                )
                .execute()
            if (areaCache != null) {
                val monsters = areaCache[monsterSpawnAreaId.value] ?: mutableListOf()
                if (!monsters.contains(entity)) {
                    monsters.add(entity)
                    areaCache[monsterSpawnAreaId.value] = monsters
                }
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert monster spawn area monster", exception)
            throw exception
        }
    }

    fun update(monsterSpawnAreaId: RPKMonsterSpawnAreaId, entity: RPKMonsterSpawnAreaMonster): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .set(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MIN_LEVEL, entity.minLevel)
                .set(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MAX_LEVEL, entity.maxLevel)
                .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID.eq(monsterSpawnAreaId.value))
                .and(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE.eq(entity.entityType.toString()))
                .execute()
            if (areaCache != null) {
                val monsters = areaCache[monsterSpawnAreaId.value] ?: mutableListOf()
                if (!monsters.contains(entity)) {
                    monsters.add(entity)
                    areaCache[monsterSpawnAreaId.value] = monsters
                }
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update monster spawn area monster", exception)
            throw exception
        }
    }

    fun get(monsterSpawnAreaId: RPKMonsterSpawnAreaId): CompletableFuture<out List<RPKMonsterSpawnAreaMonster>> {
        if (areaCache?.containsKey(monsterSpawnAreaId.value) == true) {
            return CompletableFuture.completedFuture(areaCache[monsterSpawnAreaId.value] as List<RPKMonsterSpawnAreaMonster>)
        }
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(
                    RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE,
                    RPKIT_MONSTER_SPAWN_AREA_MONSTER.MIN_LEVEL,
                    RPKIT_MONSTER_SPAWN_AREA_MONSTER.MAX_LEVEL
                )
                .from(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID.eq(monsterSpawnAreaId.value))
                .fetch()

            val monsters = results.mapNotNull { result ->
                RPKMonsterSpawnAreaMonster(
                    EntityType.valueOf(result[RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE]),
                    result[RPKIT_MONSTER_SPAWN_AREA_MONSTER.MIN_LEVEL],
                    result[RPKIT_MONSTER_SPAWN_AREA_MONSTER.MAX_LEVEL]
                )
            }.toMutableList()
            areaCache?.set(monsterSpawnAreaId.value, monsters)
            return@supplyAsync monsters
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get monster spawn area monsters", exception)
            throw exception
        }
    }

    fun delete(monsterSpawnAreaId: RPKMonsterSpawnAreaId, entity: RPKMonsterSpawnAreaMonster): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID.eq(monsterSpawnAreaId.value))
                .and(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE.eq(entity.entityType.toString()))
                .execute()
            val monsters = areaCache?.get(monsterSpawnAreaId.value) ?: mutableListOf()
            monsters.remove(entity)
            areaCache?.set(monsterSpawnAreaId.value, monsters)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete monster spawn area monster", exception)
            throw exception
        }
    }

    fun delete(monsterSpawnAreaId: RPKMonsterSpawnAreaId): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID.eq(monsterSpawnAreaId.value))
                .execute()
            areaCache?.remove(monsterSpawnAreaId.value)
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete monster spawn area monsters", exception)
            throw exception
        }
    }
}