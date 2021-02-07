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

package com.rpkit.essentials.bukkit.messages

import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit

class EssentialsMessages(plugin: RPKEssentialsBukkit) : BukkitMessages(plugin) {

    class SaveItemValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(name: String) = message.withParameters("name" to name)
    }
    val saveItemUsage = get("save-item-usage")
    val saveItemValid = getParameterized("save-item-valid").let(::SaveItemValidMessage)
    val noPermissionSaveItem = get("no-permission-save-item")
    val notFromConsole = get("not-from-console")
}