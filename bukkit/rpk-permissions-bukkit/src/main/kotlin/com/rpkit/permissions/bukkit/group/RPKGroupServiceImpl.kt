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
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

/**
 * Group service implementation.
 */
class RPKGroupServiceImpl(override val plugin: RPKPermissionsBukkit) : RPKGroupService {

    override val groups: List<RPKGroup> = plugin.config.getList("groups") as List<RPKGroupImpl>
    private val profileGroups = ConcurrentHashMap<Int, List<RPKProfileGroup>>()
    private val characterGroups = ConcurrentHashMap<Int, List<RPKCharacterGroup>>()

    init {
        groups.forEach { group ->
            plugin.server.pluginManager.addPermission(Permission(
                    "rpkit.permissions.command.group.add.${group.name.value}",
                    "Allows adding the ${group.name.value} group to players",
                    PermissionDefault.OP
            ))
            plugin.server.pluginManager.addPermission(Permission(
                "rpkit.permissions.command.charactergroup.add.${group.name.value}",
                "Allows adding the ${group.name.value} group to players",
                PermissionDefault.OP
            ))
            plugin.server.pluginManager.addPermission(Permission(
                    "rpkit.permissions.command.group.remove.${group.name.value}",
                    "Allows removing the ${group.name.value} group from players",
                    PermissionDefault.OP
            ))
            plugin.server.pluginManager.addPermission(Permission(
                "rpkit.permissions.command.charactergroup.remove.${group.name.value}",
                "Allows removing the ${group.name.value} group from players",
                PermissionDefault.OP
            ))
        }
    }

    override fun getGroup(name: RPKGroupName): RPKGroup? {
        return groups.firstOrNull { group -> group.name.value == name.value }
    }

