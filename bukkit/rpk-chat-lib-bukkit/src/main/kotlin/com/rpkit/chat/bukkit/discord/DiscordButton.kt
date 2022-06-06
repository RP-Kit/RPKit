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

package com.rpkit.chat.bukkit.discord

sealed interface DiscordButton {
    val id: String
    val variant: Variant
    val onClick: DiscordButtonClickListener

    enum class Variant {
        PRIMARY,
        SUCCESS,
        SECONDARY,
        DANGER,
        LINK
    }
}

class DiscordTextButton(
    override val id: String,
    override val variant: DiscordButton.Variant,
    val text: String,
    override val onClick: DiscordButtonClickListener
) : DiscordButton

class DiscordEmojiButton(
    override val id: String,
    override val variant: DiscordButton.Variant,
    val emoji: String,
    override val onClick: DiscordButtonClickListener
) : DiscordButton