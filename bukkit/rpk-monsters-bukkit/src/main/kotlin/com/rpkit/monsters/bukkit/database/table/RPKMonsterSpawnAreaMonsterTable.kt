/*
 * Copyright 2019 Ren Binden
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
import com.rpkit.monsters.bukkit.database.jooq.rpkit.Tables.RPKIT_MONSTER_SPAWN_AREA_MONSTER
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnArea
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaMonster
import org.bukkit.entity.EntityType
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType


class RPKMonsterSpawnAreaMonsterTable(database: Database, private val plugin: RPKMonstersBukkit): Table<RPKMonsterSpawnAreaMonster>(database, RPKMonsterSpawnAreaMonster::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_monster_spawn_area_monster.id.enabled")) {
        database.cacheManager.createCache("rpk-monsters-bukkit.rpkit_monster_spawn_area_monster.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKMonsterSpawnAreaMonster::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_monster_spawn_area_monster.id.size"))).build())
    } else {
        null
    }

    private val areaCache = if (plugin.config.getBoolean("caching.rpkit_monster_spawn_area_monster.monster_spawn_area_id.enabled")) {
        database.cacheManager.createCache("rpk-monsters-bukkit.rpkit_monster_spawn_area_monster.monster_spawn_area_id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, MutableList::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_monster_spawn_area_monster.monster_spawn_area_id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create.createTableIfNotExists(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .column(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID, SQLDataType.INTEGER)
                .column(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE, SQLDataType.VARCHAR(256))
                .column(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MIN_LEVEL, SQLDataType.INTEGER)
                .column(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MAX_LEVEL, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_monster_spawn_area").primaryKey(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.8.0")
        }
    }

    override fun insert(entity: RPKMonsterSpawnAreaMonster): Int {
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
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        if (areaCache != null) {
            val monsters: MutableList<RPKMonsterSpawnAreaMonster> =
                    (areaCache.get(entity.monsterSpawnArea.id) as? MutableList<RPKMonsterSpawnAreaMonster>)
                            ?: mutableListOf()
            if (!monsters.contains(entity)) {
                monsters.add(entity)
                areaCache.put(entity.monsterSpawnArea.id, monsters)
            }
        }
        return id
    }

    override fun update(entity: RPKMonsterSpawnAreaMonster) {
        database.create
                .update(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .set(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE, entity.entityType.toString())
                .set(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MIN_LEVEL, entity.minLevel)
                .set(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MAX_LEVEL, entity.maxLevel)
                .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
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

    override fun get(id: Int): RPKMonsterSpawnAreaMonster? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        }
        val result = database.create
                .select(
                        RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID,
                        RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE,
                        RPKIT_MONSTER_SPAWN_AREA_MONSTER.MIN_LEVEL,
                        RPKIT_MONSTER_SPAWN_AREA_MONSTER.MAX_LEVEL
                )
                .from(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ID.eq(id))
                .fetchOne() ?: return null
        val monsterSpawnArea = plugin.core.database.getTable(RPKMonsterSpawnAreaTable::class)
                .get(result[RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID])
        if (monsterSpawnArea == null) {
            database.create
                    .deleteFrom(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                    .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ID.eq(id))
                    .execute()
            cache?.remove(id)
            areaCache?.remove(result[RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID])
            return null
        }
        val monsterSpawnAreaMonster = RPKMonsterSpawnAreaMonster(
                id,
                monsterSpawnArea,
                EntityType.valueOf(result[RPKIT_MONSTER_SPAWN_AREA_MONSTER.ENTITY_TYPE]),
                result[RPKIT_MONSTER_SPAWN_AREA_MONSTER.MIN_LEVEL],
                result[RPKIT_MONSTER_SPAWN_AREA_MONSTER.MAX_LEVEL]
        )
        cache?.put(id, monsterSpawnAreaMonster)
        return monsterSpawnAreaMonster
    }

    fun get(monsterSpawnArea: RPKMonsterSpawnArea): List<RPKMonsterSpawnAreaMonster> {
        if (areaCache?.containsKey(monsterSpawnArea.id) == true) {
            return areaCache[monsterSpawnArea.id] as List<RPKMonsterSpawnAreaMonster>
        }
        val results = database.create
                .select(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ID)
                .from(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID.eq(monsterSpawnArea.id))
                .fetch()
        val monsters = results.mapNotNull { result -> get(result[RPKIT_MONSTER_SPAWN_AREA_MONSTER.ID]) }.toMutableList()
        areaCache?.put(monsterSpawnArea.id, monsters)
        return monsters
    }

    override fun delete(entity: RPKMonsterSpawnAreaMonster) {
        database.create
                .deleteFrom(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
        areaCache?.get(entity.monsterSpawnArea.id)?.remove(entity)
    }

    fun delete(monsterSpawnArea: RPKMonsterSpawnArea) {
        if (cache != null) {
            get(monsterSpawnArea).forEach { delete(it) }
        } else {
            database.create
                    .deleteFrom(RPKIT_MONSTER_SPAWN_AREA_MONSTER)
                    .where(RPKIT_MONSTER_SPAWN_AREA_MONSTER.MONSTER_SPAWN_AREA_ID.eq(monsterSpawnArea.id))
                    .execute()
        }
        areaCache?.remove(monsterSpawnArea.id)
    }
}