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

package com.rpkit.professions.bukkit.profession

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Service
import java.time.Duration


interface RPKProfessionService : Service {

    val professions: List<RPKProfession>

    fun getProfession(name: RPKProfessionName): RPKProfession?

    fun getProfessions(character: RPKCharacter): List<RPKProfession>

    fun addProfession(character: RPKCharacter, profession: RPKProfession)

    fun removeProfession(character: RPKCharacter, profession: RPKProfession)

    fun getProfessionLevel(character: RPKCharacter, profession: RPKProfession): Int

    fun setProfessionLevel(character: RPKCharacter, profession: RPKProfession, level: Int)

    fun getProfessionExperience(character: RPKCharacter, profession: RPKProfession): Int

    fun setProfessionExperience(character: RPKCharacter, profession: RPKProfession, experience: Int)

    fun getProfessionChangeCooldown(character: RPKCharacter): Duration

    fun setProfessionChangeCooldown(character: RPKCharacter, cooldown: Duration)


}