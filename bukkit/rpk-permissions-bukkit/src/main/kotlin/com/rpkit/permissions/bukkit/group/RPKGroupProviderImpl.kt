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

package com.rpkit.permissions.bukkit.group

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.database.table.RPKCharacterGroupTable
import com.rpkit.permissions.bukkit.database.table.RPKProfileGroupTable
import com.rpkit.permissions.bukkit.event.group.RPKBukkitGroupAssignCharacterEvent
import com.rpkit.permissions.bukkit.event.group.RPKBukkitGroupAssignProfileEvent
import com.rpkit.permissions.bukkit.event.group.RPKBukkitGroupUnassignCharacterEvent
import com.rpkit.permissions.bukkit.event.group.RPKBukkitGroupUnassignProfileEvent
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.Bukkit
import org.bukkit.permissions.PermissionAttachment

/**
 * Group service implementation.
 */
class RPKGroupServiceImpl(override val plugin: RPKPermissionsBukkit) : RPKGroupService {

    override val groups: List<RPKGroup> = plugin.config.getList("groups") as List<RPKGroupImpl>
    val defaultGroup = plugin.config.get("default-group") as RPKGroup
    val permissionsAttachments = mutableMapOf<Int, PermissionAttachment>()

    override fun getGroup(name: String): RPKGroup? {
        return groups.firstOrNull { group -> group.name == name }
    }

    override fun addGroup(profile: RPKProfile, group: RPKGroup, priority: Int) {
        if (!getGroups(profile).contains(group)) {
            val event = RPKBukkitGroupAssignProfileEvent(group, profile, priority)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            plugin.database.getTable(RPKProfileGroupTable::class).insert(
                    RPKProfileGroup(
                            event.profile,
                            event.group,
                            event.priority
                    )
            )
            val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return
            minecraftProfileService.getMinecraftProfiles(event.profile).forEach { minecraftProfile ->
                assignPermissions(minecraftProfile)
            }
        }
    }

    override fun addGroup(profile: RPKProfile, group: RPKGroup) {
        addGroup(
                profile,
                group,
                plugin.database.getTable(RPKProfileGroupTable::class).get(profile)
                        .minBy(RPKProfileGroup::priority)?.priority?.minus(1) ?: 0
        )
    }

