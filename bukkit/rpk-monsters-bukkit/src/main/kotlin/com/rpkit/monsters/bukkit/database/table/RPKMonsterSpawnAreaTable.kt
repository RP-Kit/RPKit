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
import com.rpkit.monsters.bukkit.database.jooq.rpkit.Tables.RPKIT_MONSTER_SPAWN_AREA
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnArea
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaImpl
import org.bukkit.Location
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType


class RPKMonsterSpawnAreaTable(database: Database, private val plugin: RPKMonstersBukkit): Table<RPKMonsterSpawnArea>(database, RPKMonsterSpawnArea::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_monster_spawn_area.id.enabled")) {
        database.cacheManager.createCache("rpk-monsters-bukkit.rpkit_monster_spawn_area.id",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKMonsterSpawnArea::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_monster_spawn_area.id.size"))).build())
    } else {
        null
    }

    private val allSpawnAreas = mutableSetOf<RPKMonsterSpawnArea>()
    private var allFetched = false

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_MONSTER_SPAWN_AREA)
                .column(RPKIT_MONSTER_SPAWN_AREA.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_MONSTER_SPAWN_AREA.WORLD, SQLDataType.VARCHAR(256))
                .column(RPKIT_MONSTER_SPAWN_AREA.MIN_X, SQLDataType.INTEGER)
                .column(RPKIT_MONSTER_SPAWN_AREA.MIN_Y, SQLDataType.INTEGER)
                .column(RPKIT_MONSTER_SPAWN_AREA.MIN_Z, SQLDataType.INTEGER)
                .column(RPKIT_MONSTER_SPAWN_AREA.MAX_X, SQLDataType.INTEGER)
                .column(RPKIT_MONSTER_SPAWN_AREA.MAX_Y, SQLDataType.INTEGER)
                .column(RPKIT_MONSTER_SPAWN_AREA.MAX_Z, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_monster_spawn_area").primaryKey(RPKIT_MONSTER_SPAWN_AREA.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.8.0")
        }
    }

    override fun insert(entity: RPKMonsterSpawnArea): Int {
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
                        entity.minPoint.world?.name,
                        entity.minPoint.blockX,
                        entity.minPoint.blockY,
                        entity.minPoint.blockZ,
                        entity.maxPoint.blockX,
                        entity.maxPoint.blockY,
                        entity.maxPoint.blockZ
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        if (!allSpawnAreas.contains(entity)) {
            allSpawnAreas.add(entity)
        }
        return id
    }

    override fun update(entity: RPKMonsterSpawnArea) {
        database.create
                .update(RPKIT_MONSTER_SPAWN_AREA)
                .set(RPKIT_MONSTER_SPAWN_AREA.WORLD, entity.minPoint.world?.name)
                .set(RPKIT_MONSTER_SPAWN_AREA.MIN_X, entity.minPoint.blockX)
                .set(RPKIT_MONSTER_SPAWN_AREA.MIN_Y, entity.minPoint.blockY)
                .set(RPKIT_MONSTER_SPAWN_AREA.MIN_Z, entity.minPoint.blockZ)
                .set(RPKIT_MONSTER_SPAWN_AREA.MAX_X, entity.maxPoint.blockX)
                .set(RPKIT_MONSTER_SPAWN_AREA.MAX_Y, entity.maxPoint.blockY)
                .set(RPKIT_MONSTER_SPAWN_AREA.MAX_Z, entity.maxPoint.blockZ)
                .where(RPKIT_MONSTER_SPAWN_AREA.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
        if (!allSpawnAreas.contains(entity)) {
            allSpawnAreas.add(entity)
        }
    }

    override fun get(id: Int): RPKMonsterSpawnArea? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        }
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
                .where(RPKIT_MONSTER_SPAWN_AREA.ID.eq(id))
                .fetchOne() ?: return null
        val world = plugin.server.getWorld(result[RPKIT_MONSTER_SPAWN_AREA.WORLD])
        val monsterSpawnArea = RPKMonsterSpawnAreaImpl(
                plugin,
                id,
                Location(
                        world,
                        result[RPKIT_MONSTER_SPAWN_AREA.MIN_X].toDouble(),
                        result[RPKIT_MONSTER_SPAWN_AREA.MIN_Y].toDouble(),
                        result[RPKIT_MONSTER_SPAWN_AREA.MIN_Z].toDouble()
                ),
                Location(
                        world,
                        result[RPKIT_MONSTER_SPAWN_AREA.MAX_X].toDouble(),
                        result[RPKIT_MONSTER_SPAWN_AREA.MAX_Y].toDouble(),
                        result[RPKIT_MONSTER_SPAWN_AREA.MAX_Z].toDouble()
                )
        )
        cache?.put(monsterSpawnArea.id, monsterSpawnArea)
        if (!allSpawnAreas.contains(monsterSpawnArea)) {
            allSpawnAreas.add(monsterSpawnArea)
        }
        return monsterSpawnArea
    }

    fun getAll(): List<RPKMonsterSpawnArea> {
        if (allFetched) return allSpawnAreas.toList()
        val results = database.create
                .select(RPKIT_MONSTER_SPAWN_AREA.ID)
                .from(RPKIT_MONSTER_SPAWN_AREA)
                .fetch()
        val spawnAreas = results.mapNotNull { get(it[RPKIT_MONSTER_SPAWN_AREA.ID]) }
        allSpawnAreas.addAll(spawnAreas)
        allFetched = true
        return allSpawnAreas.toList()
    }

    override fun delete(entity: RPKMonsterSpawnArea) {
        database.create
                .deleteFrom(RPKIT_MONSTER_SPAWN_AREA)
                .where(RPKIT_MONSTER_SPAWN_AREA.ID.eq(entity.id))
                .execute()
        database.getTable(RPKMonsterSpawnAreaMonsterTable::class).delete(entity)
        allSpawnAreas.remove(entity)
    }
}