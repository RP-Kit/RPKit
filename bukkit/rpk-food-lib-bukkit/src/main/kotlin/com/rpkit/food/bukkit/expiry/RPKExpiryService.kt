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

package com.rpkit.food.bukkit.expiry

import com.rpkit.core.service.Service
import org.bukkit.inventory.ItemStack
import java.util.*

interface RPKExpiryService : Service {

    fun setExpiry(item: ItemStack, expiryDate: Date)
    fun setExpiry(item: ItemStack)
    fun getExpiry(item: ItemStack): Date?
    fun isExpired(item: ItemStack): Boolean

}
