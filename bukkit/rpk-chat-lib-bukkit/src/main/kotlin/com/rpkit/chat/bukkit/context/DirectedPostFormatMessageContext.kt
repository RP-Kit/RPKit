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

package com.rpkit.chat.bukkit.context

import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKThinProfile
import net.md_5.bungee.api.chat.BaseComponent

interface DirectedPostFormatMessageContext {
    /**
     * The chat channel the message is being sent to.
     */
    val chatChannel: RPKChatChannel

    /**
     * The sender of the message.
     */
    val senderProfile: RPKThinProfile

    /**
     * Which Minecraft profile the sender used to send the message, if any.
     */
    val senderMinecraftProfile: RPKMinecraftProfile?

    /**
     * The receiver of the message.
     */
    val receiverMinecraftProfile: RPKMinecraftProfile

    /**
     * The message. Changing the message results in the updated message being propagated to all further pipeline
     * components.
     */
    var message: Array<BaseComponent>

    /**
     * Whether the message has been cancelled.
     * If the message is cancelled, further pipeline components may choose whether they still wish to operate on the
     * message or not.
     */
    var isCancelled: Boolean
}