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

package com.seventh_root.elysium.payments.bukkit.group

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import com.seventh_root.elysium.payments.bukkit.ElysiumPaymentsBukkit
import com.seventh_root.elysium.payments.bukkit.database.table.ElysiumPaymentGroupInviteTable
import com.seventh_root.elysium.payments.bukkit.database.table.ElysiumPaymentGroupMemberTable
import com.seventh_root.elysium.payments.bukkit.database.table.ElysiumPaymentGroupOwnerTable
import com.seventh_root.elysium.payments.bukkit.group.invite.ElysiumPaymentGroupInvite
import com.seventh_root.elysium.payments.bukkit.group.member.ElysiumPaymentGroupMember
import com.seventh_root.elysium.payments.bukkit.group.owner.ElysiumPaymentGroupOwner

/**
 * Payment group implementation.
 */
class ElysiumPaymentGroupImpl(
        val plugin: ElysiumPaymentsBukkit,
        override var id: Int = 0,
        override var name: String,
        override var amount: Int,
        override var currency: ElysiumCurrency?,
        override var interval: Long,
        override var lastPaymentTime: Long,
        override var balance: Int
): ElysiumPaymentGroup {

    override val owners: List<ElysiumCharacter>
        get() = plugin.core.database.getTable(ElysiumPaymentGroupOwnerTable::class).get(this).map { owner -> owner.character }

    override val members: List<ElysiumCharacter>
        get() = plugin.core.database.getTable(ElysiumPaymentGroupMemberTable::class).get(this).map { owner -> owner.character }

    override val invites: List<ElysiumCharacter>
        get() = plugin.core.database.getTable(ElysiumPaymentGroupInviteTable::class).get(this).map { owner -> owner.character }

    override fun addOwner(character: ElysiumCharacter) {
        plugin.core.database.getTable(ElysiumPaymentGroupOwnerTable::class).insert(ElysiumPaymentGroupOwner(paymentGroup = this, character = character))
    }

    override fun removeOwner(character: ElysiumCharacter) {
        val owner = plugin.core.database.getTable(ElysiumPaymentGroupOwnerTable::class).get(this).filter { it.character == character }.firstOrNull()
        if (owner != null) {
            plugin.core.database.getTable(ElysiumPaymentGroupOwnerTable::class).get(this).filter { it.character == character }.firstOrNull()
        }
    }

    override fun addMember(character: ElysiumCharacter) {
        plugin.core.database.getTable(ElysiumPaymentGroupMemberTable::class).insert(ElysiumPaymentGroupMember(paymentGroup = this, character = character))
    }

    override fun removeMember(character: ElysiumCharacter) {
        val member = plugin.core.database.getTable(ElysiumPaymentGroupMemberTable::class).get(this).filter { it.character == character }.firstOrNull()
        if (member != null) {
            plugin.core.database.getTable(ElysiumPaymentGroupMemberTable::class).delete(member)
        }
    }

    override fun addInvite(character: ElysiumCharacter) {
        plugin.core.database.getTable(ElysiumPaymentGroupInviteTable::class).insert(ElysiumPaymentGroupInvite(paymentGroup = this, character = character))
    }

    override fun removeInvite(character: ElysiumCharacter) {
        val invite = plugin.core.database.getTable(ElysiumPaymentGroupInviteTable::class).get(this).filter { it.character == character }.firstOrNull()
        if (invite != null) {
            plugin.core.database.getTable(ElysiumPaymentGroupInviteTable::class).delete(invite)
        }
    }

}