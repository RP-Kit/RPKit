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

package com.rpkit.permissions.bukkit.permissions

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Service
import com.rpkit.permissions.bukkit.group.RPKGroup
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

interface RPKPermissionsService : Service {

    /**
     * Checks whether a group has a permissions node
     *
     * @param group The group
     * @param node The permissions node to check
     * @return Whether the group has the given permissions node
     */
    fun hasPermission(group: RPKGroup, node: String): Boolean

    /**
     * Checks whether a group has a permissions node, using a specified default
     *
     * @param group The group
     * @param node The permissions node to check
     * @param default The default to use if the group does not have the node set
     * @return Whether the group has the given permissions node when assuming the provided default
     */
    fun hasPermission(group: RPKGroup, node: String, default: Boolean): Boolean

    /**
     * Checks whether a profile has a permissions node
     */
    fun hasPermission(profile: RPKProfile, node: String): CompletableFuture<Boolean>

    /**
     * Checks whether a character has a permissions node
     */
    fun hasPermission(character: RPKCharacter, node: String): CompletableFuture<Boolean>

    /**
     * Assigns permissions to a player in Minecraft
     *
     * @param minecraftProfile The Minecraft profile to assign permissions to
     */
    fun assignPermissions(minecraftProfile: RPKMinecraftProfile)

    /**
     * Unassigns permissions from a player in Minecraft
     *
     * @param minecraftProfile
     */
    fun unassignPermissions(minecraftProfile: RPKMinecraftProfile, bukkitPlayer: Player)

}