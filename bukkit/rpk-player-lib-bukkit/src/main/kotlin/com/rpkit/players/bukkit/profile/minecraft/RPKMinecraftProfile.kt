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

package com.rpkit.players.bukkit.profile.minecraft

import com.rpkit.core.bukkit.command.sender.RPKBukkitCommandSender
import com.rpkit.players.bukkit.profile.RPKThinProfile
import net.md_5.bungee.api.chat.BaseComponent
import java.util.*


interface RPKMinecraftProfile : RPKBukkitCommandSender {

    var id: Int?
    var profile: RPKThinProfile
    val minecraftUUID: UUID
    override val name: String
    val isOnline: Boolean

    override fun sendMessage(message: String)
    override fun sendMessage(messages: Array<String>)
    override fun sendMessage(vararg components: BaseComponent)
    override fun hasPermission(permission: String): Boolean

}