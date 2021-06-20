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
import java.util.concurrent.CompletableFuture


interface RPKProfessionService : Service {

    val professions: List<RPKProfession>
    fun getProfession(name: RPKProfessionName): RPKProfession?
    fun getProfessions(character: RPKCharacter): CompletableFuture<List<RPKProfession>>
    fun addProfession(character: RPKCharacter, profession: RPKProfession): CompletableFuture<Void>
    fun removeProfession(character: RPKCharacter, profession: RPKProfession): CompletableFuture<Void>
    fun getProfessionLevel(character: RPKCharacter, profession: RPKProfession): CompletableFuture<Int>
    fun setProfessionLevel(character: RPKCharacter, profession: RPKProfession, level: Int): CompletableFuture<Void>
    fun getProfessionExperience(character: RPKCharacter, profession: RPKProfession): CompletableFuture<Int>
    fun setProfessionExperience(character: RPKCharacter, profession: RPKProfession, experience: Int): CompletableFuture<Void>
    fun getProfessionChangeCooldown(character: RPKCharacter): CompletableFuture<Duration>
    fun setProfessionChangeCooldown(character: RPKCharacter, cooldown: Duration): CompletableFuture<Void>
    fun getPreloadedProfessions(character: RPKCharacter): List<RPKProfession>?
    fun loadProfessions(character: RPKCharacter): CompletableFuture<List<RPKProfession>>
    fun unloadProfessions(character: RPKCharacter)
    fun getPreloadedProfessionLevel(character: RPKCharacter, profession: RPKProfession): Int?
    fun getPreloadedProfessionExperience(character: RPKCharacter, profession: RPKProfession): Int?
    fun loadProfessionExperience(character: RPKCharacter, profession: RPKProfession): CompletableFuture<Int?>
    fun unloadProfessionExperience(character: RPKCharacter, profession: RPKProfession)

}