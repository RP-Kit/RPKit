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

    class GroupListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(group: RPKGroup) = message.withParameters(
            "group" to group.name.value
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

    val groupUsage = get("group-usage")
    val groupAddValid = getParameterized("group-add-valid").let(::GroupAddValidMessage)
    val groupAddInvalidGroup = get("group-add-invalid-group")
    val groupAddInvalidPlayer = get("group-add-invalid-player")
    val groupAddUsage = get("group-add-usage")
    val groupRemoveValid = get("group-remove-valid")
    val groupRemoveInvalidGroup = get("group-remove-invalid-group")
    val groupRemoveInvalidPlayer = get("group-remove-invalid-player")
    val groupRemoveUsage = get("group-remove-usage")
    val groupListTitle = get("group-list-title")
    val groupListItem = getParameterized("group-list-item").let(::GroupListItemMessage)
    val groupViewInvalidPlayer = get("group-view-invalid-player")
    val groupViewTitle = getParameterized("group-view-title").let(::GroupViewTitleMessage)
    val groupViewItem = getParameterized("group-view-item").let(::GroupViewItemMessage)
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
    val noProfile = get("no-profile")
    val noMinecraftProfile = get("no-minecraft-profile")
    val noCharacter = get("no-character")
    val noPermissionGroupAdd = get("no-permission-group-add")
    val noPermissionGroupRemove = get("no-permission-group-remove")
    val noPermissionGroupAddGroup = getParameterized("no-permission-group-add-group").let(::NoPermissionGroupAddGroupMessage)
    val noPermissionGroupRemoveGroup = getParameterized("no-permission-group-remove-group").let(::NoPermissionGroupRemoveGroupMessage)
    val noPermissionGroupList = get("no-permission-group-list")
    val noPermissionGroupView = get("no-permission-group-view")
    val noPermissionCharacterGroupView = get("no-permission-character-group-view")
    val noProfileService = get("no-profile-service")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noCharacterService = get("no-character-service")
    val noGroupService = get("no-group-service")
    val notFromConsole = get("not-from-console")

}