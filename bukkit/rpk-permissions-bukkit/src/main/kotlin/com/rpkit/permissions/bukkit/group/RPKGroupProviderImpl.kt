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

import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.database.table.PlayerGroupTable
import com.rpkit.players.bukkit.player.RPKPlayer
import org.bukkit.permissions.PermissionAttachment

/**
 * Group provider implementation.
 */
class RPKGroupProviderImpl(private val plugin: RPKPermissionsBukkit): RPKGroupProvider {

    override val groups: List<RPKGroup> = plugin.config.getList("groups") as List<RPKGroupImpl>
    val defaultGroup = plugin.config.get("default-group") as RPKGroup
    val permissionsAttachments = mutableMapOf<Int, PermissionAttachment>()

    override fun getGroup(name: String): RPKGroup? {
        return groups.filter { group -> group.name == name }.firstOrNull()
    }

    override fun addGroup(player: RPKPlayer, group: RPKGroup) {
        if (!getGroups(player).contains(group)) {
            plugin.core.database.getTable(PlayerGroupTable::class).insert(
                    PlayerGroup(player = player, group = group)
            )
            assignPermissions(player)
        }
    }

    private fun assignGroupPermissions(player: RPKPlayer, group: RPKGroup) {
        val permissionsAttachment = permissionsAttachments[player.id]
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

    override fun removeGroup(player: RPKPlayer, group: RPKGroup) {
        val playerGroupTable = plugin.core.database.getTable(PlayerGroupTable::class)
        val playerGroup = playerGroupTable.get(player)
                .filter { playerGroup -> playerGroup.group == group }
                .firstOrNull()
        if (playerGroup != null) {
            playerGroupTable.delete(playerGroup)
            assignPermissions(player)
        }
    }

    override fun assignPermissions(player: RPKPlayer) {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val onlineBukkitPlayer = bukkitPlayer.player
            if (onlineBukkitPlayer != null) {
                if (!permissionsAttachments.containsKey(player.id)) {
                    permissionsAttachments[player.id] = onlineBukkitPlayer.addAttachment(plugin)
                } else {
                    onlineBukkitPlayer.removeAttachment(permissionsAttachments[player.id])
                    permissionsAttachments[player.id] = onlineBukkitPlayer.addAttachment(plugin)
                }
                val groups = getGroups(player)
                if (groups.isEmpty()) {
                    assignGroupPermissions(player, defaultGroup)
                } else {
                    val assignedGroups = mutableListOf<RPKGroup>()
                    for (group in groups) {
                        if (!assignedGroups.contains(group)) {
                            assignGroupPermissions(player, group)
                            assignedGroups.add(group)
                        }
                        for (inheritedGroup in group.inheritance) {
                            if (!assignedGroups.contains(inheritedGroup)) {
                                assignGroupPermissions(player, inheritedGroup)
                                assignedGroups.add(group)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun unassignPermissions(player: RPKPlayer) {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val onlineBukkitPlayer = bukkitPlayer.player
            if (onlineBukkitPlayer != null) {
                onlineBukkitPlayer.removeAttachment(permissionsAttachments[player.id])
                permissionsAttachments.remove(player.id)
            }
        }
    }

    override fun getGroups(player: RPKPlayer): List<RPKGroup> {
        return plugin.core.database.getTable(PlayerGroupTable::class).get(player).map(PlayerGroup::group)
    }

    override fun hasPermission(group: RPKGroup, node: String): Boolean {
        var hasPermission = plugin.server.pluginManager.getPermission(node).default.getValue(false)
        for (inheritedGroup in group.inheritance) {
            if (node in group.allow) {
                hasPermission = true
            }
            if (node in group.deny) {
                hasPermission = false
            }
        }
        if (node in group.allow) {
            hasPermission = true
        }
        if (node in group.deny) {
            hasPermission = false
        }
        return hasPermission
    }

    override fun hasPermission(player: RPKPlayer, node: String): Boolean {
        var hasPermission = plugin.server.pluginManager.getPermission(node).default.getValue(false)
        for (group in getGroups(player)) {
            hasPermission = hasPermission(group, node)
        }
        return hasPermission
    }

}