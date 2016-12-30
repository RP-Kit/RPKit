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

package com.rpkit.permissions.bukkit.group

import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

/**
 * Group implementation.
 */
@SerializableAs("RPKGroupImpl")
data class RPKGroupImpl(
        override val name: String,
        override val allow: List<String>,
        override val deny: List<String>,
        override val inheritance: List<RPKGroup>
) : RPKGroup, ConfigurationSerializable {

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
                Pair("name", name),
                Pair("allow", allow),
                Pair("deny", deny),
                Pair("inheritance", inheritance)
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): RPKGroupImpl {
            return RPKGroupImpl(
                    serialized["name"] as String,
                    serialized["allow"] as List<String>,
                    serialized["deny"] as List<String>,
                    serialized["inheritance"] as List<RPKGroup>
            )
        }
    }

}