    override fun addGroup(character: RPKCharacter, group: RPKGroup, priority: Int) {
        if (getGroups(character).contains(group)) return
        val event = RPKBukkitGroupAssignCharacterEvent(group, character, priority)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKCharacterGroupTable::class).insert(
                RPKCharacterGroup(
                        event.character,
                        event.group,
                        event.priority
                )
        )
        val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return
        val minecraftProfile = event.character.minecraftProfile ?: return
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) return
        minecraftProfileService.getMinecraftProfiles(profile).forEach { profileMinecraftProfile ->
            assignPermissions(profileMinecraftProfile)
        }
    }

    override fun addGroup(character: RPKCharacter, group: RPKGroup) {
        addGroup(
                character,
                group,
                plugin.database.getTable(RPKCharacterGroupTable::class).get(character)
                        .minBy(RPKCharacterGroup::priority)?.priority?.minus(1) ?: 0
        )
    }

    private fun assignGroupPermissions(minecraftProfile: RPKMinecraftProfile, group: RPKGroup, assignedGroups: MutableList<RPKGroup>) {
        if (assignedGroups.contains(group)) return
        assignedGroups.add(group)
        for (inheritedGroup in group.inheritance) {
            assignGroupPermissions(minecraftProfile, inheritedGroup, assignedGroups)
        }
        val permissionsAttachment = permissionsAttachments[minecraftProfile.id]
        if (permissionsAttachment != null) {
            for (node in group.allow) {
                if (permissionsAttachment.permissions.containsKey(node)) {
                    permissionsAttachment.unsetPermission(node)
                }
                permissionsAttachment.setPermission(node, true)
            }
            for (node in group.deny) {
                if (permissionsAttachment.permissions.containsKey(node)) {
                    permissionsAttachment.unsetPermission(node)
                }
                permissionsAttachment.setPermission(node, false)
            }
        }
    }

    override fun removeGroup(profile: RPKProfile, group: RPKGroup) {
        val event = RPKBukkitGroupUnassignProfileEvent(group, profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val profileGroupTable = plugin.database.getTable(RPKProfileGroupTable::class)
        val profileGroup = profileGroupTable.get(event.profile).firstOrNull { profileGroup -> profileGroup.group == event.group }
                ?: return
        profileGroupTable.delete(profileGroup)
        val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return
        minecraftProfileService.getMinecraftProfiles(event.profile).forEach { minecraftProfile ->
            assignPermissions(minecraftProfile)
        }
    }

    override fun removeGroup(character: RPKCharacter, group: RPKGroup) {
        val event = RPKBukkitGroupUnassignCharacterEvent(group, character)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val characterGroupTable = plugin.database.getTable(RPKCharacterGroupTable::class)
        val characterGroup = characterGroupTable.get(event.character).firstOrNull { characterGroup -> characterGroup.group == event.group }
                ?: return
        characterGroupTable.delete(characterGroup)
        val minecraftProfileService = Services[RPKMinecraftProfileService::class] ?: return
        val minecraftProfile = event.character.minecraftProfile ?: return
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) return
        minecraftProfileService.getMinecraftProfiles(profile).forEach { profileMinecraftProfile ->
            assignPermissions(profileMinecraftProfile)
        }
    }

    override fun assignPermissions(minecraftProfile: RPKMinecraftProfile) {
        val minecraftProfileId = minecraftProfile.id
                ?: throw IllegalStateException("Minecraft profile has not yet been inserted into the database.")
        val bukkitPlayer = Bukkit.getOfflinePlayer(minecraftProfile.minecraftUUID)
        val onlineBukkitPlayer = bukkitPlayer.player
        if (onlineBukkitPlayer != null) {
            val permissionsAttachment = permissionsAttachments[minecraftProfileId]
            if (permissionsAttachment == null) {
                permissionsAttachments[minecraftProfileId] = onlineBukkitPlayer.addAttachment(plugin)
            } else {
                onlineBukkitPlayer.removeAttachment(permissionsAttachment)
                permissionsAttachments[minecraftProfileId] = onlineBukkitPlayer.addAttachment(plugin)
            }
            val groups = mutableListOf<RPKGroup>()
            val profile = minecraftProfile.profile
            if (profile is RPKProfile) {
                groups.addAll(getGroups(profile))
            }
            val characterService = Services[RPKCharacterService::class] ?: return
            val character = characterService.getActiveCharacter(minecraftProfile)
            if (character != null) {
                groups.addAll(getGroups(character))
            }
            if (groups.isEmpty()) {
                assignGroupPermissions(minecraftProfile, defaultGroup, mutableListOf())
            } else {
                val assignedGroups = mutableListOf<RPKGroup>()
                for (group in groups) {
                    assignGroupPermissions(minecraftProfile, group, assignedGroups)
                }
            }
        }
    }

    override fun unassignPermissions(minecraftProfile: RPKMinecraftProfile) {
        val bukkitPlayer = plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID)
        val onlineBukkitPlayer = bukkitPlayer.player
        if (onlineBukkitPlayer != null) {
            val permissionsAttachment = permissionsAttachments[minecraftProfile.id]
            if (permissionsAttachment != null) {
                onlineBukkitPlayer.removeAttachment(permissionsAttachment)
                permissionsAttachments.remove(minecraftProfile.id)
            }
        }
    }

    override fun getGroups(profile: RPKProfile): List<RPKGroup> {
        return plugin.database.getTable(RPKProfileGroupTable::class).get(profile).map(RPKProfileGroup::group)
    }

    override fun getGroups(character: RPKCharacter): List<RPKGroup> {
        return plugin.database.getTable(RPKCharacterGroupTable::class).get(character).map(RPKCharacterGroup::group)
    }

    override fun hasPermission(group: RPKGroup, node: String): Boolean {
        return hasPermission(group, node, plugin.server.pluginManager.getPermission(node)?.default?.getValue(false)
                ?: false)
    }

    override fun hasPermission(group: RPKGroup, node: String, default: Boolean): Boolean {
        var hasPermission = default
        for (inheritedGroup in group.inheritance) {
            hasPermission = hasPermission(inheritedGroup, node)
        }
        if (node in group.allow) {
            hasPermission = true
        }
        if (node in group.deny) {
            hasPermission = false
        }
        return hasPermission
    }

    override fun hasPermission(profile: RPKProfile, node: String): Boolean {
        var hasPermission = plugin.server.pluginManager.getPermission(node)?.default?.getValue(false) ?: false
        val groups = getGroups(profile)
        if (groups.isEmpty()) {
            hasPermission = hasPermission(defaultGroup, node, hasPermission)
        } else {
            for (group in groups) {
                hasPermission = hasPermission(group, node, hasPermission)
            }
        }
        return hasPermission
    }

    override fun hasPermission(character: RPKCharacter, node: String): Boolean {
        var hasPermission = plugin.server.pluginManager.getPermission(node)?.default?.getValue(false) ?: false
        val minecraftProfile = character.minecraftProfile
        if (minecraftProfile != null) {
            val profile = minecraftProfile.profile
            if (profile is RPKProfile) {
                hasPermission = hasPermission(profile, node)
            }
        }
        val groups = getGroups(character)
        for (group in groups) {
            hasPermission = hasPermission(group, node, hasPermission)
        }
        return hasPermission
    }

}