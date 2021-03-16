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
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

/**
 * Represents a payment group.
 * Payments are made to or from the balance of the group by its members. Owners may deposit and withdraw from the balance.
 */
interface RPKPaymentGroup {

    /**
     * The ID of the payment group.
     * Guaranteed to be unique.
     * Null if the payment group has not yet been inserted.
     */
    var id: RPKPaymentGroupId?

    /**
     * The name of the group.
     */
    var name: RPKPaymentGroupName

    /**
     * The owners of the group. These characters are notified by all actions concerning the group.
     * They may also withdraw and deposit money into the group balance, and may invite or kick members from the group.
     */
    val owners: CompletableFuture<List<RPKCharacter>>

    /**
     * The members of the group. These characters are paid to/from the balance of the payment group.
     * They are notified by actions concerning them, e.g. being failed to be paid, or failing to pay.
     */
    val members: CompletableFuture<List<RPKCharacter>>

    /**
     * The characters invited to the group.
     * These characters may join the group if they wish.
     */
    val invites: CompletableFuture<List<RPKCharacter>>

    /**
     * The amount of money given to the characters in the group at the specified interval.
     * If the amount is negative, the amount will be taken from the characters and added to the group's balance instead.
     * If the transaction fails, a notification is sent to all owners and the member involved in the transaction.
     */
    var amount: Int

    /**
     * The currency in which payments should occur.
     * If set to null, payments should not occur yet, as the payment group has not been fully set up.
     */
    var currency: RPKCurrency?

    /**
     * The interval at which payments should occur.
     * Measured in milliseconds.
     */
    var interval: Duration

    /**
     * The last payment time as a system timestamp.
     * This should be checked just before doing a payout or collection, and updated every time a payout or collection
     * occurs.
     */
    var lastPaymentTime: LocalDateTime

    /**
     * The balance of the payment group.
     * This may deposited and withdrawn from owners of the group.
     * It must always be enough to cover all payments, or a notification will be sent to members involved and owners.
     * It is increased upon collections and decreased upon payouts.
     */
    var balance: Int

    /**
     * Adds an owner to the payment group.
     *
     * @param character The owner to add
     */
    fun addOwner(character: RPKCharacter)

    /**
     * Removes an owner from the payment group.
     *
     * @param character The owner to remove
     */
    fun removeOwner(character: RPKCharacter): CompletableFuture<Void>

    /**
     * Adds a member to the payment group.
     *
     * @param character The member to add
     */
    fun addMember(character: RPKCharacter)

    /**
     * Removes a member from the payment group.
     *
     * @param character The member to remove
     */
    fun removeMember(character: RPKCharacter): CompletableFuture<Void>

    /**
     * Invites a character to the payment group.
     *
     * @param character The character to invite
     */
    fun addInvite(character: RPKCharacter)

    /**
     * Uninvites a character from the payment group.
     *
     * @param character The character to uninvite
     */
    fun removeInvite(character: RPKCharacter): CompletableFuture<Void>

}