/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.food.bukkit.expiry

import com.rpkit.core.service.Service
import org.bukkit.inventory.ItemStack
import java.time.Duration
import java.time.OffsetDateTime

/**
 * Represents an expiry service.
 * Provides functions to set and retrieve the expiry state of items.
 */
interface RPKExpiryService : Service {

    /**
     * Sets the expiry of the item to the duration
     *
     * @param item The item. This item will be modified by this function to have the expiry information
     * @param duration The duration. The expiry date will be calculated as the current time plus the given duration
     */
    fun setExpiry(item: ItemStack, duration: Duration)

    /**
     * Sets the expiry date of the item to the given expiry date
     *
     * @param item The item. This item will be modified by this function to have the expiry information
     * @param expiryDate The date at which the item should expire
     */
    fun setExpiry(item: ItemStack, expiryDate: OffsetDateTime)

    /**
     * Sets the expiry of the item to the default expiry.
     *
     * @param item The item. This item will be modified by this function to have the expiry information
     */
    fun setExpiry(item: ItemStack)

    /**
     * Gets the expiry of the item.
     * If the item has no expiry, this will return null.
     *
     * @param item The item
     * @return The time at which the item expires, or null if the item does not have an expiry date.
     */
    fun getExpiry(item: ItemStack): OffsetDateTime?

    /**
     * Determines if an item is expired.
     *
     * @param item The item
     * @return Whether the item has expired
     */
    fun isExpired(item: ItemStack): Boolean

}
