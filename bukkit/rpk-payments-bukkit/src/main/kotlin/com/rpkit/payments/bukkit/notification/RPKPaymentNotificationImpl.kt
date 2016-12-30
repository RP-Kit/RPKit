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

package com.rpkit.payments.bukkit.notification

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.payments.bukkit.group.RPKPaymentGroup

/**
 * Payment notification implementation.
 */
class RPKPaymentNotificationImpl(
        override var id: Int = 0,
        override val group: RPKPaymentGroup,
        override val to: RPKCharacter,
        override val character: RPKCharacter,
        override val date: Long,
        override val text: String
): RPKPaymentNotification
