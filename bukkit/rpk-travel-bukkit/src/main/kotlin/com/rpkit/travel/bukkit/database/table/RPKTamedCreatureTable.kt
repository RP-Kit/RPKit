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

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.travel.bukkit.database.create
import com.rpkit.travel.bukkit.database.jooq.Tables.RPKIT_TAMED_CREATURE
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.CompletableFuture.supplyAsync

class RPKTamedCreatureTable(private val database: Database) : Table {

    fun getOwner(entityId: UUID): CompletableFuture<RPKCharacter?> = supplyAsync {
        val tamedCreatureRecord = database.create.selectFrom(RPKIT_TAMED_CREATURE)
            .where(RPKIT_TAMED_CREATURE.ENTITY_ID.eq(entityId.toString()))
            .fetchOne() ?: return@supplyAsync null
        val characterId = tamedCreatureRecord.characterId
        val characterService = Services[RPKCharacterService::class.java] ?: return@supplyAsync null
        return@supplyAsync characterService.getCharacter(characterId.let(::RPKCharacterId)).join()
    }

    fun getOwners(): CompletableFuture<Map<UUID, RPKCharacterId>> = supplyAsync {
        return@supplyAsync database.create.selectFrom(RPKIT_TAMED_CREATURE)
            .fetch()
            .associate { UUID.fromString(it.entityId) to it.characterId.let(::RPKCharacterId) }
    }

    fun setOwner(entityId: UUID, characterId: RPKCharacterId?): CompletableFuture<Void> = runAsync {
        val tamedCreatureRecord = database.create.selectFrom(RPKIT_TAMED_CREATURE)
            .where(RPKIT_TAMED_CREATURE.ENTITY_ID.eq(entityId.toString()))
            .fetchOne()
        if (tamedCreatureRecord == null) {
            if (characterId != null) {
                database.create.insertInto(RPKIT_TAMED_CREATURE)
                    .set(RPKIT_TAMED_CREATURE.ENTITY_ID, entityId.toString())
                    .set(RPKIT_TAMED_CREATURE.CHARACTER_ID, characterId.value)
                    .execute()
            } else {
                database.create.deleteFrom(RPKIT_TAMED_CREATURE)
                    .where(RPKIT_TAMED_CREATURE.ENTITY_ID.eq(entityId.toString()))
                    .execute()
            }
        } else {
            if (characterId != null) {
                database.create.update(RPKIT_TAMED_CREATURE)
                    .set(RPKIT_TAMED_CREATURE.CHARACTER_ID, characterId.value)
                    .where(RPKIT_TAMED_CREATURE.ENTITY_ID.eq(entityId.toString()))
                    .execute()
            } else {
                database.create.deleteFrom(RPKIT_TAMED_CREATURE)
                    .where(RPKIT_TAMED_CREATURE.ENTITY_ID.eq(entityId.toString()))
                    .execute()
            }
        }
    }

}