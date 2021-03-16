/*
 * Copyright 2021 Ren Binden
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
import com.rpkit.payments.bukkit.database.create
import com.rpkit.payments.bukkit.database.jooq.Tables.RPKIT_PAYMENT_GROUP_INVITE
import com.rpkit.payments.bukkit.group.RPKPaymentGroup
import com.rpkit.payments.bukkit.group.invite.RPKPaymentGroupInvite
import java.util.concurrent.CompletableFuture

/**
 * Represents payment group invite table.
 */
class RPKPaymentGroupInviteTable(
        private val database: Database
) : Table {

    fun insert(entity: RPKPaymentGroupInvite) {
        val paymentGroupId = entity.paymentGroup.id ?: return
        val characterId = entity.character.id ?: return
        database.create
                .insertInto(
                        RPKIT_PAYMENT_GROUP_INVITE,
                        RPKIT_PAYMENT_GROUP_INVITE.PAYMENT_GROUP_ID,
                        RPKIT_PAYMENT_GROUP_INVITE.CHARACTER_ID
                )
                .values(
                        paymentGroupId.value,
                        characterId.value
                )
                .execute()
    }

    operator fun get(paymentGroup: RPKPaymentGroup): CompletableFuture<List<RPKPaymentGroupInvite>> {
        val paymentGroupId = paymentGroup.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(RPKIT_PAYMENT_GROUP_INVITE.CHARACTER_ID)
                .from(RPKIT_PAYMENT_GROUP_INVITE)
                .where(RPKIT_PAYMENT_GROUP_INVITE.PAYMENT_GROUP_ID.eq(paymentGroupId.value))
                .fetch()
            val characterService = Services[RPKCharacterService::class.java] ?: return@supplyAsync emptyList()
            return@supplyAsync results
                .mapNotNull { result ->
                    val character =
                        characterService.getCharacter(RPKCharacterId(result[RPKIT_PAYMENT_GROUP_INVITE.CHARACTER_ID])).join()
                            ?: return@mapNotNull null
                    return@mapNotNull RPKPaymentGroupInvite(paymentGroup, character)
                }
        }
    }

    fun delete(entity: RPKPaymentGroupInvite) {
        val paymentGroupId = entity.paymentGroup.id ?: return
        val characterId = entity.character.id ?: return
        database.create
                .deleteFrom(RPKIT_PAYMENT_GROUP_INVITE)
                .where(RPKIT_PAYMENT_GROUP_INVITE.PAYMENT_GROUP_ID.eq(paymentGroupId.value))
                .and(RPKIT_PAYMENT_GROUP_INVITE.CHARACTER_ID.eq(characterId.value))
                .execute()
    }

}