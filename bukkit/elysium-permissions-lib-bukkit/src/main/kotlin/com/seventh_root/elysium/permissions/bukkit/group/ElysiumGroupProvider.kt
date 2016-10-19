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

import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer


interface ElysiumGroupProvider: ServiceProvider {

    val groups: List<ElysiumGroup>
    fun getGroup(name: String): ElysiumGroup?
    fun addGroup(player: ElysiumPlayer, group: ElysiumGroup)
    fun removeGroup(player: ElysiumPlayer, group: ElysiumGroup)
    fun getGroups(player: ElysiumPlayer): List<ElysiumGroup>
    fun hasPermission(group: ElysiumGroup, node: String): Boolean
    fun hasPermission(player: ElysiumPlayer, node: String): Boolean
    fun assignPermissions(player: ElysiumPlayer)
    fun unassignPermissions(player: ElysiumPlayer)

}