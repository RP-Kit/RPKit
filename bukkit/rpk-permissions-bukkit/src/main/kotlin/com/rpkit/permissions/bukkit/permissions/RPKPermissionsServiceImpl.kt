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

package com.rpkit.permissions.bukkit.permissions

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroup
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.permissions.bukkit.group.groups
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.permissions.PermissionAttachment
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

class RPKPermissionsServiceImpl(override val plugin: RPKPermissionsBukkit) : RPKPermissionsService {

    val defaultGroup = plugin.config.get("default-group") as RPKGroup
    private val permissionsAttachments = ConcurrentHashMap<Int, PermissionAttachment>()

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
        val groupService = Services[RPKGroupService::class.java] ?: return hasPermission
        val groups = groupService.getGroups(profile)
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
        for (group in character.groups) {
            hasPermission = hasPermission(group, node, hasPermission)
        }
        return hasPermission
    }

    override fun assignPermissions(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Void> {
        val minecraftProfileId = minecraftProfile.id
                ?: throw IllegalStateException("Minecraft profile has not yet been inserted into the database.")
        val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID)
            ?: return CompletableFuture.completedFuture(null)
        val permissionsAttachment = permissionsAttachments[minecraftProfileId.value]
        if (permissionsAttachment == null) {
            permissionsAttachments[minecraftProfileId.value] = bukkitPlayer.addAttachment(plugin)
        } else {
            bukkitPlayer.removeAttachment(permissionsAttachment)
            permissionsAttachments[minecraftProfileId.value] = bukkitPlayer.addAttachment(plugin)
        }
        val groups = mutableListOf<RPKGroup>()
        val profile = minecraftProfile.profile
        if (profile is RPKProfile) {
            groups.addAll(profile.groups)
        }
        val characterService = Services[RPKCharacterService::class.java] ?: return CompletableFuture.completedFuture(null)
        return characterService.getActiveCharacter(minecraftProfile).thenAccept { character ->
            if (character != null) {
                groups.addAll(character.groups)
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

    private fun assignGroupPermissions(minecraftProfile: RPKMinecraftProfile, group: RPKGroup, assignedGroups: MutableList<RPKGroup>) {
        if (assignedGroups.contains(group)) return
        assignedGroups.add(group)
        for (inheritedGroup in group.inheritance) {
            assignGroupPermissions(minecraftProfile, inheritedGroup, assignedGroups)
        }
        val minecraftProfileId = minecraftProfile.id ?: return
        val permissionsAttachment = permissionsAttachments[minecraftProfileId.value]
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

    override fun unassignPermissions(minecraftProfile: RPKMinecraftProfile) {
        val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID)
        if (bukkitPlayer != null) {
            val minecraftProfileId = minecraftProfile.id ?: return
            val permissionsAttachment = permissionsAttachments[minecraftProfileId.value]
            if (permissionsAttachment != null) {
                bukkitPlayer.removeAttachment(permissionsAttachment)
                permissionsAttachments.remove(minecraftProfileId.value)
            }
        }
    }


}