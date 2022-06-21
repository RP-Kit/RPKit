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

package com.rpkit.payments.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.create
import com.rpkit.payments.bukkit.database.jooq.Tables.RPKIT_PAYMENT_GROUP_MEMBER
import com.rpkit.payments.bukkit.group.RPKPaymentGroup
import com.rpkit.payments.bukkit.group.member.RPKPaymentGroupMember
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.logging.Level
import java.util.logging.Level.SEVERE

/**
 * Represents payment group member table.
 */
class RPKPaymentGroupMemberTable(
        private val database: Database,
        private val plugin: RPKPaymentsBukkit
) : Table {

    fun insert(entity: RPKPaymentGroupMember): CompletableFuture<Void> {
        val paymentGroupId = entity.paymentGroup.id ?: return CompletableFuture.completedFuture(null)
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_PAYMENT_GROUP_MEMBER,
                    RPKIT_PAYMENT_GROUP_MEMBER.PAYMENT_GROUP_ID,
                    RPKIT_PAYMENT_GROUP_MEMBER.CHARACTER_ID
                )
                .values(
                    paymentGroupId.value,
                    characterId.value
                )
                .execute()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert payment group member", exception)
            throw exception
        }
    }

    operator fun get(paymentGroup: RPKPaymentGroup): CompletableFuture<List<RPKPaymentGroupMember>> {
        val paymentGroupId = paymentGroup.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_PAYMENT_GROUP_MEMBER.CHARACTER_ID)
                .from(RPKIT_PAYMENT_GROUP_MEMBER)
                .where(RPKIT_PAYMENT_GROUP_MEMBER.PAYMENT_GROUP_ID.eq(paymentGroupId.value))
                .fetch()
            val characterService = Services[RPKCharacterService::class.java] ?: return@supplyAsync emptyList()
            return@supplyAsync results.mapNotNull { result ->
                val character =
                    characterService.getCharacter(RPKCharacterId(result[RPKIT_PAYMENT_GROUP_MEMBER.CHARACTER_ID])).join()
                        ?: return@mapNotNull null
                return@mapNotNull RPKPaymentGroupMember(
                    paymentGroup,
                    character
                )
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get payment group members", exception)
            throw exception
        }
    }

    fun delete(entity: RPKPaymentGroupMember): CompletableFuture<Void> {
        val paymentGroupId = entity.paymentGroup.id ?: return CompletableFuture.completedFuture(null)
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_PAYMENT_GROUP_MEMBER)
                .where(RPKIT_PAYMENT_GROUP_MEMBER.PAYMENT_GROUP_ID.eq(paymentGroupId.value))
                .and(RPKIT_PAYMENT_GROUP_MEMBER.CHARACTER_ID.eq(characterId.value))
                .execute()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete payment group member", exception)
            throw exception
        }
    }

    fun delete(characterId: RPKCharacterId): CompletableFuture<Void> = runAsync {
        database.create
            .deleteFrom(RPKIT_PAYMENT_GROUP_MEMBER)
            .where(RPKIT_PAYMENT_GROUP_MEMBER.CHARACTER_ID.eq(characterId.value))
            .execute()
    }.exceptionally { exception ->
        plugin.logger.log(SEVERE, "Failed to delete payment group members for character id", exception)
        throw exception
    }

}