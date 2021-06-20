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

package com.rpkit.moderation.bukkit.ticket

import com.rpkit.core.location.RPKLocation
import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfile
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

/**
 * Provides ticket related functionality
 */
interface RPKTicketService : Service {

    /**
     * Gets a ticket by ID.
     *
     * @param id The ID to get
     * @return The ticket
     */
    fun getTicket(id: RPKTicketId): CompletableFuture<RPKTicket?>

    /**
     * Gets open tickets.
     *
     * @return All open tickets
     */
    fun getOpenTickets(): CompletableFuture<List<RPKTicket>>

    /**
     * Gets closed tickets.
     *
     * @return All closed tickets
     */
    fun getClosedTickets(): CompletableFuture<List<RPKTicket>>

    /**
     * Adds a ticket
     *
     * @param ticket The ticket to add
     */
    fun addTicket(ticket: RPKTicket): CompletableFuture<Void>

    fun createTicket(
        reason: String,
        issuer: RPKProfile,
        resolver: RPKProfile?,
        location: RPKLocation?,
        openDate: LocalDateTime,
        closeDate: LocalDateTime?,
        isClosed: Boolean
    ): CompletableFuture<RPKTicket>

    /**
     * Updates a ticket
     *
     * @param ticket The ticket to update
     */
    fun updateTicket(ticket: RPKTicket): CompletableFuture<Void>

    /**
     * Removes a ticket
     *
     * @param ticket The ticket to remove
     */
    fun removeTicket(ticket: RPKTicket): CompletableFuture<Void>

}