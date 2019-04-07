/*
 * Copyright 2016 Ross Binden
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

package com.rpkit.payments.bukkit.group

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.table.RPKPaymentGroupInviteTable
import com.rpkit.payments.bukkit.database.table.RPKPaymentGroupMemberTable
import com.rpkit.payments.bukkit.database.table.RPKPaymentGroupOwnerTable
import com.rpkit.payments.bukkit.group.invite.RPKPaymentGroupInvite
import com.rpkit.payments.bukkit.group.member.RPKPaymentGroupMember
import com.rpkit.payments.bukkit.group.owner.RPKPaymentGroupOwner

/**
 * Payment group implementation.
 */
class RPKPaymentGroupImpl(
        val plugin: RPKPaymentsBukkit,
        override var id: Int = 0,
        override var name: String,
        override var amount: Int,
        override var currency: RPKCurrency?,
        override var interval: Long,
        override var lastPaymentTime: Long,
        override var balance: Int
): RPKPaymentGroup {

    override val owners: List<RPKCharacter>
        get() = plugin.core.database.getTable(RPKPaymentGroupOwnerTable::class).get(this).map { owner -> owner.character }

    override val members: List<RPKCharacter>
        get() = plugin.core.database.getTable(RPKPaymentGroupMemberTable::class).get(this).map { owner -> owner.character }

    override val invites: List<RPKCharacter>
        get() = plugin.core.database.getTable(RPKPaymentGroupInviteTable::class).get(this).map { owner -> owner.character }

    override fun addOwner(character: RPKCharacter) {
        plugin.core.database.getTable(RPKPaymentGroupOwnerTable::class).insert(RPKPaymentGroupOwner(paymentGroup = this, character = character))
    }

    override fun removeOwner(character: RPKCharacter) {
        val owner = plugin.core.database.getTable(RPKPaymentGroupOwnerTable::class).get(this)
                .firstOrNull { it.character == character }
        if (owner != null) {
            plugin.core.database.getTable(RPKPaymentGroupOwnerTable::class).get(this)
                    .firstOrNull { it.character == character }
        }
    }

    override fun addMember(character: RPKCharacter) {
        plugin.core.database.getTable(RPKPaymentGroupMemberTable::class).insert(RPKPaymentGroupMember(paymentGroup = this, character = character))
    }

    override fun removeMember(character: RPKCharacter) {
        val member = plugin.core.database.getTable(RPKPaymentGroupMemberTable::class).get(this)
                .firstOrNull { it.character == character }
        if (member != null) {
            plugin.core.database.getTable(RPKPaymentGroupMemberTable::class).delete(member)
        }
    }

    override fun addInvite(character: RPKCharacter) {
        plugin.core.database.getTable(RPKPaymentGroupInviteTable::class).insert(RPKPaymentGroupInvite(paymentGroup = this, character = character))
    }

    override fun removeInvite(character: RPKCharacter) {
        val invite = plugin.core.database.getTable(RPKPaymentGroupInviteTable::class).get(this)
                .firstOrNull { it.character == character }
        if (invite != null) {
            plugin.core.database.getTable(RPKPaymentGroupInviteTable::class).delete(invite)
        }
    }

}