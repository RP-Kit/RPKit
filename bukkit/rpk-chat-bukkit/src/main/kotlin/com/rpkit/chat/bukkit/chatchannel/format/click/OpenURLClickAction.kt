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

package com.rpkit.chat.bukkit.chatchannel.format.click

import com.rpkit.chat.bukkit.context.DirectedPreFormatMessageContext
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.OPEN_URL
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("OpenURLClickAction")
class OpenURLClickAction(val url: String) : ClickAction, ConfigurationSerializable {
    override fun toClickEvent(context: DirectedPreFormatMessageContext) = ClickEvent(
            OPEN_URL,
            url
    )

    override fun serialize() = mutableMapOf(
            "url" to url
    )

    companion object {
        @JvmStatic fun deserialize(serialized: Map<String, Any>) = OpenURLClickAction(
                serialized["url"] as String
        )
    }
}