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

package com.rpkit.characters.bukkit.web.character

import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class CharacterPutRequest(
    val name: String,
    val gender: String?,
    val age: Int,
    val race: String?,
    val description: String,
    val isDead: Boolean,
    val isProfileHidden: Boolean,
    val isNameHidden: Boolean,
    val isGenderHidden: Boolean,
    val isAgeHidden: Boolean,
    val isRaceHidden: Boolean,
    val isDescriptionHidden: Boolean
) {
    companion object {
        val lens = Body.auto<CharacterPutRequest>().toLens()
    }
}