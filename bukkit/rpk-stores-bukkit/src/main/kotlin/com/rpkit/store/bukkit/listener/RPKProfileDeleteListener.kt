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

package com.rpkit.store.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileDeleteEvent
import com.rpkit.store.bukkit.purchase.RPKPurchaseService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKProfileDeleteListener : Listener {

    @EventHandler
    fun onProfileDelete(event: RPKBukkitProfileDeleteEvent) {
        val purchaseService = Services[RPKPurchaseService::class.java] ?: return
        purchaseService.getPurchases(event.profile).thenAccept { purchases ->
            purchases.forEach { purchase ->
                purchaseService.removePurchase(purchase)
            }
        }
    }

}