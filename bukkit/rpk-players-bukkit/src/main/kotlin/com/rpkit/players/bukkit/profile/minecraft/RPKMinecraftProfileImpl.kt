/*
 * Copyright 2021 Ren Binden
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

import com.rpkit.players.bukkit.profile.RPKThinProfile
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Bukkit
import java.util.UUID

class RPKMinecraftProfileImpl(
    override var id: RPKMinecraftProfileId? = null,
    override var profile: RPKThinProfile,
    override val minecraftUUID: UUID
) : RPKMinecraftProfile {

    override val isOnline: Boolean
        get() = Bukkit.getOfflinePlayer(minecraftUUID).isOnline

    override val minecraftUsername: RPKMinecraftUsername
        get() = RPKMinecraftUsername(name)

    override val name: String
        get() = Bukkit.getOfflinePlayer(minecraftUUID).name ?: ""

    override fun sendMessage(message: String) {
        Bukkit.getPlayer(minecraftUUID)?.sendMessage(message)
    }

    override fun sendMessage(messages: Array<String>) {
        Bukkit.getPlayer(minecraftUUID)?.sendMessage(messages)
    }

    override fun hasPermission(permission: String): Boolean {
        return Bukkit.getPlayer(minecraftUUID)?.hasPermission(permission) == true
    }

    override fun sendMessage(vararg components: BaseComponent) {
        Bukkit.getPlayer(minecraftUUID)?.spigot()?.sendMessage(*components)
    }

}
