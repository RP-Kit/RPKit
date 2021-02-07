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

package com.rpkit.chat.bukkit.chatchannel.context

import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.chat.bukkit.context.DirectedPostFormatMessageContext
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKThinProfile
import net.md_5.bungee.api.chat.BaseComponent

class DirectedPostFormatMessageContextImpl(
        override val chatChannel: RPKChatChannel,
        override val senderProfile: RPKThinProfile,
        override val senderMinecraftProfile: RPKMinecraftProfile?,
        override val receiverMinecraftProfile: RPKMinecraftProfile,
        override var message: Array<BaseComponent>,
        override var isCancelled: Boolean = false
) : DirectedPostFormatMessageContext