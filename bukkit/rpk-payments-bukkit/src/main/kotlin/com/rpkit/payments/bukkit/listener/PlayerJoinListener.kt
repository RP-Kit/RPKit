/*
 * Copyright 2016 Ross Binden
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

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.notification.RPKPaymentNotificationProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Player join listener for sending payment notifications.
 */
class PlayerJoinListener(private val plugin: RPKPaymentsBukkit): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val paymentNotificationProvider = plugin.core.serviceManager.getServiceProvider(RPKPaymentNotificationProvider::class)
        val player = playerProvider.getPlayer(event.player)
        val character = characterProvider.getActiveCharacter(player)
        if (character != null) {
            paymentNotificationProvider.getPaymentNotificationsFor(character).forEach { notification ->
                event.player.sendMessage(notification.text)
                paymentNotificationProvider.removePaymentNotification(notification)
            }
        }
    }

}
