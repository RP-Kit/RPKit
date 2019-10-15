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
import com.rpkit.monsters.bukkit.database.table.RPKMonsterSpawnAreaTable
import org.bukkit.Location


class RPKMonsterSpawnAreaProviderImpl(private val plugin: RPKMonstersBukkit): RPKMonsterSpawnAreaProvider {

    override fun getSpawnArea(location: Location): RPKMonsterSpawnArea? {
        return plugin.core.database.getTable(RPKMonsterSpawnAreaTable::class).getAll().firstOrNull { spawnArea ->
            location.world == spawnArea.minPoint.world
                    && location.x >= spawnArea.minPoint.x
                    && location.y >= spawnArea.minPoint.y
                    && location.z >= spawnArea.minPoint.z
                    && location.x <= spawnArea.maxPoint.x
                    && location.y <= spawnArea.maxPoint.y
                    && location.z <= spawnArea.maxPoint.z
        }
    }

    override fun addSpawnArea(monsterSpawnArea: RPKMonsterSpawnArea) {
        plugin.core.database.getTable(RPKMonsterSpawnAreaTable::class).insert(monsterSpawnArea)
    }

    override fun removeSpawnArea(monsterSpawnArea: RPKMonsterSpawnArea) {
        plugin.core.database.getTable(RPKMonsterSpawnAreaTable::class).delete(monsterSpawnArea)
    }

}