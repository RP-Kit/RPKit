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

package com.rpkit.payments.bukkit.messages

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.economy.bukkit.currency.RPKCurrency
import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import com.rpkit.payments.bukkit.group.RPKPaymentGroup
import net.md_5.bungee.api.chat.*
import org.bukkit.ChatColor
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PaymentsMessages(plugin: RPKPaymentsBukkit) : BukkitMessages(plugin) {
    class PaymentListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKPaymentGroup, rank: String) = message.withParameters(
            "name" to group.name.value,
            "rank" to rank
        )
    }

    class PaymentInfoOwnerMessage(private val message: List<String>) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            group: RPKPaymentGroup,
            owners: List<RPKCharacter>,
            members: List<RPKCharacter>,
            invites: List<RPKCharacter>
        ) = message.map { line ->
            val messageComponents = mutableListOf<BaseComponent>()
            var chatColor: ChatColor? = null
            var chatFormat: ChatColor? = null
            var i = 0
            while (i < line.length) {
                if (line[i] == ChatColor.COLOR_CHAR) {
                    val colourOrFormat = ChatColor.getByChar(line[i + 1])
                    if (colourOrFormat?.isColor == true) {
                        chatColor = colourOrFormat
                        chatFormat = null
                    }
                    if (colourOrFormat?.isFormat == true) chatFormat = colourOrFormat
                    i += 1
                } else {
                    var fieldFound = false
                    if (line.length >= i + "\${name}".length) {
                        if (line.substring(i, i + "\${name}".length) == "\${name}") {
                            val textComponent = TextComponent(group.name.value)
                            if (chatColor != null) {
                                textComponent.color = chatColor.asBungee()
                            }
                            if (chatFormat != null) {
                                textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                textComponent.isBold = chatFormat == ChatColor.BOLD
                                textComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                                textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                textComponent.isItalic = chatFormat == ChatColor.ITALIC
                            }
                            messageComponents.add(textComponent)
                            i += "\${name}".length - 1
                            fieldFound = true
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${owners}".length) {
                            if (line.substring(i, i + "\${owners}".length) == "\${owners}") {
                                val hiddenOwners = owners.filter { it.isNameHidden }.size
                                val textComponent = TextComponent(
                                    owners
                                        .filter { owner -> !owner.isNameHidden }
                                        .map(RPKCharacter::name)
                                        .joinToString(", ")
                                            + if (hiddenOwners > 0) " (plus $hiddenOwners hidden)" else ""
                                )
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${owners}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${members}".length) {
                            if (line.substring(i, i + "\${members}".length) == "\${members}") {
                                val hiddenMembers = members.filter { it.isNameHidden }.size
                                val textComponent = TextComponent(
                                    members
                                        .filter { member -> !member.isNameHidden }
                                        .map(RPKCharacter::name)
                                        .joinToString(", ")
                                            + if (hiddenMembers > 0) " (plus $hiddenMembers hidden)" else ""
                                )
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${members}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${invites}".length) {
                            if (line.substring(i, i + "\${invites}".length) == "\${invites}") {
                                val textComponent = TextComponent(
                                    invites
                                        .filter { invite -> !invite.isNameHidden }
                                        .map(RPKCharacter::name)
                                        .joinToString(", ")
                                )
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${invites}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${amount}".length) {
                            if (line.substring(i, i + "\${amount}".length) == "\${amount}") {
                                if (group.currency != null) {
                                    messageComponents.add(
                                        TextComponent(
                                            "${group.amount} ${
                                                if (group.balance == 1)
                                                    group.currency?.nameSingular ?: ""
                                                else
                                                    group.currency?.namePlural ?: ""
                                            }"
                                        )
                                    )
                                } else {
                                    messageComponents.add(TextComponent("(Currency unset)"))
                                }
                                if (chatColor != null) {
                                    messageComponents.last().color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    messageComponents.last().isObfuscated =
                                        chatFormat == ChatColor.MAGIC
                                    messageComponents.last().isBold = chatFormat == ChatColor.BOLD
                                    messageComponents.last().isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    messageComponents.last().isUnderlined =
                                        chatFormat == ChatColor.UNDERLINE
                                    messageComponents.last().isItalic = chatFormat == ChatColor.ITALIC
                                }
                                i += "\${amount}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${currency}".length) {
                            if (line.substring(i, i + "\${currency}".length) == "\${currency}") {
                                val currency = group.currency
                                if (currency != null) {
                                    messageComponents.add(TextComponent(currency.name.value))
                                } else {
                                    messageComponents.add(TextComponent("unset"))
                                }
                                if (chatColor != null) {
                                    messageComponents.last().color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    messageComponents.last().isObfuscated =
                                        chatFormat == ChatColor.MAGIC
                                    messageComponents.last().isBold = chatFormat == ChatColor.BOLD
                                    messageComponents.last().isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    messageComponents.last().isUnderlined =
                                        chatFormat == ChatColor.UNDERLINE
                                    messageComponents.last().isItalic = chatFormat == ChatColor.ITALIC
                                }
                                i += "\${currency}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${interval}".length) {
                            if (line.substring(i, i + "\${interval}".length) == "\${interval}") {
                                val textComponent =
                                    TextComponent("${group.interval.toMillis() / 1000} seconds")
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${interval}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${last_payment_time}".length) {
                            if (line.substring(
                                    i,
                                    i + "\${last_payment_time}".length
                                ) == "\${last_payment_time}"
                            ) {
                                val textComponent = TextComponent(
                                    dateFormat.format(
                                        group.lastPaymentTime.atZone(
                                            ZoneId.systemDefault()
                                        )
                                    )
                                )
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${last_payment_time}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${balance}".length) {
                            if (line.substring(i, i + "\${balance}".length) == "\${balance}") {
                                val textComponent = TextComponent(
                                    if (group.currency != null) {
                                        "${group.balance} ${if (group.balance == 1) group.currency?.nameSingular ?: "" else group.currency?.namePlural ?: ""}"
                                    } else {
                                        "unset"
                                    }
                                )
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${balance}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        var editFound = false
                        if (line.length >= i + "\${edit(name)}".length) {
                            if (line.substring(i, i + "\${edit(name)}".length) == "\${edit(name)}") {
                                val textComponent = TextComponent("Edit")
                                textComponent.clickEvent = ClickEvent(
                                    ClickEvent.Action.RUN_COMMAND,
                                    "/payment set name ${group.name.value}"
                                )
                                textComponent.hoverEvent = HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    ComponentBuilder().appendLegacy("Click here to change the payment group name").create()
                                )
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${edit(name)}".length - 1
                                editFound = true
                            }
                        }
                        if (!editFound) {
                            if (line.length >= i + "\${edit(amount)}".length) {
                                if (line.substring(
                                        i,
                                        i + "\${edit(amount)}".length
                                    ) == "\${edit(amount)}"
                                ) {
                                    val textComponent = TextComponent("Edit")
                                    textComponent.clickEvent = ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/payment set amount ${group.name.value}"
                                    )
                                    textComponent.hoverEvent = HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        ComponentBuilder().appendLegacy("Click here to change the payment group amount").create()
                                    )
                                    if (chatColor != null) {
                                        textComponent.color = chatColor.asBungee()
                                    }
                                    if (chatFormat != null) {
                                        textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                        textComponent.isBold = chatFormat == ChatColor.BOLD
                                        textComponent.isStrikethrough =
                                            chatFormat == ChatColor.STRIKETHROUGH
                                        textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                        textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                    }
                                    messageComponents.add(textComponent)
                                    i += "\${edit(amount)}".length - 1
                                    editFound = true
                                }
                            }
                        }
                        if (!editFound) {
                            if (line.length >= i + "\${edit(currency)}".length) {
                                if (line.substring(
                                        i,
                                        i + "\${edit(currency)}".length
                                    ) == "\${edit(currency)}"
                                ) {
                                    val textComponent = TextComponent("Edit")
                                    textComponent.clickEvent = ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/payment set currency ${group.name.value}"
                                    )
                                    textComponent.hoverEvent = HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        ComponentBuilder().appendLegacy("Click here to change the payment group currency").create()
                                    )
                                    if (chatColor != null) {
                                        textComponent.color = chatColor.asBungee()
                                    }
                                    if (chatFormat != null) {
                                        textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                        textComponent.isBold = chatFormat == ChatColor.BOLD
                                        textComponent.isStrikethrough =
                                            chatFormat == ChatColor.STRIKETHROUGH
                                        textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                        textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                    }
                                    messageComponents.add(textComponent)
                                    i += "\${edit(currency)}".length - 1
                                    editFound = true
                                }
                            }
                        }
                        if (!editFound) {
                            if (line.length >= i + "\${edit(interval)}".length) {
                                if (line.substring(
                                        i,
                                        i + "\${edit(interval)}".length
                                    ) == "\${edit(interval)}"
                                ) {
                                    val textComponent = TextComponent("Edit")
                                    textComponent.clickEvent = ClickEvent(
                                        ClickEvent.Action.RUN_COMMAND,
                                        "/payment set interval ${group.name.value}"
                                    )
                                    textComponent.hoverEvent = HoverEvent(
                                        HoverEvent.Action.SHOW_TEXT,
                                        ComponentBuilder().appendLegacy("Click here to change the payment group interval").create()
                                    )
                                    if (chatColor != null) {
                                        textComponent.color = chatColor.asBungee()
                                    }
                                    if (chatFormat != null) {
                                        textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                        textComponent.isBold = chatFormat == ChatColor.BOLD
                                        textComponent.isStrikethrough =
                                            chatFormat == ChatColor.STRIKETHROUGH
                                        textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                        textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                    }
                                    messageComponents.add(textComponent)
                                    i += "\${edit(interval)}".length - 1
                                    editFound = true
                                }
                            }
                        }
                        if (!editFound) {
                            val textComponent = TextComponent(line[i].toString())
                            if (chatColor != null) {
                                textComponent.color = chatColor.asBungee()
                            }
                            if (chatFormat != null) {
                                textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                textComponent.isBold = chatFormat == ChatColor.BOLD
                                textComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                                textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                textComponent.isItalic = chatFormat == ChatColor.ITALIC
                            }
                            messageComponents.add(textComponent)
                        }
                    }
                }
                i++
            }
            messageComponents.toList()
        }
    }

    class PaymentInfoNotOwnerMessage(private val message: List<String>) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            group: RPKPaymentGroup,
            owners: List<RPKCharacter>,
            members: List<RPKCharacter>,
            invites: List<RPKCharacter>
        ) = message.map { line ->
            val messageComponents = mutableListOf<BaseComponent>()
            var chatColor: ChatColor? = null
            var chatFormat: ChatColor? = null
            var i = 0
            while (i < line.length) {
                if (line[i] == ChatColor.COLOR_CHAR) {
                    val colourOrFormat = ChatColor.getByChar(line[i + 1])
                    if (colourOrFormat?.isColor == true) {
                        chatColor = colourOrFormat
                        chatFormat = null
                    }
                    if (colourOrFormat?.isFormat == true) chatFormat = colourOrFormat
                    i += 1
                } else {
                    var fieldFound = false
                    if (line.length >= i + "\${name}".length) {
                        if (line.substring(i, i + "\${name}".length) == "\${name}") {
                            val textComponent = TextComponent(group.name.value)
                            if (chatColor != null) {
                                textComponent.color = chatColor.asBungee()
                            }
                            if (chatFormat != null) {
                                textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                textComponent.isBold = chatFormat == ChatColor.BOLD
                                textComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                                textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                textComponent.isItalic = chatFormat == ChatColor.ITALIC
                            }
                            messageComponents.add(textComponent)
                            i += "\${name}".length - 1
                            fieldFound = true
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${owners}".length) {
                            if (line.substring(i, i + "\${owners}".length) == "\${owners}") {
                                val hiddenOwners = owners.filter { it.isNameHidden }.size
                                val textComponent = TextComponent(
                                    owners
                                        .filter { owner -> !owner.isNameHidden }
                                        .map(RPKCharacter::name)
                                        .joinToString(", ")
                                            + if (hiddenOwners > 0) " (plus $hiddenOwners hidden)" else ""
                                )
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${owners}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${members}".length) {
                            if (line.substring(i, i + "\${members}".length) == "\${members}") {
                                val hiddenMembers = members.filter { it.isNameHidden }.size
                                val textComponent = TextComponent(
                                    members
                                        .filter { member -> !member.isNameHidden }
                                        .map(RPKCharacter::name)
                                        .joinToString(", ")
                                            + if (hiddenMembers > 0) " (plus $hiddenMembers hidden)" else ""
                                )
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${members}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${invites}".length) {
                            if (line.substring(i, i + "\${invites}".length) == "\${invites}") {
                                val textComponent = TextComponent(invites.joinToString(", "))
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${invites}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${amount}".length) {
                            if (line.substring(i, i + "\${amount}".length) == "\${amount}") {
                                if (group.currency != null) {
                                    messageComponents.add(TextComponent("${group.amount} ${if (group.balance == 1) group.currency?.nameSingular ?: "" else group.currency?.namePlural ?: ""}"))
                                } else {
                                    messageComponents.add(TextComponent("(Currency unset)"))
                                }
                                if (chatColor != null) {
                                    messageComponents.last().color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    messageComponents.last().isObfuscated =
                                        chatFormat == ChatColor.MAGIC
                                    messageComponents.last().isBold = chatFormat == ChatColor.BOLD
                                    messageComponents.last().isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    messageComponents.last().isUnderlined =
                                        chatFormat == ChatColor.UNDERLINE
                                    messageComponents.last().isItalic = chatFormat == ChatColor.ITALIC
                                }
                                i += "\${amount}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${currency}".length) {
                            if (line.substring(i, i + "\${currency}".length) == "\${currency}") {
                                val currency = group.currency
                                if (currency != null) {
                                    messageComponents.add(TextComponent(currency.name.value))
                                } else {
                                    messageComponents.add(TextComponent("unset"))
                                }
                                if (chatColor != null) {
                                    messageComponents.last().color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    messageComponents.last().isObfuscated =
                                        chatFormat == ChatColor.MAGIC
                                    messageComponents.last().isBold = chatFormat == ChatColor.BOLD
                                    messageComponents.last().isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    messageComponents.last().isUnderlined =
                                        chatFormat == ChatColor.UNDERLINE
                                    messageComponents.last().isItalic = chatFormat == ChatColor.ITALIC
                                }
                                i += "\${currency}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${interval}".length) {
                            if (line.substring(i, i + "\${interval}".length) == "\${interval}") {
                                val textComponent =
                                    TextComponent("${group.interval.toMillis() / 1000} seconds")
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${interval}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${last_payment_time}".length) {
                            if (line.substring(
                                    i,
                                    i + "\${last_payment_time}".length
                                ) == "\${last_payment_time}"
                            ) {
                                val textComponent = TextComponent(
                                    dateFormat.format(
                                        group.lastPaymentTime.atZone(
                                            ZoneId.systemDefault()
                                        )
                                    )
                                )
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${last_payment_time}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        if (line.length >= i + "\${balance}".length) {
                            if (line.substring(i, i + "\${balance}".length) == "\${balance}") {
                                val textComponent = TextComponent(
                                    if (group.currency != null) {
                                        "${group.balance} ${if (group.balance == 1) group.currency?.nameSingular ?: "" else group.currency?.namePlural ?: ""}"
                                    } else {
                                        "unset"
                                    }
                                )
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough =
                                        chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${balance}".length - 1
                                fieldFound = true
                            }
                        }
                    }
                    if (!fieldFound) {
                        val textComponent = TextComponent(line[i].toString())
                        if (chatColor != null) {
                            textComponent.color = chatColor.asBungee()
                        }
                        if (chatFormat != null) {
                            textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                            textComponent.isBold = chatFormat == ChatColor.BOLD
                            textComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                            textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                            textComponent.isItalic = chatFormat == ChatColor.ITALIC
                        }
                        messageComponents.add(textComponent)
                    }
                }
                i++
            }
            messageComponents.toList()
        }
    }

    class PaymentNotificationMemberFailToPayTitleMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationMemberFailToPayMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationOwnerFailToPayTitleMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationOwnerFailToPayMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationMemberFailToBePaidTitleMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationMemberFailToBePaidMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationMemberJoinTitleMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationMemberJoinMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationMemberLeaveTitleMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationMemberLeaveMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationInviteTitleMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationInviteMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationKickTitleMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class PaymentNotificationKickMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        fun withParameters(
            member: RPKCharacter,
            group: RPKPaymentGroup,
            date: ZonedDateTime
        ) = message.withParameters(
            "member" to member.name,
            "group" to group.name.value,
            "date" to dateFormat.format(date)
        )
    }

    class CurrencyListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            currency: RPKCurrency
        ) = message.withParameters(
            "currency" to currency.name.value
        )
    }

    val notFromConsole = get("not-from-console")
    val operationCancelled = get("operation-cancelled")
    val paymentUsage = get("payment-usage")
    val paymentCreateValid = get("payment-create-valid")
    val paymentCreateInvalidNameAlreadyExists = get("payment-create-invalid-name-already-exists")
    val paymentCreateUsage = get("payment-create-usage")
    val paymentRemoveValid = get("payment-remove-valid")
    val paymentRemoveInvalidPaymentGroup = get("payment-remove-invalid-payment-group")
    val paymentRemoveInvalidNotAnOwner = get("payment-remove-invalid-not-an-owner")
    val paymentRemoveUsage = get("payment-remove-usage")
    val paymentInviteValid = get("payment-invite-valid")
    val paymentInviteInvalidCharacter = get("payment-invite-invalid-character")
    val paymentInviteInvalidGroup = get("payment-invite-invalid-group")
    val paymentInviteUsage = get("payment-invite-usage")
    val paymentKickValid = get("payment-kick-valid")
    val paymentKickInvalidPlayer = get("payment-kick-invalid-player")
    val paymentKickInvalidCharacter = get("payment-kick-invalid-character")
    val paymentKickInvalidGroup = get("payment-kick-invalid-group")
    val paymentKickUsage = get("payment-kick-usage")
    val paymentJoinValid = get("payment-join-valid")
    val paymentJoinInvalidInvite = get("payment-join-invalid-invite")
    val paymentJoinInvalidCharacter = get("payment-join-invalid-character")
    val paymentJoinInvalidGroup = get("payment-join-invalid-group")
    val paymentJoinUsage = get("payment-join-usage")
    val paymentLeaveValid = get("payment-leave-valid")
    val paymentLeaveInvalidMember = get("payment-leave-invalid-member")
    val paymentLeaveInvalidCharacter = get("payment-leave-invalid-character")
    val paymentLeaveInvalidGroup = get("payment-leave-invalid-group")
    val paymentLeaveUsage = get("payment-leave-usage")
    val paymentWithdrawValid = get("payment-withdraw-valid")
    val paymentWithdrawInvalidBalance = get("payment-withdraw-invalid-balance")
    val paymentWithdrawInvalidAmount = get("payment-withdraw-invalid-amount")
    val paymentWithdrawInvalidCurrency = get("payment-withdraw-invalid-currency")
    val paymentWithdrawInvalidGroup = get("payment-withdraw-invalid-group")
    val paymentWithdrawInvalidCharacter = get("payment-withdraw-invalid-character")
    val paymentWithdrawInvalidOwner = get("payment-withdraw-invalid-owner")
    val paymentWithdrawUsage = get("payment-withdraw-usage")
    val paymentDepositValid = get("payment-deposit-valid")
    val paymentDepositInvalidBalance = get("payment-deposit-invalid-balance")
    val paymentDepositInvalidAmount = get("payment-deposit-invalid-amount")
    val paymentDepositInvalidCurrency = get("payment-deposit-invalid-currency")
    val paymentDepositInvalidGroup = get("payment-deposit-invalid-group")
    val paymentDepositInvalidCharacter = get("payment-deposit-invalid-character")
    val paymentDepositInvalidOwner = get("payment-deposit-invalid-owner")
    val paymentDepositUsage = get("payment-deposit-usage")
    val paymentListTitle = get("payment-list-title")
    val paymentListItem = getParameterized("payment-list-item")
        .let(::PaymentListItemMessage)
    val paymentInfoOwner = getList("payment-info-owner")
        .let(::PaymentInfoOwnerMessage)
    val paymentInfoNotOwnerMessage = getList("payment-info-not-owner")
        .let(::PaymentInfoNotOwnerMessage)
    val paymentInfoInvalidGroup = get("payment-info-invalid-group")
    val paymentInfoUsage = get("payment-info-usage")
    val paymentSetAmountInvalidOwner = get("payment-set-amount-invalid-owner")
    val paymentSetAmountInvalidGroup = get("payment-set-amount-invalid-group")
    val paymentSetAmountUsage = get("payment-set-amount-usage")
    val paymentSetAmountPrompt = get("payment-set-amount-prompt")
    val paymentSetAmountInvalidNumber = get("payment-set-amount-invalid-number")
    val paymentSetAmountValid = get("payment-set-amount-valid")
    val paymentSetCurrencyInvalidOwner = get("payment-set-currency-invalid-owner")
    val paymentSetCurrencyInvalidGroup = get("payment-set-currency-invalid-group")
    val paymentSetCurrencyUsage = get("payment-set-currency-usage")
    val paymentSetCurrencyPrompt = get("payment-set-currency-prompt")
    val paymentSetCurrencyInvalidCurrency = get("payment-set-currency-invalid-currency")
    val paymentSetCurrencyValid = get("payment-set-currency-valid")
    val paymentSetIntervalInvalidOwner = get("payment-set-interval-invalid-owner")
    val paymentSetIntervalInvalidGroup = get("payment-set-interval-invalid-group")
    val paymentSetIntervalUsage = get("payment-set-interval-usage")
    val paymentSetIntervalPrompt = get("payment-set-interval-prompt")
    val paymentSetIntervalInvalidValidation = get("payment-set-interval-invalid-validation")
    val paymentSetIntervalInvalidNumber = get("payment-set-interval-invalid-number")
    val paymentSetIntervalValid = get("payment-set-interval-valid")
    val paymentSetNameInvalidOwner = get("payment-set-name-invalid-owner")
    val paymentSetNameInvalidGroup = get("payment-set-name-invalid-group")
    val paymentSetNameInvalidNameAlreadyExists = get("payment-set-name-invalid-name-already-exists")
    val paymentSetNameUsage = get("payment-set-name-usage")
    val paymentSetNamePrompt = get("payment-set-name-prompt")
    val paymentSetNameValid = get("payment-set-name-valid")
    val paymentSetUsage = get("payment-set-usage")
    val paymentNotificationMemberFailToPayTitle = getParameterized("payment-notification-member-fail-to-pay-title")
        .let(::PaymentNotificationMemberFailToPayTitleMessage)
    val paymentNotificationMemberFailToPay = getParameterized("payment-notification-member-fail-to-pay")
        .let(::PaymentNotificationMemberFailToPayMessage)
    val paymentNotificationOwnerFailToPayTitle = getParameterized("payment-notification-owner-fail-to-pay-title")
        .let(::PaymentNotificationOwnerFailToPayTitleMessage)
    val paymentNotificationOwnerFailToPay = getParameterized("payment-notification-owner-fail-to-pay")
        .let(::PaymentNotificationOwnerFailToPayMessage)
    val paymentNotificationMemberFailToBePaidTitle = getParameterized("payment-notification-member-fail-to-be-paid-title")
        .let(::PaymentNotificationMemberFailToBePaidTitleMessage)
    val paymentNotificationMemberFailToBePaid = getParameterized("payment-notification-member-fail-to-be-paid")
        .let(::PaymentNotificationMemberFailToBePaidMessage)
    val paymentNotificationMemberJoinTitle = getParameterized("payment-notification-member-join-title")
        .let(::PaymentNotificationMemberJoinTitleMessage)
    val paymentNotificationMemberJoin = getParameterized("payment-notification-member-join")
        .let(::PaymentNotificationMemberJoinMessage)
    val paymentNotificationMemberLeaveTitle = getParameterized("payment-notification-member-leave-title")
        .let(::PaymentNotificationMemberLeaveTitleMessage)
    val paymentNotificationMemberLeave = getParameterized("payment-notification-member-leave")
        .let(::PaymentNotificationMemberLeaveMessage)
    val paymentNotificationInviteTitle = getParameterized("payment-notification-invite-title")
        .let(::PaymentNotificationInviteTitleMessage)
    val paymentNotificationInvite = getParameterized("payment-notification-invite")
        .let(::PaymentNotificationInviteMessage)
    val paymentNotificationKickTitle = getParameterized("payment-notification-kick-title")
        .let(::PaymentNotificationKickTitleMessage)
    val paymentNotificationKick = getParameterized("payment-notification-kick")
        .let(::PaymentNotificationKickMessage)
    val currencyListTitle = get("currency-list-title")
    val currencyListItem = getParameterized("currency-list-item")
        .let(::CurrencyListItemMessage)
    val noProfile = get("no-profile")
    val noMinecraftProfile = get("no-minecraft-profile")
    val noCharacter = get("no-character")
    val noPermissionPaymentSetAmount = get("no-permission-payment-set-amount")
    val noPermissionPaymentSetCurrency = get("no-permission-payment-set-currency")
    val noPermissionPaymentSetInterval = get("no-permission-payment-set-interval")
    val noPermissionPaymentSetName = get("no-permission-payment-set-name")
    val noPermissionPaymentCreate = get("no-permission-payment-create")
    val noPermissionPaymentRemove = get("no-permission-payment-remove")
    val noPermissionPaymentDeposit = get("no-permission-payment-deposit")
    val noPermissionPaymentInfo = get("no-permission-payment-info")
    val noPermissionPaymentInvite = get("no-permission-payment-invite")
    val noPermissionPaymentJoin = get("no-permission-payment-join")
    val noPermissionPaymentKick = get("no-permission-payment-kick")
    val noPermissionPaymentLeave = get("no-permission-payment-leave")
    val noPermissionPaymentList = get("no-permission-payment-list")
    val noPermissionPaymentWithdraw = get("no-permission-payment-withdraw")
    val noPaymentGroupService = get("no-payment-group-service")
    val noCurrencyService = get("no-currency-service")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noCharacterService = get("no-character-service")
    val noBankService = get("no-bank-service")
    val noEconomyService = get("no-economy-service")
    val noNotificationService = get("no-notification-service")
}