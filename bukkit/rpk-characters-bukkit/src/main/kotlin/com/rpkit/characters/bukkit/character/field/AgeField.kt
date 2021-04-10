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

package com.rpkit.characters.bukkit.character.field

import com.rpkit.characters.bukkit.character.RPKCharacter
import java.util.concurrent.CompletableFuture

/**
 * A character card field for age.
 */
class AgeField : HideableCharacterCardField {

    override val name = "age"
    override fun get(character: RPKCharacter): CompletableFuture<String> {
        return isHidden(character).thenApply { hidden ->
            if (hidden) {
                return@thenApply "[HIDDEN]"
            } else {
                return@thenApply character.age.toString()
            }
        }
    }

    override fun isHidden(character: RPKCharacter): CompletableFuture<Boolean> {
        return CompletableFuture.completedFuture(character.isAgeHidden)
    }

    override fun setHidden(character: RPKCharacter, hidden: Boolean): CompletableFuture<Void> {
        character.isAgeHidden = hidden
        return CompletableFuture.completedFuture(null)
    }

}
