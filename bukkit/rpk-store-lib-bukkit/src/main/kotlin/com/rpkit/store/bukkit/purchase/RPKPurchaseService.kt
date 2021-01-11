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

package com.rpkit.store.bukkit.purchase

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfile

/**
 * Provides purchase related operations.
 */
interface RPKPurchaseService : Service {

    /**
     * Gets a list of purchases by a given profile
     *
     * @param profile The profile to get purchases for
     * @return A list of purchases made by the given profile. If no purchases, an empty list is returned.
     */
    fun getPurchases(profile: RPKProfile): List<RPKPurchase>

    /**
     * Gets a purchase by ID
     *
     * @param id The id of the purchase
     * @return The purchase, or null if there are none with the given ID
     */
    fun getPurchase(id: RPKPurchaseId): RPKPurchase?

    /**
     * Adds a purchase
     *
     * @param purchase The purchase to add
     */
    fun addPurchase(purchase: RPKPurchase)

    /**
     * Updates a purchase in data storage
     *
     * @param purchase the purchase to update
     */
    fun updatePurchase(purchase: RPKPurchase)

    /**
     * Removes a purchase
     *
     * @param purchase The purchase to remove
     */
    fun removePurchase(purchase: RPKPurchase)

}