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

package com.rpkit.travel.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.travel.bukkit.database.create
import com.rpkit.travel.bukkit.database.jooq.Tables.RPKIT_UNTAMER
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.CompletableFuture.supplyAsync

class RPKUntamerTable(private val database: Database) : Table {

    fun insert(minecraftProfileId: RPKMinecraftProfileId) = runAsync {
        database.create
            .insertInto(RPKIT_UNTAMER)
            .set(RPKIT_UNTAMER.MINECRAFT_PROFILE_ID, minecraftProfileId.value)
            .execute()
    }

    fun delete(minecraftProfileId: RPKMinecraftProfileId) = runAsync {
        database.create
            .delete(RPKIT_UNTAMER)
            .where(RPKIT_UNTAMER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
            .execute()
    }

    fun isPresent(minecraftProfileId: RPKMinecraftProfileId) = supplyAsync {
        val results = database.create
            .select(RPKIT_UNTAMER.MINECRAFT_PROFILE_ID)
            .from(RPKIT_UNTAMER)
            .where(RPKIT_UNTAMER.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
            .fetch()
        return@supplyAsync results.isNotEmpty
    }

}