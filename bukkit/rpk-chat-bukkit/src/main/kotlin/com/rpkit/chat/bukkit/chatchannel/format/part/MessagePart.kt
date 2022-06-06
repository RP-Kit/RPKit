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

package com.rpkit.chat.bukkit.chatchannel.format.part

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.format.click.ClickAction
import com.rpkit.chat.bukkit.chatchannel.format.hover.HoverAction
import com.rpkit.chat.bukkit.context.DirectedPreFormatMessageContext
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.TextComponent.fromLegacyText
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.CompletableFuture.supplyAsync

@SerializableAs("MessagePart")
class MessagePart(
    private val plugin: RPKChatBukkit,
    font: String? = null,
    color: String? = null,
    isBold: Boolean? = null,
    isItalic: Boolean? = null,
    isUnderlined: Boolean? = null,
    isStrikethrough: Boolean? = null,
    isObfuscated: Boolean? = null,
    insertion: String? = null,
    hover: HoverAction? = null,
    click: ClickAction? = null,
    private val enableLinkReplacement: Boolean = true,
    private val linkText: String = "[link]",
    private val linkFont: String? = null,
    private val linkColor: String? = null,
    private val linkBold: Boolean? = null,
    private val linkItalic: Boolean? = null,
    private val linkUnderlined: Boolean? = null,
    private val linkStrikethrough: Boolean? = null,
    private val linkObfuscated: Boolean? = null,
    private val linkInsertion: String? = null,
) : GenericTextPart(
    plugin,
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

    override fun getText(context: DirectedPreFormatMessageContext) = completedFuture(context.message)

    override fun toChatComponents(context: DirectedPreFormatMessageContext): CompletableFuture<Array<BaseComponent>> = supplyAsync {
        if (!enableLinkReplacement) return@supplyAsync fromLegacyText(getText(context).join()).applyFormatting(context)
        val message = getText(context).join()
        val urlRegex =
            Regex("((([A-Za-z]{3,9}:(?://)?)(?:[\\-;:&=+$,\\w]+@)?[A-Za-z\\d.\\-]+|(?:www\\.|[\\-;:&=+$,\\w]+@)[A-Za-z\\d.\\-]+)((?:/[+~%/.\\w\\-_]*)?\\??[\\-+=&;%@.\\w_]*#?[.!/\\\\\\w]*)?)")
        val matcher = urlRegex.toPattern().matcher(message)
        val components = mutableListOf<BaseComponent>()
        var index = 0
        var startIndex: Int
        var endIndex = 0
        while (matcher.find()) {
            startIndex = matcher.start()
            endIndex = matcher.end()
            if (startIndex > index) {
                components.addAll(fromLegacyText(message.substring(index, startIndex)).applyFormatting(context))
            }
            var link = message.substring(startIndex, endIndex)
            if (!link.contains("://")) link = "https://$link"
            components.add(
                TextComponent(linkText).apply {
                    if (linkFont != null) font = linkFont
                    if (linkColor != null) color = ChatColor.of(linkColor)
                    if (linkBold != null) isBold = linkBold
                    if (linkItalic != null) isItalic = linkItalic
                    if (linkUnderlined != null) isUnderlined = linkUnderlined
                    if (linkStrikethrough != null) isStrikethrough = linkStrikethrough
                    if (linkObfuscated != null) isObfuscated = linkObfuscated
                    if (linkInsertion != null) insertion = linkInsertion
                    clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, link)
                    hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(link))
                }
            )
            index = endIndex
        }
        if (endIndex <= message.length - 1) {
            components.addAll(fromLegacyText(message.substring(endIndex, message.length)).applyFormatting(context))
        }
        return@supplyAsync components.toTypedArray()
    }

    private fun Array<BaseComponent>.applyFormatting(context: DirectedPreFormatMessageContext) = apply {
        for (component in this) {
            if (font != null) component.font = font
            if (color != null) component.color = ChatColor.of(color)
            if (isBold != null) component.isBold = isBold
            if (isItalic != null) component.isItalic = isItalic
            if (isUnderlined != null) component.isUnderlined = isUnderlined
            if (isStrikethrough != null) component.isStrikethrough = isStrikethrough
            if (isObfuscated != null) component.isObfuscated = isObfuscated
            if (insertion != null) component.insertion = insertion
            if (hover != null) component.hoverEvent = hover.toHoverEvent(context).join()
            if (click != null) component.clickEvent = click.toClickEvent(context).join()
        }
    }

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
        "click" to click,
        "links" to mapOf(
            "enable-link-replacement" to enableLinkReplacement,
            "text" to linkText,
            "font" to linkFont,
            "color" to linkColor,
            "bold" to linkBold,
            "italic" to linkItalic,
            "underlined" to linkUnderlined,
            "strikethrough" to linkStrikethrough,
            "obfuscated" to linkObfuscated,
            "insertion" to linkInsertion
        )
    )

    companion object {
        @JvmStatic
        fun deserialize(serialized: Map<String, Any>) = MessagePart(
            Bukkit.getPluginManager().getPlugin("rpk-chat-bukkit") as RPKChatBukkit,
            serialized["font"] as? String,
            serialized["color"] as? String,
            serialized["bold"] as? Boolean,
            serialized["italic"] as? Boolean,
            serialized["underlined"] as? Boolean,
            serialized["strikethrough"] as? Boolean,
            serialized["obfuscated"] as? Boolean,
            serialized["insertion"] as? String,
            serialized["hover"] as? HoverAction,
            serialized["click"] as? ClickAction,
            (serialized["links"] as? Map<*, *>)?.get("enable-link-replacement") as? Boolean ?: true,
            (serialized["links"] as? Map<*, *>)?.get("text") as? String ?: "[link]",
            (serialized["links"] as? Map<*, *>)?.get("font") as? String,
            (serialized["links"] as? Map<*, *>)?.get("color") as? String,
            (serialized["links"] as? Map<*, *>)?.get("bold") as? Boolean,
            (serialized["links"] as? Map<*, *>)?.get("italic") as? Boolean,
            (serialized["links"] as? Map<*, *>)?.get("underlined") as? Boolean,
            (serialized["links"] as? Map<*, *>)?.get("strikethrough") as? Boolean,
            (serialized["links"] as? Map<*, *>)?.get("obfuscated") as? Boolean,
            (serialized["links"] as? Map<*, *>)?.get("insertion") as? String,
        )
    }
}