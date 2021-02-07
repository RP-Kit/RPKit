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
import com.rpkit.permissions.bukkit.permissions.RPKPermissionsService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.Bukkit

fun RPKProfile.addGroup(group: RPKGroup) {
    Services[RPKGroupService::class.java]?.addGroup(this, group)
}

fun RPKProfile.addGroup(group: RPKGroup, priority: Int) {
    Services[RPKGroupService::class.java]?.addGroup(this, group, priority)
}

fun RPKProfile.removeGroup(group: RPKGroup) {
    Services[RPKGroupService::class.java]?.removeGroup(this, group)
}

fun RPKProfile.hasPermission(node: String): Boolean {
    return Services[RPKPermissionsService::class.java]?.hasPermission(this, node)
            ?: Bukkit.getPluginManager().getPermission(node)?.default?.getValue(false)
            ?: false
}

val RPKProfile.groups
    get() = Services[RPKGroupService::class.java]?.getGroups(this) ?: emptyList()

fun RPKCharacter.addGroup(group: RPKGroup) {
    Services[RPKGroupService::class.java]?.addGroup(this, group)
}

fun RPKCharacter.addGroup(group: RPKGroup, priority: Int) {
    Services[RPKGroupService::class.java]?.addGroup(this, group, priority)
}

fun RPKCharacter.removeGroup(group: RPKGroup) {
    Services[RPKGroupService::class.java]?.removeGroup(this, group)
}

fun RPKCharacter.hasPermission(node: String): Boolean {
    return Services[RPKPermissionsService::class.java]?.hasPermission(this, node)
            ?: Bukkit.getPluginManager().getPermission(node)?.default?.getValue(false)
            ?: false
}

val RPKCharacter.groups
    get() = Services[RPKGroupService::class.java]?.getGroups(this) ?: emptyList()

fun RPKMinecraftProfile.assignPermissions() {
    Services[RPKPermissionsService::class.java]?.assignPermissions(this)
}

fun RPKMinecraftProfile.unassignPermissions() {
    Services[RPKPermissionsService::class.java]?.unassignPermissions(this)
}

fun RPKGroup.hasPermission(node: String, default: Boolean): Boolean {
    return Services[RPKPermissionsService::class.java]?.hasPermission(this, node, default)
            ?: Bukkit.getPluginManager().getPermission(node)?.default?.getValue(false)
            ?: false
}

fun RPKGroup.hasPermission(node: String): Boolean {
    return Services[RPKPermissionsService::class.java]?.hasPermission(this, node)
            ?: Bukkit.getPluginManager().getPermission(node)?.default?.getValue(false)
            ?: false
}