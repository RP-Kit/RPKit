/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.characters.bukkit.character.field

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionDefault

/**
 * Character card field service implementation.
 */
class RPKCharacterCardFieldServiceImpl(override val plugin: RPKCharactersBukkit) : RPKCharacterCardFieldService {

    override val characterCardFields = mutableListOf<CharacterCardField>()

    override fun addCharacterCardField(field: CharacterCardField) {
        if (field is HideableCharacterCardField) {
            plugin.server.pluginManager.addPermission(
                Permission(
                    "rpkit.characters.command.character.hide.${field.name}",
                    "Allows hiding your character's ${field.name}",
                    PermissionDefault.TRUE
                )
            )
            plugin.server.pluginManager.addPermission(
                Permission(
                    "rpkit.characters.command.character.unhide.${field.name}",
                    "Allows unhiding your character's ${field.name}",
                    PermissionDefault.TRUE
                )
            )
        }
        characterCardFields.add(field)
    }

    override fun removeCharacterCardField(field: CharacterCardField) {
        if (field is HideableCharacterCardField) {
            plugin.server.pluginManager.removePermission("rpkit.characters.command.character.hide.${field.name}")
            plugin.server.pluginManager.removePermission("rpkit.characters.command.character.unhide.${field.name}")
        }
        characterCardFields.remove(field)
    }

}