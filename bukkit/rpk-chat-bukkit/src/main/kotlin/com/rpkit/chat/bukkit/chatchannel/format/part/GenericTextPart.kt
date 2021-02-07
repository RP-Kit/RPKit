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

import com.rpkit.chat.bukkit.chatchannel.format.FormatPart
import com.rpkit.chat.bukkit.chatchannel.format.click.ClickAction
import com.rpkit.chat.bukkit.chatchannel.format.hover.HoverAction
import com.rpkit.chat.bukkit.context.DirectedPreFormatMessageContext
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent.fromLegacyText

abstract class GenericTextPart(
        val font: String? = null,
        val color: String? = null,
        val isBold: Boolean? = null,
        val isItalic: Boolean? = null,
        val isUnderlined: Boolean? = null,
        val isStrikethrough: Boolean? = null,
        val isObfuscated: Boolean? = null,
        val insertion: String? = null,
        val hover: HoverAction? = null,
        val click: ClickAction? = null
) : FormatPart {

    abstract fun getText(context: DirectedPreFormatMessageContext): String

    override fun toChatComponents(context: DirectedPreFormatMessageContext) = fromLegacyText(getText(context)).also {
        for (component in it) {
            if (font != null) component.font = font
            if (color != null) component.color = ChatColor.of(color)
            if (isBold != null) component.isBold = isBold
            if (isItalic != null) component.isItalic = isItalic
            if (isUnderlined != null) component.isUnderlined = isUnderlined
            if (isStrikethrough != null) component.isStrikethrough = isStrikethrough
            if (isObfuscated != null) component.isObfuscated = isObfuscated
            if (insertion != null) component.insertion = insertion
            if (hover != null) component.hoverEvent = hover.toHoverEvent(context)
            if (click != null) component.clickEvent = click.toClickEvent(context)
        }
    }
}