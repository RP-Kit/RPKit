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

package com.rpkit.payments.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationService
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Player join listener for sending payment notifications.
 */
class PlayerJoinListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return
        val characterService = Services[RPKCharacterService::class] ?: return
        val paymentNotificationService = Services[RPKPaymentNotificationService::class] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player) ?: return
        val character = characterService.getActiveCharacter(minecraftProfile) ?: return
        paymentNotificationService.getPaymentNotificationsFor(character).forEach { notification ->
            event.player.sendMessage(notification.text)
            paymentNotificationService.removePaymentNotification(notification)
        }
    }

}
