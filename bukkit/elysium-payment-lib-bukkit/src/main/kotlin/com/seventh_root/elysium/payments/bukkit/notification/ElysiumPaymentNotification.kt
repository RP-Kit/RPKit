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

package com.seventh_root.elysium.payments.bukkit.notification

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.core.database.Entity
import com.seventh_root.elysium.payments.bukkit.group.ElysiumPaymentGroup

/**
 * Represents a payment notification.
 * A payment notification is created when a character fails to pay or is failed to be paid by a payment group.
 * Upon players coming online, the payment notifications are sent and cleared from the DB.
 */
interface ElysiumPaymentNotification: Entity {

    /**
     * The group for which the payment notification concerns.
     */
    val group: ElysiumPaymentGroup

    /**
     * The character which the notification is sent to.
     */
    val to: ElysiumCharacter

    /**
     * The character which triggered this notification.
     */
    val character: ElysiumCharacter

    /**
     * The date at which the notification occurred.
     */
    val date: Long

    /**
     * The text of the notification.
     */
    val text: String

}