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

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.group.hasPermission
import com.rpkit.players.bukkit.profile.RPKProfile
import java.util.concurrent.CompletableFuture

/**
 * Character card field for name.
 */
class NameField : HideableCharacterCardField {

    override val name = "name"
    override fun get(character: RPKCharacter): CompletableFuture<String> {
        return isHidden(character).thenApply { hidden ->
            if (hidden) {
                "[HIDDEN]"
            } else {
                character.name
            }
        }
    }

    override fun get(character: RPKCharacter, viewer: RPKProfile): CompletableFuture<String> {
        return isHidden(character).thenApplyAsync { hidden ->
            if (viewer.hasPermission("rpkit.characters.command.character.card.bypasshidden").join() || !hidden) {
                return@thenApplyAsync character.name
            } else {
                return@thenApplyAsync "[HIDDEN]"
            }
        }
    }

    override fun isHidden(character: RPKCharacter): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(character.isNameHidden)
    }

    override fun setHidden(character: RPKCharacter, hidden: Boolean): CompletableFuture<Void> {
        character.isNameHidden = hidden
        return Services[RPKCharacterService::class.java]?.updateCharacter(character)
            ?.thenApply { null }
            ?: CompletableFuture.completedFuture(null)
    }

}