    override fun addGroup(profile: RPKProfile, group: RPKGroup, priority: Int): CompletableFuture<Void> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            if (!profile.groups.join().contains(group)) {
                val event = RPKBukkitGroupAssignProfileEvent(group, profile, priority, true)
                plugin.server.pluginManager.callEvent(event)
                if (event.isCancelled) return@runAsync
                val profileGroup = RPKProfileGroup(
                    event.profile,
                    event.group,
                    event.priority
                )
                plugin.database.getTable(RPKProfileGroupTable::class.java).insert(profileGroup).join()
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return@runAsync
                minecraftProfileService.getMinecraftProfiles(event.profile).thenAccept { minecraftProfiles ->
                    minecraftProfiles.forEach { minecraftProfile ->
                        plugin.server.scheduler.runTask(plugin, Runnable {
                            if (minecraftProfile.isOnline) {
                                val profileGroups = this.profileGroups[profileId.value]
                                    ?.plus(profileGroup)
                                    ?.toSet()
                                    ?.sortedBy(RPKProfileGroup::priority)
                                    ?: listOf(profileGroup)
                                this.profileGroups[profileId.value] = profileGroups
                                minecraftProfile.assignPermissions()
                            }
                        })
                    }
                }.join()
            }
        }
    }

    override fun addGroup(profile: RPKProfile, group: RPKGroup): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            addGroup(
                profile,
                group,
                plugin.database.getTable(RPKProfileGroupTable::class.java).get(profile).join()
                    .minByOrNull(RPKProfileGroup::priority)?.priority?.minus(1) ?: 0
            ).join()
        }
    }

    override fun removeGroup(profile: RPKProfile, group: RPKGroup): CompletableFuture<Void> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            val event = RPKBukkitGroupUnassignProfileEvent(group, profile, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val profileGroupTable = plugin.database.getTable(RPKProfileGroupTable::class.java)
            val profileGroup =
                profileGroupTable.get(event.profile).join().firstOrNull { profileGroup -> profileGroup.group == event.group }
                    ?: return@runAsync
            profileGroupTable.delete(profileGroup).join()
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return@runAsync
            minecraftProfileService.getMinecraftProfiles(event.profile).thenAccept { minecraftProfiles ->
                minecraftProfiles.forEach { minecraftProfile ->
                    plugin.server.scheduler.runTask(plugin, Runnable {
                        if (minecraftProfile.isOnline) {
                            val profileGroups = this.profileGroups[profileId.value]?.minus(profileGroup) ?: listOf(profileGroup)
                            this.profileGroups[profileId.value] = profileGroups
                            minecraftProfile.assignPermissions()
                        }
                    })
                }
            }.join()
        }
    }

    override fun getGroups(profile: RPKProfile): CompletableFuture<List<RPKGroup>> {
        val preloadedGroups = getPreloadedGroups(profile)
        if (preloadedGroups != null) return CompletableFuture.completedFuture(preloadedGroups)
        return plugin.database.getTable(RPKProfileGroupTable::class.java).get(profile).thenApply { it.map(RPKProfileGroup::group) }
    }

    override fun getPreloadedGroups(profile: RPKProfile): List<RPKGroup>? {
        val profileId = profile.id ?: return null
        return profileGroups[profileId.value]?.map(RPKProfileGroup::group)
    }

    override fun loadGroups(profile: RPKProfile): CompletableFuture<List<RPKGroup>> {
        val profileId = profile.id ?: return CompletableFuture.completedFuture(null)
        if (profileGroups.containsKey(profileId.value)) return CompletableFuture.completedFuture(profileGroups[profileId.value]?.map(RPKProfileGroup::group))
        plugin.logger.info("Loading groups for profile ${profile.name + profile.discriminator} (${profileId.value})...")
        return CompletableFuture.supplyAsync {
            val profileGroups = plugin.database.getTable(RPKProfileGroupTable::class.java).get(profile).join()
            this.profileGroups[profileId.value] = profileGroups
            plugin.logger.info("Loaded groups for profile ${profile.name + profile.discriminator} (${profileId.value}): ${profileGroups.joinToString(", ") { it.group.name.value }}")
            return@supplyAsync profileGroups.map(RPKProfileGroup::group)
        }
    }

    override fun unloadGroups(profile: RPKProfile) {
        val profileId = profile.id ?: return
        profileGroups.remove(profileId.value)
        plugin.logger.info("Unloaded groups for profile ${profile.name + profile.discriminator} (${profile.id})")
    }

    override fun getGroupPriority(profile: RPKProfile, group: RPKGroup): CompletableFuture<Int?> {
        val profileGroupTable = plugin.database.getTable(RPKProfileGroupTable::class.java)
        return profileGroupTable[profile, group].thenApply { it?.priority }
    }

    override fun setGroupPriority(profile: RPKProfile, group: RPKGroup, priority: Int): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val profileGroupTable = plugin.database.getTable(RPKProfileGroupTable::class.java)
            val profileGroup = profileGroupTable[profile, group].join() ?: return@runAsync
            profileGroup.priority = priority
            profileGroupTable.update(profileGroup).join()
            Services[RPKMinecraftProfileService::class.java]?.getMinecraftProfiles(profile)
                ?.thenAccept { minecraftProfiles ->
                    minecraftProfiles.forEach { minecraftProfile ->
                        plugin.server.scheduler.runTask(plugin, Runnable {
                            if (minecraftProfile.isOnline) {
                                minecraftProfile.assignPermissions()
                            }
                        })
                    }
                }?.join()
        }
    }

    override fun addGroup(character: RPKCharacter, group: RPKGroup, priority: Int): CompletableFuture<Void> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            if (character.groups.join().contains(group)) return@runAsync
            val event = RPKBukkitGroupAssignCharacterEvent(group, character, priority, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val characterGroup = RPKCharacterGroup(
                event.character,
                event.group,
                event.priority
            )
            plugin.database.getTable(RPKCharacterGroupTable::class.java).insert(characterGroup).join()
            val minecraftProfile = event.character.minecraftProfile ?: return@runAsync
            plugin.server.scheduler.runTask(plugin, Runnable {
                if (minecraftProfile.isOnline) {
                    val characterGroups = this.characterGroups[characterId.value]?.plus(characterGroup) ?: listOf(characterGroup)
                    this.characterGroups[characterId.value] = characterGroups
                    minecraftProfile.assignPermissions()
                }
            })
        }
    }

    override fun addGroup(character: RPKCharacter, group: RPKGroup): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            addGroup(
                character,
                group,
                plugin.database.getTable(RPKCharacterGroupTable::class.java).get(character).join()
                    .minByOrNull(RPKCharacterGroup::priority)?.priority?.minus(1) ?: 0
            ).join()
        }
    }

    override fun removeGroup(character: RPKCharacter, group: RPKGroup): CompletableFuture<Void> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            val event = RPKBukkitGroupUnassignCharacterEvent(group, character, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val characterGroupTable = plugin.database.getTable(RPKCharacterGroupTable::class.java)
            val characterGroup = characterGroupTable.get(event.character).join()
                .firstOrNull { characterGroup -> characterGroup.group == event.group }
                ?: return@runAsync
            characterGroupTable.delete(characterGroup).join()
            val minecraftProfile = event.character.minecraftProfile ?: return@runAsync
            plugin.server.scheduler.runTask(plugin, Runnable {
                if (minecraftProfile.isOnline) {
                    val characterGroups = this.characterGroups[characterId.value]?.minus(characterGroup) ?: listOf(characterGroup)
                    this.characterGroups[characterId.value] = characterGroups
                    minecraftProfile.assignPermissions()
                }
            })
        }
    }

    override fun getPreloadedGroups(character: RPKCharacter): List<RPKGroup>? {
        val characterId = character.id ?: return null
        return characterGroups[characterId.value]?.map(RPKCharacterGroup::group)
    }

    override fun loadGroups(character: RPKCharacter): CompletableFuture<List<RPKGroup>> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        if (characterGroups.containsKey(characterId.value)) return CompletableFuture.completedFuture(characterGroups[characterId.value]?.map(RPKCharacterGroup::group))
        plugin.logger.info("Loading groups for character ${character.name} (${characterId.value})...")
        return CompletableFuture.supplyAsync {
            val characterGroups = plugin.database.getTable(RPKCharacterGroupTable::class.java).get(character).join()
            this.characterGroups[characterId.value] = characterGroups
            plugin.logger.info("Loaded groups for character ${character.name} (${characterId.value}): ${characterGroups.joinToString(", ") { it.group.name.value }}")
            return@supplyAsync characterGroups.map(RPKCharacterGroup::group)
        }
    }

    override fun unloadGroups(character: RPKCharacter) {
        val characterId = character.id ?: return
        characterGroups.remove(characterId.value)
        plugin.logger.info("Unloaded groups for character ${character.name} (${characterId.value})")
    }

    override fun getGroups(character: RPKCharacter): CompletableFuture<List<RPKGroup>> {
        val preloadedGroups = getPreloadedGroups(character)
        if (preloadedGroups != null) return CompletableFuture.completedFuture(preloadedGroups)
        return plugin.database.getTable(RPKCharacterGroupTable::class.java).get(character).thenApply { it.map(RPKCharacterGroup::group) }
    }

    override fun getGroupPriority(character: RPKCharacter, group: RPKGroup): CompletableFuture<Int?> {
        val characterGroupTable = plugin.database.getTable(RPKCharacterGroupTable::class.java)
        return characterGroupTable[character, group].thenApply { it?.priority }
    }

    override fun setGroupPriority(character: RPKCharacter, group: RPKGroup, priority: Int): CompletableFuture<Void> {
        val characterGroupTable = plugin.database.getTable(RPKCharacterGroupTable::class.java)
        return characterGroupTable[character, group].thenAccept { characterGroup ->
            if (characterGroup == null) return@thenAccept
            characterGroup.priority = priority
            characterGroupTable.update(characterGroup)
            character.minecraftProfile?.assignPermissions()
        }
    }

}