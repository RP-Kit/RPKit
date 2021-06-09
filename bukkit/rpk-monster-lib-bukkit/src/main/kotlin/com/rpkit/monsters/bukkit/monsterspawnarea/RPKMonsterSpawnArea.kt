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

package com.rpkit.monsters.bukkit.monsterspawnarea

import com.rpkit.core.location.RPKBlockLocation
import org.bukkit.entity.EntityType
import java.util.concurrent.CompletableFuture


interface RPKMonsterSpawnArea {

    var id: com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaId?
    val minPoint: RPKBlockLocation
    val maxPoint: RPKBlockLocation
    val allowedMonsters: Set<EntityType>
    fun getMinLevel(entityType: EntityType): Int
    fun getMaxLevel(entityType: EntityType): Int
    fun addMonster(entityType: EntityType, minLevel: Int, maxLevel: Int): CompletableFuture<Void>
    fun removeMonster(entityType: EntityType): CompletableFuture<Void>

}