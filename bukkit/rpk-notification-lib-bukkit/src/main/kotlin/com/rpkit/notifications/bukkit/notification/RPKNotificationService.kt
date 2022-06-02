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

package com.rpkit.notifications.bukkit.notification

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfile
import java.util.concurrent.CompletableFuture

interface RPKNotificationService : Service {
    fun getNotifications(recipient: RPKProfile): CompletableFuture<out List<RPKNotification>>
    fun getNotification(notificationId: RPKNotificationId): CompletableFuture<out RPKNotification?>
    fun addNotification(notification: RPKNotification): CompletableFuture<Void>
    fun createNotification(
        recipient: RPKProfile,
        title: String,
        content: String
    ): CompletableFuture<RPKNotification>
    fun updateNotification(notification: RPKNotification): CompletableFuture<Void>
    fun removeNotification(notification: RPKNotification): CompletableFuture<Void>
}