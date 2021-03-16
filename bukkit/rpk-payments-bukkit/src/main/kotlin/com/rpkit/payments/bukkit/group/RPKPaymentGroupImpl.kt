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

package com.rpkit.payments.bukkit.group

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.database.table.RPKPaymentGroupInviteTable
import com.rpkit.payments.bukkit.database.table.RPKPaymentGroupMemberTable
import com.rpkit.payments.bukkit.database.table.RPKPaymentGroupOwnerTable
import com.rpkit.payments.bukkit.event.group.*
import com.rpkit.payments.bukkit.group.invite.RPKPaymentGroupInvite
import com.rpkit.payments.bukkit.group.member.RPKPaymentGroupMember
import com.rpkit.payments.bukkit.group.owner.RPKPaymentGroupOwner
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

/**
 * Payment group implementation.
 */
class RPKPaymentGroupImpl(
        val plugin: RPKPaymentsBukkit,
        override var id: RPKPaymentGroupId? = null,
        override var name: RPKPaymentGroupName,
        override var amount: Int,
        override var currency: RPKCurrency?,
        override var interval: Duration,
        override var lastPaymentTime: LocalDateTime,
        override var balance: Int
) : RPKPaymentGroup {

    override val owners: CompletableFuture<List<RPKCharacter>>
        get() = plugin.database.getTable(RPKPaymentGroupOwnerTable::class.java)[this]
            .thenApply { it.map { owner -> owner.character } }

    override val members: CompletableFuture<List<RPKCharacter>>
        get() = plugin.database.getTable(RPKPaymentGroupMemberTable::class.java)[this]
            .thenApply { it.map { owner -> owner.character } }

    override val invites: CompletableFuture<List<RPKCharacter>>
        get() = plugin.database.getTable(RPKPaymentGroupInviteTable::class.java)[this]
            .thenApply { it.map { owner -> owner.character } }

    override fun addOwner(character: RPKCharacter) {
        val event = RPKBukkitPaymentGroupOwnerAddEvent(this, character)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKPaymentGroupOwnerTable::class.java).insert(RPKPaymentGroupOwner(paymentGroup = event.paymentGroup, character = event.character))
    }

    override fun removeOwner(character: RPKCharacter): CompletableFuture<Void> {
        val event = RPKBukkitPaymentGroupOwnerRemoveEvent(this, character)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            val ownerTable = plugin.database.getTable(RPKPaymentGroupOwnerTable::class.java)
            val owner =
                ownerTable[event.paymentGroup].thenApply { owners -> owners.firstOrNull { it.character == event.character } }.join()
            if (owner != null) {
                ownerTable.delete(owner).join()
            }
        }
    }

    override fun addMember(character: RPKCharacter) {
        val event = RPKBukkitPaymentGroupMemberAddEvent(this, character)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKPaymentGroupMemberTable::class.java).insert(RPKPaymentGroupMember(paymentGroup = event.paymentGroup, character = event.character))
    }

    override fun removeMember(character: RPKCharacter): CompletableFuture<Void> {
        val event = RPKBukkitPaymentGroupMemberRemoveEvent(this, character)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return CompletableFuture.completedFuture(null)
        val memberTable = plugin.database.getTable(RPKPaymentGroupMemberTable::class.java)
        return memberTable[event.paymentGroup].thenAccept { members ->
            val member = members.firstOrNull { it.character == event.character }
            if (member != null) {
                memberTable.delete(member)
            }
        }
    }

    override fun addInvite(character: RPKCharacter) {
        val event = RPKBukkitPaymentGroupInviteEvent(this, character)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKPaymentGroupInviteTable::class.java).insert(RPKPaymentGroupInvite(paymentGroup = event.paymentGroup, character = event.character))
    }

    override fun removeInvite(character: RPKCharacter): CompletableFuture<Void> {
        val event = RPKBukkitPaymentGroupUninviteEvent(this, character)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return CompletableFuture.completedFuture(null)
        return plugin.database.getTable(RPKPaymentGroupInviteTable::class.java)[event.paymentGroup].thenAccept { invites ->
            val invite = invites.firstOrNull { it.character == event.character }
            if (invite != null) {
                plugin.database.getTable(RPKPaymentGroupInviteTable::class.java).delete(invite)
            }
        }
    }

}