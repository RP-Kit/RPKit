/*
 * Copyright 2024 Ren Binden
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

package com.rpkit.notifications.bukkit.messages

import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.notifications.bukkit.RPKNotificationsBukkit
import com.rpkit.notifications.bukkit.notification.RPKNotification
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificationsMessages(plugin: RPKNotificationsBukkit): BukkitMessages(plugin) {

    class NotificationListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            notification: RPKNotification
        ) = message.withParameters(
            "id" to notification.id?.value.toString(),
            "recipient" to notification.recipient.name.value,
            "title" to notification.title,
            "content" to notification.content,
            "time" to DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(notification.time),
            "read" to notification.read.toString()
        )
    }

    class NotificationViewValidMessage(private val messages: List<ParameterizedMessage>) {
        fun withParameters(
            notification: RPKNotification
        ) = messages.map {
            it.withParameters(
                "id" to notification.id?.value.toString(),
                "recipient" to notification.recipient.name.value,
                "title" to notification.title,
                "content" to notification.content,
                "time" to DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(notification.time),
                "read" to notification.read.toString()
            )
        }
    }

    class NewNotificationsMessage(private val message: ParameterizedMessage) {
        fun withParameters(amount: Int) = message.withParameters(
            "amount" to amount.toString()
        )
    }

    class PageMessage(private val message: ParameterizedMessage) {
        fun withParameters(pageNumber: Int) = message.withParameters(
            "page_number" to pageNumber.toString()
        )
    }

    class InvalidPageMessage(private val message: ParameterizedMessage) {
        fun withParameters(pageNumber: Int) = message.withParameters(
            "page_number" to pageNumber.toString()
        )
    }

    class NotificationReceivedMessage(private val message: ParameterizedMessage) {
        fun withParameters(notification: RPKNotification) = message.withParameters(
            "id" to notification.id?.value.toString(),
            "recipient" to notification.recipient.name.value,
            "title" to notification.title,
            "content" to notification.content,
            "time" to DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                .withZone(ZoneId.systemDefault())
                .format(notification.time),
            "read" to notification.read.toString()
        )
    }

    val notificationUsage = get("notification-usage")
    val notificationDismissUsage = get("notification-dismiss-usage")
    val notificationDismissInvalidNotificationIdNotANumber = get("notification-dismiss-invalid-notification-id-not-a-number")
    val notificationDismissInvalidRecipient = get("notification-dismiss-invalid-recipient")
    val notificationDismissInvalidNotification = get("notification-dismiss-invalid-notification")
    val notificationDismissValid = get("notification-dismiss-valid")
    val notificationViewInvalidRecipient = get("notification-view-invalid-recipient")
    val notificationViewInvalidNotification = get("notification-view-invalid-notification")
    val notificationViewValid = getParameterizedList("notification-view-valid")
        .let(::NotificationViewValidMessage)
    val notificationViewViewList = get("notification-view-view-list")
    val notificationViewViewListHover = get("notification-view-view-list-hover")
    val notificationViewDismiss = get("notification-view-dismiss")
    val notificationViewDismissHover = get("notification-view-dismiss-hover")
    val notificationListTitle = get("notification-list-title")
    val notificationListItem = getParameterized("notification-list-item")
        .let(::NotificationListItemMessage)
    val notificationListItemHover = get("notification-list-item-hover")
    val notificationListNoNotifications = get("notification-list-no-notifications")
    val notFromConsole = get("not-from-console")
    val noProfileSelf = get("no-profile-self")
    val noNotificationService = get("no-notification-service")
    val noPermissionNotificationDismiss = get("no-permission-notification-dismiss")
    val noPermissionNotificationList = get("no-permission-notification-list")
    val noPermissionNotificationView = get("no-permission-notification-view")
    val newNotifications = getParameterized("new-notifications").let(::NewNotificationsMessage)
    val newNotificationsHover = get("new-notifications-hover")
    val previousPage = get("previous-page")
    val previousPageHover = get("previous-page-hover")
    val nextPage = get("next-page")
    val nextPageHover = get("next-page-hover")
    val page = getParameterized("page").let(::PageMessage)
    val invalidPage = getParameterized("invalid-page").let(::InvalidPageMessage)
    val notificationReceived = getParameterized("notification-received").let(::NotificationReceivedMessage)

}