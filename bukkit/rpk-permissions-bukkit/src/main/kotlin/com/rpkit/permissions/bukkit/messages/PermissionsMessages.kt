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

package com.rpkit.permissions.bukkit.messages

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroup
import com.rpkit.players.bukkit.profile.RPKProfile

class PermissionsMessages(plugin: RPKPermissionsBukkit) : BukkitMessages(plugin) {

    class GroupAddValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKGroup, profile: RPKProfile) = message.withParameters(
            "group" to group.name.value,
            "player" to profile.name.value
        )
    }

    class GroupRemoveValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKGroup, profile: RPKProfile) = message.withParameters(
            "group" to group.name.value,
            "player" to profile.name.value
        )
    }

    class GroupListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKGroup) = message.withParameters(
            "group" to group.name.value
        )
    }

    class GroupSwitchPriorityInvalidGroupMessage(private val message: ParameterizedMessage) {
        fun withParameters(groupName: String) = message.withParameters(
            "group_name" to groupName
        )
    }

    class GroupSwitchPriorityGroupNotPresentMessage(private val message: ParameterizedMessage) {
        fun withParameters(profile: RPKProfile, group: RPKGroup) = message.withParameters(
            "profile" to profile.name + profile.discriminator,
            "group" to group.name.value
        )
    }

    class GroupSwitchPriorityValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(profile: RPKProfile, group1: RPKGroup, group2: RPKGroup) = message.withParameters(
            "profile" to profile.name + profile.discriminator,
            "group1" to group1.name.value,
            "group2" to group2.name.value
        )
    }

    class CharacterGroupAddValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKGroup, character: RPKCharacter) = message.withParameters(
            "group" to group.name.value,
            "character" to character.name
        )
    }

    class CharacterGroupRemoveValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKGroup, character: RPKCharacter) = message.withParameters(
            "group" to group.name.value,
            "character" to character.name
        )
    }

    class GroupViewTitleMessage(private val message: ParameterizedMessage) {
        fun withParameters(profile: RPKProfile) = message.withParameters(
            "player" to profile.name + profile.discriminator
        )
    }

    class GroupViewItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKGroup) = message.withParameters(
            "group" to group.name.value
        )
    }

    class CharacterGroupViewTitleMessage(private val message: ParameterizedMessage) {
        fun withParameters(character: RPKCharacter) = message.withParameters(
            "character" to character.name
        )
    }

    class CharacterGroupViewItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKGroup) = message.withParameters(
            "group" to group.name.value
        )
    }

    class CharacterGroupSwitchPriorityInvalidGroupMessage(private val message: ParameterizedMessage) {
        fun withParameters(groupName: String) = message.withParameters(
            "group_name" to groupName
        )
    }

    class CharacterGroupSwitchPriorityInvalidGroupNotPresentMessage(private val message: ParameterizedMessage) {
        fun withParameters(character: RPKCharacter, group: RPKGroup) = message.withParameters(
            "character" to character.name,
            "group" to group.name.value
        )
    }

    class CharacterGroupSwitchPriorityValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(character: RPKCharacter, group1: RPKGroup, group2: RPKGroup) = message.withParameters(
            "character" to character.name,
            "group1" to group1.name.value,
            "group2" to group2.name.value
        )
    }

    class NoPermissionGroupAddGroupMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKGroup) = message.withParameters(
            "group" to group.name.value
        )
    }

    class NoPermissionGroupRemoveGroupMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKGroup) = message.withParameters(
            "group" to group.name.value
        )
    }

    class NoPermissionCharacterGroupAddGroupMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKGroup) = message.withParameters(
            "group" to group.name.value
        )
    }

    class NoPermissionCharacterGroupRemoveGroupMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKGroup) = message.withParameters(
            "group" to group.name.value
        )
    }

    val groupUsage = get("group-usage")
    val groupAddValid = getParameterized("group-add-valid").let(::GroupAddValidMessage)
    val groupAddInvalidGroup = get("group-add-invalid-group")
    val groupAddInvalidPlayer = get("group-add-invalid-player")
    val groupAddUsage = get("group-add-usage")
    val groupRemoveValid = getParameterized("group-remove-valid").let(::GroupRemoveValidMessage)
    val groupRemoveInvalidGroup = get("group-remove-invalid-group")
    val groupRemoveInvalidPlayer = get("group-remove-invalid-player")
    val groupRemoveUsage = get("group-remove-usage")
    val groupListTitle = get("group-list-title")
    val groupListItem = getParameterized("group-list-item").let(::GroupListItemMessage)
    val groupViewInvalidPlayer = get("group-view-invalid-player")
    val groupViewTitle = getParameterized("group-view-title").let(::GroupViewTitleMessage)
    val groupViewItem = getParameterized("group-view-item").let(::GroupViewItemMessage)
    val groupSwitchPriorityUsage = get("group-switch-priority-usage")
    val groupSwitchPriorityInvalidTarget = get("group-switch-priority-invalid-target")
    val groupSwitchPriorityInvalidGroup = getParameterized("group-switch-priority-invalid-group").let(::GroupSwitchPriorityInvalidGroupMessage)
    val groupSwitchPriorityInvalidGroupNotPresent = getParameterized("group-switch-priority-group-not-present").let(::GroupSwitchPriorityGroupNotPresentMessage)
    val groupSwitchPriorityValid = getParameterized("group-switch-priority-valid").let(::GroupSwitchPriorityValidMessage)
    val groupPrepareSwitchPriorityUsage = get("group-prepare-switch-priority-usage")
    val characterGroupUsage = get("character-group-usage")
    val characterGroupAddValid = getParameterized("character-group-add-valid").let(::CharacterGroupAddValidMessage)
    val characterGroupAddInvalidGroup = get("character-group-add-invalid-group")
    val characterGroupAddInvalidPlayer = get("character-group-add-invalid-player")
    val characterGroupAddUsage = get("character-group-add-usage")
    val characterGroupRemoveValid = getParameterized("character-group-remove-valid").let(::CharacterGroupRemoveValidMessage)
    val characterGroupRemoveInvalidGroup = get("character-group-remove-invalid-group")
    val characterGroupRemoveInvalidPlayer = get("character-group-remove-invalid-player")
    val characterGroupRemoveUsage = get("character-group-remove-usage")
    val characterGroupViewUsage = get("character-group-view-usage")
    val characterGroupViewInvalidProfileName = get("character-group-view-invalid-profile-name")
    val characterGroupViewInvalidProfile = get("character-group-view-invalid-profile")
    val characterGroupViewTitle = getParameterized("character-group-view-title").let(::CharacterGroupViewTitleMessage)
    val characterGroupViewItem = getParameterized("character-group-view-item").let(::CharacterGroupViewItemMessage)
    val characterGroupSwitchPriorityUsage = get("character-group-switch-priority-usage")
    val characterGroupSwitchPriorityInvalidProfileName = get("character-group-switch-priority-invalid-profile-name")
    val characterGroupSwitchPriorityInvalidProfile = get("character-group-switch-priority-invalid-profile")
    val characterGroupSwitchPriorityInvalidGroup = getParameterized("character-group-switch-priority-invalid-group").let(::CharacterGroupSwitchPriorityInvalidGroupMessage)
    val characterGroupSwitchPriorityInvalidGroupNotPresent = getParameterized("character-group-switch-priority-invalid-group-not-present").let(::CharacterGroupSwitchPriorityInvalidGroupNotPresentMessage)
    val characterGroupSwitchPriorityValid = getParameterized("character-group-switch-priority-valid").let(::CharacterGroupSwitchPriorityValidMessage)
    val characterGroupPrepareSwitchPriorityUsage = get("character-group-prepare-switch-priority")
    val noProfile = get("no-profile")
    val noMinecraftProfileSelf = get("no-minecraft-profile-self")
    val noMinecraftProfileOther = get("no-minecraft-profile-other")
    val noCharacter = get("no-character")
    val noPermissionGroupAdd = get("no-permission-group-add")
    val noPermissionGroupRemove = get("no-permission-group-remove")
    val noPermissionGroupAddGroup = getParameterized("no-permission-group-add-group").let(::NoPermissionGroupAddGroupMessage)
    val noPermissionGroupRemoveGroup = getParameterized("no-permission-group-remove-group").let(::NoPermissionGroupRemoveGroupMessage)
    val noPermissionGroupList = get("no-permission-group-list")
    val noPermissionGroupView = get("no-permission-group-view")
    val noPermissionGroupSwitchPriority = get("no-permission-group-switch-priority")
    val noPermissionCharacterGroupAdd = get("no-permission-character-group-add")
    val noPermissionCharacterGroupRemove = get("no-permission-character-group-remove")
    val noPermissionCharacterGroupAddGroup = getParameterized("no-permission-character-group-add-group").let(::NoPermissionCharacterGroupAddGroupMessage)
    val noPermissionCharacterGroupRemoveGroup = getParameterized("no-permission-character-group-remove-group").let(::NoPermissionCharacterGroupRemoveGroupMessage)
    val noPermissionCharacterGroupView = get("no-permission-character-group-view")
    val noPermissionCharacterGroupSwitchPriority = get("no-permission-character-group-switch-priority")
    val noProfileService = get("no-profile-service")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noCharacterService = get("no-character-service")
    val noGroupService = get("no-group-service")
    val notFromConsole = get("not-from-console")

}