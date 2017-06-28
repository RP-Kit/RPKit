/*
 * Copyright 2016 Ross Binden
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

package com.rpkit.permissions.bukkit.group

import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKProfile

/**
 * Provides group related operations.
 */
interface RPKGroupProvider: ServiceProvider {

    /**
     * A list of groups managed by this group provider.
     */
    val groups: List<RPKGroup>

    /**
     * Gets a group by name.
     * If there is no group with the given name, null is returned.
     *
     * @param name The name of the group
     * @return The group
     */
    fun getGroup(name: String): RPKGroup?

    /**
     * Adds a group to a player.
     *
     * @param player The player
     * @param group The group to add
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("addGroup(profile, group)"))
    fun addGroup(player: RPKPlayer, group: RPKGroup)

    /**
     * Adds a group to a profile.
     *
     * @param profile The profile
     * @param group The group to add
     */
    fun addGroup(profile: RPKProfile, group: RPKGroup)

    /**
     * Removes a group from a player.
     *
     * @param player The player
     * @param group The group to remove
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("removeGroup(profile, group)"))
    fun removeGroup(player: RPKPlayer, group: RPKGroup)

    /**
     * Removes a group from a profile.
     *
     * @param profile The profile
     * @param group The group to remove
     */
    fun removeGroup(profile: RPKProfile, group: RPKGroup)

    /**
     * Gets groups assigned to a player.
     *
     * @param player The player
     * @return A list of groups assigned to the player
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("getGroups(profile)"))
    fun getGroups(player: RPKPlayer): List<RPKGroup>

    /**
     * Gets groups assigned to a profile.
     *
     * @param profile The profile
     * @return A list of groups assigned to the profile
     */
    fun getGroups(profile: RPKProfile): List<RPKGroup>

    /**
     * Checks whether a group has a permissions node
     *
     * @param group The group
     * @param node The permissions node to check
     * @return Whether the group has the given permissions node
     */
    fun hasPermission(group: RPKGroup, node: String): Boolean

    /**
     * Checks whether a player has a permissions node
     *
     * @param player The player
     * @param node The permissions node to check
     * @return Whether the player has the given permissions node
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("hasPermission(profile, node)"))
    fun hasPermission(player: RPKPlayer, node: String): Boolean

    /**
     * Checks whether a profile has a permissions node
     */
    fun hasPermission(profile: RPKProfile, node: String): Boolean

    /**
     * Assigns permissions to a player in Minecraft
     *
     * @param player The player to assign permissions to
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("assignPermissions(minecraftProfile)"))
    fun assignPermissions(player: RPKPlayer)

    /**
     * Unassigns permissions from a player in Minecraft
     *
     * @param player The player to unassign permissions from
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("unassignPermissions(minecraftProfile)"))
    fun unassignPermissions(player: RPKPlayer)

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
    fun unassignPermissions(minecraftProfile: RPKMinecraftProfile)

}