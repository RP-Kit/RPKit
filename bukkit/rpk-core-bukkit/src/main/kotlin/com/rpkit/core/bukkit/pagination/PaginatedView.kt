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

package com.rpkit.core.bukkit.pagination

import com.rpkit.core.bukkit.command.sender.RPKBukkitCommandSender
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.command.CommandSender

class PaginatedView private constructor(
    private val title: Array<out BaseComponent>,
    private val lines: List<Array<out BaseComponent>>,
    private val previousPageText: String,
    private val previousPageHover: String,
    private val nextPageText: String,
    private val nextPageHover: String,
    private val pageText: GetPageTextFunction,
    pageLength: Int = 10,
    val viewPageCommand: ViewPageCommandFunction
) {

    fun interface GetPageTextFunction {
        operator fun invoke(page: Int): String
    }

    fun interface ViewPageCommandFunction {
        operator fun invoke(page: Int): String
    }

    val pages = lines.chunked(pageLength)

    companion object {
        @JvmStatic
        fun fromStrings(
            title: String,
            lines: List<String>,
            previousPageText: String,
            previousPageHover: String,
            nextPageText: String,
            nextPageHover: String,
            pageText: GetPageTextFunction,
            pageLength: Int = 10,
            viewPageCommand: ViewPageCommandFunction
        ) = PaginatedView(
            TextComponent.fromLegacyText(title),
            lines.map(TextComponent::fromLegacyText),
            previousPageText,
            previousPageHover,
            nextPageText,
            nextPageHover,
            pageText,
            pageLength,
            viewPageCommand
        )

        @JvmStatic
        fun fromChatComponents(
            title: Array<out BaseComponent>,
            lines: List<Array<out BaseComponent>>,
            previousPageText: String,
            previousPageHover: String,
            nextPageText: String,
            nextPageHover: String,
            pageText: GetPageTextFunction,
            pageLength: Int = 10,
            viewPageCommand: ViewPageCommandFunction
        ) = PaginatedView(
            title,
            lines,
            previousPageText,
            previousPageHover,
            nextPageText,
            nextPageHover,
            pageText,
            pageLength,
            viewPageCommand
        )
    }

    private fun renderPage(pageNumber: Int = 1): List<Array<out BaseComponent>> {
        val pageIndex = pageNumber - 1
        return buildList {
            add(title)
            if (pageIndex in pages.indices) {
                addAll(pages[pageIndex])
            }
            add(buildList {
                if (pageNumber - 1 >= 1) {
                    addAll(TextComponent.fromLegacyText(previousPageText).map { component ->
                        component.apply {
                            clickEvent = ClickEvent(
                                RUN_COMMAND,
                                viewPageCommand(pageNumber - 1)
                            )
                            hoverEvent = HoverEvent(
                                SHOW_TEXT,
                                Text(TextComponent.fromLegacyText(previousPageHover))
                            )
                        }
                    })
                    add(TextComponent(" - ").apply {
                        color = GRAY
                    })
                }
                addAll(TextComponent.fromLegacyText(pageText(pageNumber)))
                if (pageNumber + 1 < pages.size + 1) {
                    add(TextComponent(" - ").apply {
                        color = GRAY
                    })
                    addAll(TextComponent.fromLegacyText(nextPageText).map { component ->
                        component.apply {
                            clickEvent = ClickEvent(
                                RUN_COMMAND,
                                viewPageCommand(pageNumber + 1)
                            )
                            hoverEvent = HoverEvent(
                                SHOW_TEXT,
                                Text(TextComponent.fromLegacyText(nextPageHover))
                            )
                        }
                    })
                }
            }.toTypedArray())
        }
    }

    fun isPageValid(pageNumber: Int): Boolean {
        return (pageNumber - 1) in pages.indices
    }

    fun sendPage(sender: CommandSender, pageNumber: Int = 0) {
        renderPage(pageNumber).forEach { line ->
            sender.spigot().sendMessage(*line)
        }
    }

    fun sendPage(sender: RPKBukkitCommandSender, pageNumber: Int = 0) {
        renderPage(pageNumber).forEach { line ->
            sender.sendMessage(*line)
        }
    }

}