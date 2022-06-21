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

package com.rpkit.food.bukkit.messages

import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.food.bukkit.RPKFoodBukkit
import java.time.Duration

class FoodMessages(plugin: RPKFoodBukkit) : BukkitMessages(plugin) {

    class ExpiryViewValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(duration: Duration): String {
            return message.withParameters(
                "days" to duration.toDays().toString(),
                "hours" to duration.toHoursPart().toString(),
                "minutes" to duration.toMinutesPart().toString(),
                "seconds" to duration.toSecondsPart().toString()
            )
        }
    }

    val expiryUsage = get("expiry-usage")
    val expiryViewInvalidNoExpiry = get("expiry-view-invalid-no-expiry")
    val expiryViewInvalidExpired = get("expiry-view-invalid-expired")
    val expiryViewValid = getParameterized("expiry-view-valid")
        .let(::ExpiryViewValidMessage)
    val expirySetUsage = get("expiry-set-usage")
    val expirySetInvalidHours = get("expiry-set-invalid-hours")
    val expirySetInvalidInedibleItem = get("expiry-set-invalid-inedible-item")
    val expirySetValid = get("expiry-set-valid")
    val notFromConsole = get("not-from-console")
    val noExpiryService = get("no-expiry-service")
    val noPermissionExpiryView = get("no-permission-expiry-view")
    val noPermissionExpirySet = get("no-permission-expiry-set")

}