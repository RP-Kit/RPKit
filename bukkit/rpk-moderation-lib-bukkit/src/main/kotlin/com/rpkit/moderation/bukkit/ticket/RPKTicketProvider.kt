/*
 * Copyright 2018 Ross Binden
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

package com.rpkit.moderation.bukkit.ticket

import com.rpkit.core.service.ServiceProvider

/**
 * Provides ticket related functionality
 */
interface RPKTicketProvider: ServiceProvider {

    /**
     * Gets a ticket by ID.
     *
     * @param id The ID to get
     * @return The ticket
     */
    fun getTicket(id: Int): RPKTicket?

    /**
     * Gets open tickets.
     *
     * @return All open tickets
     */
    fun getOpenTickets(): List<RPKTicket>

    /**
     * Gets closed tickets.
     *
     * @return All closed tickets
     */
    fun getClosedTickets(): List<RPKTicket>

    /**
     * Adds a ticket
     *
     * @param ticket The ticket to add
     */
    fun addTicket(ticket: RPKTicket)

    /**
     * Updates a ticket
     *
     * @param ticket The ticket to update
     */
    fun updateTicket(ticket: RPKTicket)

    /**
     * Removes a ticket
     *
     * @param ticket The ticket to remove
     */
    fun removeTicket(ticket: RPKTicket)

}