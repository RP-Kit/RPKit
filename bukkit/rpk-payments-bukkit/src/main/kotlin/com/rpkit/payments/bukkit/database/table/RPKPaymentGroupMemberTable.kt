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

package com.rpkit.payments.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.payments.bukkit.database.jooq.Tables.RPKIT_PAYMENT_GROUP_MEMBER
import com.rpkit.payments.bukkit.group.RPKPaymentGroup
import com.rpkit.payments.bukkit.group.member.RPKPaymentGroupMember

/**
 * Represents payment group member table.
 */
class RPKPaymentGroupMemberTable(
        private val database: Database
) : Table {

    fun insert(entity: RPKPaymentGroupMember) {
        database.create
                .insertInto(
                        RPKIT_PAYMENT_GROUP_MEMBER,
                        RPKIT_PAYMENT_GROUP_MEMBER.PAYMENT_GROUP_ID,
                        RPKIT_PAYMENT_GROUP_MEMBER.CHARACTER_ID
                )
                .values(
                        entity.paymentGroup.id,
                        entity.character.id
                )
                .execute()
    }

    operator fun get(paymentGroup: RPKPaymentGroup): List<RPKPaymentGroupMember> {
        val results = database.create
                .select(RPKIT_PAYMENT_GROUP_MEMBER.CHARACTER_ID)
                .from(RPKIT_PAYMENT_GROUP_MEMBER)
                .where(RPKIT_PAYMENT_GROUP_MEMBER.PAYMENT_GROUP_ID.eq(paymentGroup.id))
                .fetch()
        val characterService = Services[RPKCharacterService::class] ?: return emptyList()
        return results.mapNotNull { result ->
            val character = characterService.getCharacter(result[RPKIT_PAYMENT_GROUP_MEMBER.CHARACTER_ID]) ?: return@mapNotNull null
            return@mapNotNull RPKPaymentGroupMember(
                    paymentGroup,
                    character
            )
        }
    }

    fun delete(entity: RPKPaymentGroupMember) {
        database.create
                .deleteFrom(RPKIT_PAYMENT_GROUP_MEMBER)
                .where(RPKIT_PAYMENT_GROUP_MEMBER.PAYMENT_GROUP_ID.eq(entity.paymentGroup.id))
                .and(RPKIT_PAYMENT_GROUP_MEMBER.CHARACTER_ID.eq(entity.character.id))
                .execute()
    }

}