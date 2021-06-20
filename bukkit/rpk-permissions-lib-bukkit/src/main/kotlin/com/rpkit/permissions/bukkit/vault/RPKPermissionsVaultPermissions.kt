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
import com.rpkit.permissions.bukkit.group.*
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
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (playerHas)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(playerName)).join() ?: return false
        val profile = minecraftProfile.profile as? RPKProfile ?: return false
        return profile.hasPermission(permission).join()
    }

    override fun playerAdd(worldName: String, playerName: String, permission: String): Boolean {
        return false
    }

    override fun playerRemove(worldName: String, playerName: String, permission: String): Boolean {
        return false
    }

    override fun groupHas(worldName: String, groupName: String, permission: String): Boolean {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (groupHas)")
        }
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
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (playerInGroup)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
        val groupService = Services[RPKGroupService::class.java] ?: return false
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(playerName)).join() ?: return false
        val profile = minecraftProfile.profile as? RPKProfile ?: return false
        val group = groupService.getGroup(RPKGroupName(groupName)) ?: return false
        return profile.groups.join().map(RPKGroup::name).contains(group.name)
    }

    override fun playerAddGroup(worldName: String, playerName: String, groupName: String): Boolean {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (playerAddGroup)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
        val groupService = Services[RPKGroupService::class.java] ?: return false
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(playerName)).join() ?: return false
        val profile = minecraftProfile.profile as? RPKProfile ?: return false
        val group = groupService.getGroup(RPKGroupName(groupName)) ?: return false
        profile.addGroup(group)
        return true
    }

    override fun playerRemoveGroup(worldName: String, playerName: String, groupName: String): Boolean {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (playerRemoveGroup)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return false
        val groupService = Services[RPKGroupService::class.java] ?: return false
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(playerName)).join() ?: return false
        val profile = minecraftProfile.profile as? RPKProfile ?: return false
        val group = groupService.getGroup(RPKGroupName(groupName)) ?: return false
        profile.removeGroup(group)
        return true
    }

    override fun getPlayerGroups(worldName: String, playerName: String): Array<String> {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (getPlayerGroups)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return emptyArray()
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(playerName)).join() ?: return emptyArray()
        val profile = minecraftProfile.profile as? RPKProfile ?: return emptyArray()
        return profile.groups.join().map { group -> group.name.value }.toTypedArray()
    }

    override fun getPrimaryGroup(worldName: String, playerName: String): String? {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (getPrimaryGroup)")
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return null
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(playerName)).join() ?: return null
        val profile = minecraftProfile.profile as? RPKProfile ?: return null
        return profile.groups.join()[0].name.value
    }

    override fun getGroups(): Array<String> {
        if (plugin.server.isPrimaryThread) {
            plugin.logger.warning("Vault is being used from the main thread! This may cause lag! (getGroups)")
        }
        val groupService = Services[RPKGroupService::class.java] ?: return emptyArray()
        return groupService.groups.map { group -> group.name.value }.toTypedArray()
    }

    override fun hasGroupSupport(): Boolean {
        return true
    }
    
}