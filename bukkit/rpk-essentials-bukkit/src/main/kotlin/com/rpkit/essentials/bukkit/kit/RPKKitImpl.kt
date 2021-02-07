/*
 * Copyright 2021 Ren Binden
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

package com.rpkit.essentials.bukkit.kit

import com.rpkit.kit.bukkit.kit.RPKKit
import com.rpkit.kit.bukkit.kit.RPKKitName
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack


class RPKKitImpl(
    override val name: RPKKitName,
    override val items: List<ItemStack>
) : RPKKit, ConfigurationSerializable {
    override fun serialize(): Map<String, Any> {
        return mapOf(
                "name" to name.value,
                "items" to items
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>): RPKKitImpl {
            return RPKKitImpl(
                    RPKKitName(serialized["name"] as String),
                    serialized["items"] as List<ItemStack>
            )
        }
    }

}