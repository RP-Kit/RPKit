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
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

fun RPKProfile.addGroup(group: RPKGroup): CompletableFuture<Void> {
    return Services[RPKGroupService::class.java]?.addGroup(this, group) ?: CompletableFuture.completedFuture(null)
}

fun RPKProfile.addGroup(group: RPKGroup, priority: Int): CompletableFuture<Void> {
    return Services[RPKGroupService::class.java]?.addGroup(this, group, priority) ?: CompletableFuture.completedFuture(null)
}

fun RPKProfile.removeGroup(group: RPKGroup): CompletableFuture<Void> {
    return Services[RPKGroupService::class.java]?.removeGroup(this, group) ?: CompletableFuture.completedFuture(null)
}

fun RPKProfile.hasPermission(node: String): CompletableFuture<Boolean> {
    return Services[RPKPermissionsService::class.java]?.hasPermission(this, node)
            ?: CompletableFuture.completedFuture(
                Bukkit.getPluginManager().getPermission(node)?.default?.getValue(false)
                    ?: false
            )
}

val RPKProfile.groups: CompletableFuture<List<RPKGroup>>
    get() = Services[RPKGroupService::class.java]?.getGroups(this) ?: CompletableFuture.completedFuture(emptyList())

val RPKProfile.preloadedGroups: List<RPKGroup>?
    get() = Services[RPKGroupService::class.java]?.getPreloadedGroups(this)

fun RPKCharacter.addGroup(group: RPKGroup): CompletableFuture<Void> {
    return Services[RPKGroupService::class.java]?.addGroup(this, group) ?: CompletableFuture.completedFuture(null)
}

fun RPKCharacter.addGroup(group: RPKGroup, priority: Int): CompletableFuture<Void> {
    return Services[RPKGroupService::class.java]?.addGroup(this, group, priority) ?: CompletableFuture.completedFuture(null)
}

fun RPKCharacter.removeGroup(group: RPKGroup): CompletableFuture<Void> {
    return Services[RPKGroupService::class.java]?.removeGroup(this, group) ?: CompletableFuture.completedFuture(null)
}

fun RPKCharacter.hasPermission(node: String): CompletableFuture<Boolean> {
    return Services[RPKPermissionsService::class.java]?.hasPermission(this, node)
            ?: CompletableFuture.completedFuture(
                Bukkit.getPluginManager().getPermission(node)?.default?.getValue(false)
                    ?: false
            )
}

val RPKCharacter.groups: CompletableFuture<List<RPKGroup>>
    get() = Services[RPKGroupService::class.java]?.getGroups(this) ?: CompletableFuture.completedFuture(emptyList())

val RPKCharacter.preloadedGroups: List<RPKGroup>?
    get() = Services[RPKGroupService::class.java]?.getPreloadedGroups(this)

fun RPKMinecraftProfile.assignPermissions() {
    Services[RPKPermissionsService::class.java]?.assignPermissions(this)
}

fun RPKMinecraftProfile.unassignPermissions(bukkitPlayer: Player) {
    Services[RPKPermissionsService::class.java]?.unassignPermissions(this, bukkitPlayer)
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