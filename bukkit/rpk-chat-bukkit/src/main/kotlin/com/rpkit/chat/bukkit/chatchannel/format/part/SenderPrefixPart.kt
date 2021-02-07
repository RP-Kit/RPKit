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

package com.rpkit.chat.bukkit.chatchannel.format.part

import com.rpkit.chat.bukkit.chatchannel.format.click.ClickAction
import com.rpkit.chat.bukkit.chatchannel.format.hover.HoverAction
import com.rpkit.chat.bukkit.context.DirectedPreFormatMessageContext
import com.rpkit.chat.bukkit.prefix.RPKPrefixService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("SenderPrefixPart")
class SenderPrefixPart(
        font: String? = null,
        color: String? = null,
        isBold: Boolean? = null,
        isItalic: Boolean? = null,
        isUnderlined: Boolean? = null,
        isStrikethrough: Boolean? = null,
        isObfuscated: Boolean? = null,
        insertion: String? = null,
        hover: HoverAction? = null,
        click: ClickAction? = null
) : GenericTextPart(
        font,
        color,
        isBold,
        isItalic,
        isUnderlined,
        isStrikethrough,
        isObfuscated,
        insertion,
        hover,
        click
), ConfigurationSerializable {

    override fun getText(context: DirectedPreFormatMessageContext) = (context.senderProfile as? RPKProfile)
            ?.let { Services[RPKPrefixService::class.java]?.getPrefix(it) }
            ?: ""

    override fun serialize() = mutableMapOf(
            "font" to font,
            "color" to color,
            "bold" to isBold,
            "italic" to isItalic,
            "underlined" to isUnderlined,
            "strikethrough" to isStrikethrough,
            "obfuscated" to isObfuscated,
            "insertion" to insertion,
            "hover" to hover,
            "click" to click
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = SenderPrefixPart(
                serialized["font"] as? String,
                serialized["color"] as? String,
                serialized["bold"] as? Boolean,
                serialized["italic"] as? Boolean,
                serialized["underlined"] as? Boolean,
                serialized["strikethrough"] as? Boolean,
                serialized["obfuscated"] as? Boolean,
                serialized["insertion"] as? String,
                serialized["hover"] as? HoverAction,
                serialized["click"] as? ClickAction
        )
    }
}