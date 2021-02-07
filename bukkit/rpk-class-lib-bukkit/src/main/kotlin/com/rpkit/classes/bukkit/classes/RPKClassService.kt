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

package com.rpkit.classes.bukkit.classes

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Service


interface RPKClassService : Service {

    val classes: List<RPKClass>
    fun getClass(name: RPKClassName): RPKClass?
    fun getClass(character: RPKCharacter): RPKClass?
    fun setClass(character: RPKCharacter, `class`: RPKClass)
    fun getLevel(character: RPKCharacter, `class`: RPKClass): Int
    fun setLevel(character: RPKCharacter, `class`: RPKClass, level: Int)
    fun getExperience(character: RPKCharacter, `class`: RPKClass): Int
    fun setExperience(character: RPKCharacter, `class`: RPKClass, experience: Int)

}