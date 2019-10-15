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

package com.rpkit.monsters.bukkit.monsterspawnarea

import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.database.table.RPKMonsterSpawnAreaMonsterTable
import com.rpkit.monsters.bukkit.database.table.RPKMonsterSpawnAreaTable
import org.bukkit.Location
import org.bukkit.entity.EntityType


class RPKMonsterSpawnAreaImpl constructor(
        private val plugin: RPKMonstersBukkit,
        override var id: Int = 0,
        override val minPoint: Location,
        override val maxPoint: Location
): RPKMonsterSpawnArea {

    override val allowedMonsters: Set<EntityType>
        get() = plugin.core.database.getTable(RPKMonsterSpawnAreaMonsterTable::class).get(this)
                .mapTo(mutableSetOf(), RPKMonsterSpawnAreaMonster::entityType)

    val minLevels: MutableMap<EntityType, Int>
        get() = plugin.core.database.getTable(RPKMonsterSpawnAreaMonsterTable::class).get(this)
                .map { monsterSpawnAreaMonster -> Pair(monsterSpawnAreaMonster.entityType, monsterSpawnAreaMonster.minLevel) }
                .toMap()
                .toMutableMap()

    val maxLevels: MutableMap<EntityType, Int>
        get() = plugin.core.database.getTable(RPKMonsterSpawnAreaMonsterTable::class).get(this)
                .map { monsterSpawnAreaMonster -> Pair(monsterSpawnAreaMonster.entityType, monsterSpawnAreaMonster.maxLevel) }
                .toMap()
                .toMutableMap()

    override fun getMinLevel(entityType: EntityType): Int {
        if (!allowedMonsters.contains(entityType)) return 0
        return minLevels[entityType] ?: 1
    }

    override fun getMaxLevel(entityType: EntityType): Int {
        if (!allowedMonsters.contains(entityType)) return 0
        return maxLevels[entityType] ?: 1
    }

    override fun addMonster(entityType: EntityType, minLevel: Int, maxLevel: Int) {
        if (id == 0) plugin.core.database.getTable(RPKMonsterSpawnAreaTable::class).insert(this)
        plugin.core.database.getTable(RPKMonsterSpawnAreaMonsterTable::class).insert(RPKMonsterSpawnAreaMonster(
                monsterSpawnArea = this,
                entityType = entityType,
                minLevel = minLevel,
                maxLevel = maxLevel
        ))
    }

    override fun removeMonster(entityType: EntityType) {
        if (id == 0) return
        val monsterSpawnAreaMonsterTable = plugin.core.database.getTable(RPKMonsterSpawnAreaMonsterTable::class)
        monsterSpawnAreaMonsterTable.get(this)
                .filter { monster -> monster.entityType == entityType }
                .forEach { monsterSpawnAreaMonsterTable.delete(it) }
    }

}