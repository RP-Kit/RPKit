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

package com.seventh_root.elysium.permissions.bukkit.group

import com.seventh_root.elysium.permissions.bukkit.ElysiumPermissionsBukkit
import com.seventh_root.elysium.permissions.bukkit.database.table.PlayerGroupTable
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import org.bukkit.permissions.PermissionAttachment


class ElysiumGroupProviderImpl(private val plugin: ElysiumPermissionsBukkit): ElysiumGroupProvider {

    override val groups: List<ElysiumGroup> = plugin.config.getList("groups") as List<ElysiumGroupImpl>
    private val permissionsAttachments = mutableMapOf<Int, PermissionAttachment>()

    override fun getGroup(name: String): ElysiumGroup? {
        return groups.filter { group -> group.name == name }.firstOrNull()
    }

    override fun addGroup(player: ElysiumPlayer, group: ElysiumGroup) {
        if (!getGroups(player).contains(group)) {
            plugin.core.database.getTable(PlayerGroupTable::class).insert(
                    PlayerGroup(player = player, group = group)
            )
            assignPermissions(player)
        }
    }

    private fun assignGroupPermissions(player: ElysiumPlayer, group: ElysiumGroup) {
        for (inheritedGroup in group.inheritance) {
            assignGroupPermissions(player, inheritedGroup)
        }
        for (node in group.allow) {
            if (permissionsAttachments[player.id]?.permissions?.containsKey(node)?:false) {
                permissionsAttachments[player.id]?.unsetPermission(node)
            }
            permissionsAttachments[player.id]?.setPermission(node, true)
        }
        for (node in group.deny) {
            if (permissionsAttachments[player.id]?.permissions?.containsKey(node)?:false) {
                permissionsAttachments[player.id]?.unsetPermission(node)
            }
            permissionsAttachments[player.id]?.setPermission(node, false)
        }
    }

    override fun removeGroup(player: ElysiumPlayer, group: ElysiumGroup) {
        val playerGroupTable = plugin.core.database.getTable(PlayerGroupTable::class)
        val playerGroup = playerGroupTable.get(player)
                .filter { playerGroup -> playerGroup.group == group }
                .firstOrNull()
        if (playerGroup != null) {
            playerGroupTable.delete(playerGroup)
            assignPermissions(player)
        }
    }

    override fun assignPermissions(player: ElysiumPlayer) {
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
                if (getGroups(player).isEmpty()) {
                    val defaultGroup = plugin.config.get("default-group") as? ElysiumGroup
                    if (defaultGroup != null) {
                        addGroup(player, defaultGroup)
                    }
                }
                for (existingGroup in getGroups(player)) {
                    assignGroupPermissions(player, existingGroup)
                }
            }
        }
    }

    override fun unassignPermissions(player: ElysiumPlayer) {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val onlineBukkitPlayer = bukkitPlayer.player
            if (onlineBukkitPlayer != null) {
                onlineBukkitPlayer.removeAttachment(permissionsAttachments[player.id])
                permissionsAttachments.remove(player.id)
            }
        }
    }

    override fun getGroups(player: ElysiumPlayer): List<ElysiumGroup> {
        return plugin.core.database.getTable(PlayerGroupTable::class).get(player).map(PlayerGroup::group)
    }

    override fun hasPermission(group: ElysiumGroup, node: String): Boolean {
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

    override fun hasPermission(player: ElysiumPlayer, node: String): Boolean {
        var hasPermission = plugin.server.pluginManager.getPermission(node).default.getValue(false)
        for (group in getGroups(player)) {
            hasPermission = hasPermission(group, node)
        }
        return hasPermission
    }

}