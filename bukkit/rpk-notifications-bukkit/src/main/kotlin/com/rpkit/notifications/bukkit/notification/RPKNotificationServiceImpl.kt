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

import com.rpkit.core.database.Database
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.notifications.bukkit.database.table.RPKNotificationTable
import com.rpkit.players.bukkit.profile.RPKProfile
import java.time.Instant
import java.util.concurrent.CompletableFuture

class RPKNotificationServiceImpl(override val plugin: RPKPlugin, private val database: Database) : RPKNotificationService {
    override fun getNotifications(recipient: RPKProfile): CompletableFuture<out List<RPKNotification>> {
        return database.getTable(RPKNotificationTable::class.java).get(recipient)
    }

    override fun getNotification(notificationId: RPKNotificationId): CompletableFuture<out RPKNotification?> {
        return database.getTable(RPKNotificationTable::class.java).get(notificationId)
    }

    override fun addNotification(notification: RPKNotification): CompletableFuture<Void> {
        return database.getTable(RPKNotificationTable::class.java).insert(notification)
    }

    override fun createNotification(
        recipient: RPKProfile,
        title: String,
        content: String
    ): CompletableFuture<RPKNotification> {
        val notification = RPKNotificationImpl(
            null,
            recipient,
            title,
            content,
            Instant.now(),
            false
        )
        return addNotification(notification).thenApply { notification }
    }

    override fun updateNotification(notification: RPKNotification): CompletableFuture<Void> {
        return database.getTable(RPKNotificationTable::class.java).update(notification)
    }

    override fun removeNotification(notification: RPKNotification): CompletableFuture<Void> {
        return database.getTable(RPKNotificationTable::class.java).delete(notification)
    }
}