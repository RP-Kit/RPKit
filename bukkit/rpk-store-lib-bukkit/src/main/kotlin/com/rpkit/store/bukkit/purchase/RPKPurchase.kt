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

package com.rpkit.store.bukkit.purchase

import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.store.bukkit.storeitem.RPKStoreItem
import java.time.LocalDateTime

/**
 * Represents a purchase.
 * A purchase is tied to an individual profile.
 */
interface RPKPurchase {

    /**
     * The ID of the purchase.
     * Guaranteed to be unique.
     * Null if it has not yet been inserted into the database.
     */
    var id: Int?

    /**
     * The store item that has been purchased
     */
    val storeItem: RPKStoreItem

    /**
     * The profile that made the purchase
     */
    val profile: RPKProfile

    /**
     * The date at which the purchase was made
     */
    val purchaseDate: LocalDateTime

}