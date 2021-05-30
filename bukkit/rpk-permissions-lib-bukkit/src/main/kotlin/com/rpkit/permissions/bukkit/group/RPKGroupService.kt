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

package com.rpkit.permissions.bukkit.group

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfile
import java.util.concurrent.CompletableFuture

/**
 * Provides group related operations.
 */
interface RPKGroupService : Service {

    /**
     * A list of groups managed by this group service.
     */
    val groups: List<RPKGroup>

    /**
     * Gets a group by name.
     * If there is no group with the given name, null is returned.
     *
     * @param name The name of the group
     * @return The group
     */
    fun getGroup(name: RPKGroupName): RPKGroup?

    /**
     * Adds a group to a profile.
     *
     * @param profile The profile
     * @param group The group to add
     * @param priority The priority at which to add the group, a higher priority value means the group will be assigned earlier
     */
    fun addGroup(profile: RPKProfile, group: RPKGroup, priority: Int): CompletableFuture<Void>

    /**
     * Adds a group to a profile, at the lowest priority.
     * @param profile The profile
     * @param group The group to add
     */
    fun addGroup(profile: RPKProfile, group: RPKGroup): CompletableFuture<Void>

    /**
     * Adds a group to a character.
     * The group will only be applied while using this character.
     *
     * @param character The character
     * @param group The group to add
     */
    fun addGroup(character: RPKCharacter, group: RPKGroup, priority: Int): CompletableFuture<Void>

    /**
     * Adds a group to a character, at the lowest priority.
     * @param character The character
     * @param group The group to add
     */
    fun addGroup(character: RPKCharacter, group: RPKGroup): CompletableFuture<Void>

    /**
     * Removes a group from a profile.
     *
     * @param profile The profile
     * @param group The group to remove
     */
    fun removeGroup(profile: RPKProfile, group: RPKGroup): CompletableFuture<Void>

    /**
     * Removes a group from a character.
     * The group will only be removed from the individual character - if applied at the profile level, this method will
     * not do anything, and you should pass the profile instead of the character.
     *
     * @param character The character
     * @param group The group to remove
     */
    fun removeGroup(character: RPKCharacter, group: RPKGroup): CompletableFuture<Void>

    /**
     * Gets groups assigned to a profile.
     *
     * @param profile The profile
     * @return A list of groups assigned to the profile
     */
    fun getGroups(profile: RPKProfile): CompletableFuture<List<RPKGroup>>

    /**
     * Gets preloaded groups assigned to a profile.
     * If the groups have not been loaded, returns null.
     * The groups must first be loaded with [loadGroups]. This should happen when a player is online using this profile.
     *
     * @param profile The profile
     * @return A list of preloaded groups assigned to the profile
     */
    fun getPreloadedGroups(profile: RPKProfile): List<RPKGroup>?

    /**
     * Loads the groups assigned to a profile.
     * This makes them available for calls to [getPreloadedGroups].
     * This should be called by the permissions plugin when a player is logging on using the profile.
     *
     * @param profile The profile
     */
    fun loadGroups(profile: RPKProfile): CompletableFuture<List<RPKGroup>>

    /**
     * Unloads the groups assigned to a profile.
     * This makes them unavailable for calls to [getPreloadedGroups].
     * This should be called by the permissions plugin when the last player using the profile logs out of the server.
     *
     * @param profile The profile
     */
    fun unloadGroups(profile: RPKProfile)

    /**
     * Gets group priority for the given group on the given profile
     *
     * @param profile The profile
     * @param group The group
     * @return The priority of the group on the profile, or null if the group is absent
     */
    fun getGroupPriority(profile: RPKProfile, group: RPKGroup): CompletableFuture<Int?>

    /**
     * Sets group priority for the given group on the given profile
     *
     * @param profile The profile
     * @param group The group
     * @param priority The priority to set
     */
    fun setGroupPriority(profile: RPKProfile, group: RPKGroup, priority: Int): CompletableFuture<Void>

    /**
     * Gets groups assigned to a character.
     *
     * @oaram character: The character
     * @return A list of groups assigned to the character
     */
    fun getGroups(character: RPKCharacter): CompletableFuture<List<RPKGroup>>

    /**
     * Gets preloaded groups assigned to a character.
     * If the groups have not been loaded, returns null.
     * The groups must first be loaded with [loadGroups]. This should happen when an online player is playing this character.
     *
     * @param profile The profile
     * @return A list of preloaded groups assigned to the profile
     */
    fun getPreloadedGroups(character: RPKCharacter): List<RPKGroup>?

    /**
     * Loads the groups assigned to a character.
     * This makes them available for calls to [getPreloadedGroups].
     * This should be called by the permissions plugin when a player logs in with a character or when they switch to it.
     *
     * @param character The character
     */
    fun loadGroups(character: RPKCharacter): CompletableFuture<List<RPKGroup>>

    /**
     * Unloads the groups assigned to a character.
     * This makes them unavailable for calls to [getPreloadedGroups].
     * This should be called when a player switches to a different character, or logs off.
     *
     * @param character The character
     */
    fun unloadGroups(character: RPKCharacter)

    /**
     * Gets group priority for the given group on the given character
     *
     * @param character The character
     * @param group The group
     * @return The priority of the group on the character, or null if the group is absent
     */
    fun getGroupPriority(character: RPKCharacter, group: RPKGroup): CompletableFuture<Int?>

    /**
     * Sets group priority for the given group on the given character
     *
     * @param character The character
     * @param group The group
     * @param priority The priority to set
     */
    fun setGroupPriority(character: RPKCharacter, group: RPKGroup, priority: Int): CompletableFuture<Void>

}