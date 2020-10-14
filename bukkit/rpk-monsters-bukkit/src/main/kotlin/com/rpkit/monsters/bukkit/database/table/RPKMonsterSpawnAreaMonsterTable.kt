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

package com.rpkit.monsters.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.database.jooq.Tables.RPKIT_MONSTER_SPAWN_AREA_MONSTER
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnArea
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaMonster
import org.bukkit.entity.EntityType
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKMonsterSpawnAreaMonsterTable(private val database: Database, private val plugin: RPKMonstersBukkit) : Table {

    private val areaCache = if (plugin.config.getBoolean("caching.rpkit_monster_spawn_area_monster.monster_spawn_area_id.enabled")) {
        database.cacheManager.createCache("rpk-monsters-bukkit.rpkit_monster_spawn_area_monster.monster_spawn_area_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_monster_spawn_area_monster.monster_spawn_area_id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKMonsterSpawnAreaMonster) {
        database.create
                .insertInto(
                        RPKIT_MONSTER_SPAWN_AREA_MONSTER,
                        RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID,
                        RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE,
                        RPKIT_MONSTER_SPAWN_AREA_MONSTER.MIN_LEVEL,
                        RPKIT_MONSTER_SPAWN_AREA_MONSTER.MAX_LEVEL
                )
                .values(
                        entity.monsterSpawnArea.id,
                        entity.entityType.toString(),
                        entity.minLevel,
                        entity.maxLevel
                )
                .execute()
        if (areaCache != null) {
            val monsters: MutableList<RPKMonsterSpawnAreaMonster> =
                    (areaCache.get(entity.monsterSpawnArea.id) as? MutableList<RPKMonsterSpawnAreaMonster>)
                            ?: mutableListOf()
            if (!monsters.contains(entity)) {
                monsters.add(entity)
                areaCache.put(entity.monsterSpawnArea.id, monsters)
            }
        }
    }

    fun update(entity: RPKMonsterSpawnAreaMonster) {
        database.create
                .update(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .set(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MIN_LEVEL, entity.minLevel)
                .set(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MAX_LEVEL, entity.maxLevel)
                .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID.eq(entity.monsterSpawnArea.id))
                .and(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE.eq(entity.entityType.toString()))
                .execute()
        if (areaCache != null) {
            val monsters: MutableList<RPKMonsterSpawnAreaMonster> =
                    (areaCache.get(entity.monsterSpawnArea.id) as? MutableList<RPKMonsterSpawnAreaMonster>)
                            ?: mutableListOf()
            if (!monsters.contains(entity)) {
                monsters.add(entity)
                areaCache.put(entity.monsterSpawnArea.id, monsters)
            }
        }
    }

    fun get(monsterSpawnArea: RPKMonsterSpawnArea): List<RPKMonsterSpawnAreaMonster> {
        if (areaCache?.containsKey(monsterSpawnArea.id) == true) {
            return areaCache[monsterSpawnArea.id] as List<RPKMonsterSpawnAreaMonster>
        }
        val results = database.create
                .select(
                        RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE,
                        RPKIT_MONSTER_SPAWN_AREA_MONSTER.MIN_LEVEL,
                        RPKIT_MONSTER_SPAWN_AREA_MONSTER.MAX_LEVEL
                )
                .from(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID.eq(monsterSpawnArea.id))
                .fetch()

        val monsters = results.mapNotNull { result ->
            RPKMonsterSpawnAreaMonster(
                    monsterSpawnArea,
                    EntityType.valueOf(result[RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE]),
                    result[RPKIT_MONSTER_SPAWN_AREA_MONSTER.MIN_LEVEL],
                    result[RPKIT_MONSTER_SPAWN_AREA_MONSTER.MAX_LEVEL]
            )
        }.toMutableList()
        areaCache?.put(monsterSpawnArea.id, monsters)
        return monsters
    }

    fun delete(entity: RPKMonsterSpawnAreaMonster) {
        database.create
                .deleteFrom(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID.eq(entity.monsterSpawnArea.id))
                .and(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE.eq(entity.entityType.toString()))
                .execute()
        areaCache?.get(entity.monsterSpawnArea.id)?.remove(entity)
    }

    fun delete(monsterSpawnArea: RPKMonsterSpawnArea) {
        database.create
                .deleteFrom(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID.eq(monsterSpawnArea.id))
                .execute()
        areaCache?.remove(monsterSpawnArea.id)
    }
}