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

package com.rpkit.notifications.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.notifications.bukkit.RPKNotificationsBukkit
import com.rpkit.notifications.bukkit.database.create
import com.rpkit.notifications.bukkit.database.jooq.Tables.RPKIT_NOTIFICATION
import com.rpkit.notifications.bukkit.database.jooq.tables.records.RpkitNotificationRecord
import com.rpkit.notifications.bukkit.notification.RPKNotification
import com.rpkit.notifications.bukkit.notification.RPKNotificationId
import com.rpkit.notifications.bukkit.notification.RPKNotificationImpl
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.CompletableFuture.supplyAsync
import java.util.logging.Level

class RPKNotificationTable(private val database: Database, private val plugin: RPKNotificationsBukkit) : Table {

    fun get(id: RPKNotificationId): CompletableFuture<out RPKNotification?> {
        return supplyAsync {
            val result = database.create
                .selectFrom(RPKIT_NOTIFICATION)
                .where(RPKIT_NOTIFICATION.ID.eq(id.value))
                .fetchOne() ?: return@supplyAsync null
            return@supplyAsync result.toDomain()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get notification", exception)
            throw exception
        }
    }

    fun get(recipient: RPKProfile): CompletableFuture<out List<RPKNotification>> {
        return supplyAsync {
            val result = database.create
                .selectFrom(RPKIT_NOTIFICATION)
                .where(RPKIT_NOTIFICATION.RECIPIENT_ID.eq(recipient.id?.value))
                .fetch()
            return@supplyAsync result.mapNotNull { it.toDomain() }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to get notifications", exception)
            throw exception
        }
    }

    fun insert(notification: RPKNotification): CompletableFuture<Void> {
        return runAsync {
            database.create
                .insertInto(
                    RPKIT_NOTIFICATION,
                    RPKIT_NOTIFICATION.RECIPIENT_ID,
                    RPKIT_NOTIFICATION.TITLE,
                    RPKIT_NOTIFICATION.CONTENT,
                    RPKIT_NOTIFICATION.TIME,
                    RPKIT_NOTIFICATION.READ
                )
                .values(
                    notification.recipient.id?.value,
                    notification.title,
                    notification.content,
                    LocalDateTime.ofInstant(notification.time, UTC),
                    notification.read
                )
                .execute()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to insert notification", exception)
            throw exception
        }
    }

    fun update(notification: RPKNotification): CompletableFuture<Void> {
        return runAsync {
            database.create
                .update(RPKIT_NOTIFICATION)
                .set(RPKIT_NOTIFICATION.TITLE, notification.title)
                .set(RPKIT_NOTIFICATION.CONTENT, notification.content)
                .set(RPKIT_NOTIFICATION.TIME, LocalDateTime.ofInstant(notification.time, UTC))
                .set(RPKIT_NOTIFICATION.READ, notification.read)
                .where(RPKIT_NOTIFICATION.ID.eq(notification.id?.value))
                .execute()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to update notification", exception)
            throw exception
        }
    }

    fun delete(notification: RPKNotification): CompletableFuture<Void> {
        return runAsync {
            database.create
                .deleteFrom(RPKIT_NOTIFICATION)
                .where(RPKIT_NOTIFICATION.ID.eq(notification.id?.value))
                .execute()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to delete notification", exception)
            throw exception
        }
    }

    private fun RpkitNotificationRecord.toDomain(): RPKNotificationImpl? {
        val profileService = Services[RPKProfileService::class.java] ?: return null
        val recipient = profileService.getProfile(RPKProfileId(recipientId)).join() ?: return null
        return RPKNotificationImpl(
            RPKNotificationId(id),
            recipient,
            title,
            content,
            time.toInstant(UTC),
            read
        )
    }

}