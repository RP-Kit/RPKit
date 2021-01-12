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

package com.rpkit.permissions.bukkit.vault

import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsLibBukkit
import com.rpkit.permissions.bukkit.group.RPKGroup
import com.rpkit.permissions.bukkit.group.RPKGroupName
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.permissions.bukkit.group.addGroup
import com.rpkit.permissions.bukkit.group.groups
import com.rpkit.permissions.bukkit.group.hasPermission
import com.rpkit.permissions.bukkit.group.removeGroup
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftUsername
import net.milkbowl.vault.permission.Permission

class RPKPermissionsVaultPermissions(private val plugin: RPKPermissionsLibBukkit) : Permission() {
    
    override fun getName(): String {
        return "rpk-permissions"
    }

    override fun isEnabled(): Boolean {
        return true
    }

    override fun hasSuperPermsCompat(): Boolean {
        return true
    }

    override fun playerHas(worldName: String, playerName: String, permission: String): Boolean {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(playerName)) ?: return false
        val profile = minecraftProfile.profile as? RPKProfile ?: return false
        return profile.hasPermission(permission)
    }

    override fun playerAdd(worldName: String, playerName: String, permission: String): Boolean {
        return false
    }

    override fun playerRemove(worldName: String, playerName: String, permission: String): Boolean {
        return false
    }

    override fun groupHas(worldName: String, groupName: String, permission: String): Boolean {
        val groupService = Services[RPKGroupService::class.java] ?: return false
        val group = groupService.getGroup(RPKGroupName(groupName)) ?: return false
        return group.hasPermission(permission)
    }

    override fun groupAdd(worldName: String, groupName: String, permission: String): Boolean {
        return false
    }

    override fun groupRemove(worldName: String, groupName: String, permission: String): Boolean {
        return false
    }

    override fun playerInGroup(worldName: String, playerName: String, groupName: String): Boolean {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
        val groupService = Services[RPKGroupService::class.java] ?: return false
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(playerName)) ?: return false
        val profile = minecraftProfile.profile as? RPKProfile ?: return false
        val group = groupService.getGroup(RPKGroupName(groupName)) ?: return false
        return profile.groups.map(RPKGroup::name).contains(group.name)
    }

    override fun playerAddGroup(worldName: String, playerName: String, groupName: String): Boolean {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
        val groupService = Services[RPKGroupService::class.java] ?: return false
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(playerName)) ?: return false
        val profile = minecraftProfile.profile as? RPKProfile ?: return false
        val group = groupService.getGroup(RPKGroupName(groupName)) ?: return false
        profile.addGroup(group)
        return true
    }

    override fun playerRemoveGroup(worldName: String, playerName: String, groupName: String): Boolean {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
        val groupService = Services[RPKGroupService::class.java] ?: return false
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(playerName)) ?: return false
        val profile = minecraftProfile.profile as? RPKProfile ?: return false
        val group = groupService.getGroup(RPKGroupName(groupName)) ?: return false
        profile.removeGroup(group)
        return true
    }

    override fun getPlayerGroups(worldName: String, playerName: String): Array<String> {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return emptyArray()
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(playerName)) ?: return emptyArray()
        val profile = minecraftProfile.profile as? RPKProfile ?: return emptyArray()
        return profile.groups.map { group -> group.name.value }.toTypedArray()
    }

    override fun getPrimaryGroup(worldName: String, playerName: String): String? {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return null
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(playerName)) ?: return null
        val profile = minecraftProfile.profile as? RPKProfile ?: return null
        return profile.groups[0].name.value
    }

    override fun getGroups(): Array<String> {
        val groupService = Services[RPKGroupService::class.java] ?: return emptyArray()
        return groupService.groups.map { group -> group.name.value }.toTypedArray()
    }

    override fun hasGroupSupport(): Boolean {
        return true
    }
    
}