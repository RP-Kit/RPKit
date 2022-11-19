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

package com.rpkit.notifications.bukkit.test

import com.rpkit.core.database.Database
import com.rpkit.core.service.Services
import com.rpkit.notifications.bukkit.RPKNotificationsBukkit
import com.rpkit.notifications.bukkit.database.table.RPKNotificationTable
import com.rpkit.notifications.bukkit.notification.RPKNotification
import com.rpkit.notifications.bukkit.notification.RPKNotificationId
import com.rpkit.notifications.bukkit.notification.RPKNotificationServiceImpl
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.concurrent.CompletableFuture.completedFuture

class RPKNotificationServiceTest : WordSpec({
    val recipient = mockk<RPKProfile>()
    val plugin = mockk<RPKNotificationsBukkit>()
    val database = mockk<Database>()
    val notificationTable = mockk<RPKNotificationTable>()
    every { database.getTable(RPKNotificationTable::class.java) } returns notificationTable
    val notificationService = RPKNotificationServiceImpl(plugin, database)
    "Notification service" should {
        "return notifications for a profile" {
            val notifications = listOf(mockk<RPKNotification>())
            every { notificationTable.get(recipient) } returns completedFuture(notifications)
            notificationService.getNotifications(recipient).join() shouldBe notifications
        }
        "return notification by id" {
            val id = RPKNotificationId(1)
            val notification = mockk<RPKNotification>()
            every { notificationTable.get(id) } returns completedFuture(notification)
            notificationService.getNotification(id).join() shouldBe notification
        }
        "delete notification" {
            val notification = mockk<RPKNotification>()
            every { notificationTable.delete(notification) } returns completedFuture(null)
            notificationService.removeNotification(notification).join()
            verify { notificationTable.delete(notification) }
        }
        "create notification" {
            Services.delegate = mockk {
                every { get(RPKMinecraftProfileService::class.java) } returns mockk {
                    every { getMinecraftProfiles(any()) } returns completedFuture(listOf(
                        mockk()
                    ))
                }
            }
            every { notificationTable.insert(any()) } returns completedFuture(null)
            notificationService.createNotification(
                recipient,
                "test title",
                "test content",
            ).join() should {
                it.recipient shouldBe recipient
                it.title shouldBe "test title"
                it.content shouldBe "test content"
                it.read shouldBe false
            }
        }
        "add notification" {
            every { notificationTable.insert(any()) } returns completedFuture(null)
            val notification = mockk<RPKNotification>()
            notificationService.addNotification(notification)
            verify { notificationTable.insert(notification) }
        }
    }
})