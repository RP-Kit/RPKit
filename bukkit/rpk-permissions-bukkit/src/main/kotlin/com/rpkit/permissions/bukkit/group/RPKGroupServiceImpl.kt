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
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.database.table.RPKCharacterGroupTable
import com.rpkit.permissions.bukkit.database.table.RPKProfileGroupTable
import com.rpkit.permissions.bukkit.event.group.RPKBukkitGroupAssignCharacterEvent
import com.rpkit.permissions.bukkit.event.group.RPKBukkitGroupAssignProfileEvent
import com.rpkit.permissions.bukkit.event.group.RPKBukkitGroupUnassignCharacterEvent
import com.rpkit.permissions.bukkit.event.group.RPKBukkitGroupUnassignProfileEvent
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault

/**
 * Group service implementation.
 */
class RPKGroupServiceImpl(override val plugin: RPKPermissionsBukkit) : RPKGroupService {

    override val groups: List<RPKGroup> = plugin.config.getList("groups") as List<RPKGroupImpl>

    init {
        groups.forEach { group ->
            plugin.server.pluginManager.addPermission(Permission(
                    "rpkit.permissions.command.group.add.${group.name}",
                    "Allows adding the ${group.name} group to players",
                    PermissionDefault.OP
            ))
            plugin.server.pluginManager.addPermission(Permission(
                    "rpkit.permissions.command.group.remove.${group.name}",
                    "Allows removing the ${group.name} group from players",
                    PermissionDefault.OP
            ))
        }
    }

    override fun getGroup(name: String): RPKGroup? {
        return groups.firstOrNull { group -> group.name == name }
    }

    override fun addGroup(profile: RPKProfile, group: RPKGroup, priority: Int) {
        if (!profile.groups.contains(group)) {
            val event = RPKBukkitGroupAssignProfileEvent(group, profile, priority)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return
            plugin.database.getTable(RPKProfileGroupTable::class.java).insert(
                    RPKProfileGroup(
                            event.profile,
                            event.group,
                            event.priority
                    )
            )
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
            minecraftProfileService.getMinecraftProfiles(event.profile).forEach { minecraftProfile ->
                minecraftProfile.assignPermissions()
            }
        }
    }

    override fun addGroup(profile: RPKProfile, group: RPKGroup) {
        addGroup(
                profile,
                group,
                plugin.database.getTable(RPKProfileGroupTable::class.java).get(profile)
                        .minByOrNull(RPKProfileGroup::priority)?.priority?.minus(1) ?: 0
        )
    }

    override fun addGroup(character: RPKCharacter, group: RPKGroup, priority: Int) {
        if (character.groups.contains(group)) return
        val event = RPKBukkitGroupAssignCharacterEvent(group, character, priority)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKCharacterGroupTable::class.java).insert(
                RPKCharacterGroup(
                        event.character,
                        event.group,
                        event.priority
                )
        )
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val minecraftProfile = event.character.minecraftProfile ?: return
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) return
        minecraftProfileService.getMinecraftProfiles(profile).forEach { profileMinecraftProfile ->
            profileMinecraftProfile.assignPermissions()
        }
    }

    override fun addGroup(character: RPKCharacter, group: RPKGroup) {
        addGroup(
                character,
                group,
                plugin.database.getTable(RPKCharacterGroupTable::class.java).get(character)
                        .minByOrNull(RPKCharacterGroup::priority)?.priority?.minus(1) ?: 0
        )
    }



    override fun removeGroup(profile: RPKProfile, group: RPKGroup) {
        val event = RPKBukkitGroupUnassignProfileEvent(group, profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val profileGroupTable = plugin.database.getTable(RPKProfileGroupTable::class.java)
        val profileGroup = profileGroupTable.get(event.profile).firstOrNull { profileGroup -> profileGroup.group == event.group }
                ?: return
        profileGroupTable.delete(profileGroup)
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        minecraftProfileService.getMinecraftProfiles(event.profile).forEach { minecraftProfile ->
            minecraftProfile.assignPermissions()
        }
    }

    override fun removeGroup(character: RPKCharacter, group: RPKGroup) {
        val event = RPKBukkitGroupUnassignCharacterEvent(group, character)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val characterGroupTable = plugin.database.getTable(RPKCharacterGroupTable::class.java)
        val characterGroup = characterGroupTable.get(event.character).firstOrNull { characterGroup -> characterGroup.group == event.group }
                ?: return
        characterGroupTable.delete(characterGroup)
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val minecraftProfile = event.character.minecraftProfile ?: return
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) return
        minecraftProfileService.getMinecraftProfiles(profile).forEach { profileMinecraftProfile ->
            profileMinecraftProfile.assignPermissions()
        }
    }

    override fun getGroups(profile: RPKProfile): List<RPKGroup> {
        return plugin.database.getTable(RPKProfileGroupTable::class.java).get(profile).map(RPKProfileGroup::group)
    }

    override fun getGroups(character: RPKCharacter): List<RPKGroup> {
        return plugin.database.getTable(RPKCharacterGroupTable::class.java).get(character).map(RPKCharacterGroup::group)
    }

}