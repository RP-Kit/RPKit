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

package com.rpkit.statbuilds.bukkit.statbuild

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Service
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttribute

interface RPKStatBuildService : Service {

    fun getStatPoints(character: RPKCharacter, statAttribute: RPKStatAttribute): Int
    fun setStatPoints(character: RPKCharacter, statAttribute: RPKStatAttribute, amount: Int)
    fun getMaxStatPoints(character: RPKCharacter, statAttribute: RPKStatAttribute): Int
    fun getTotalStatPoints(character: RPKCharacter): Int
    fun getUnassignedStatPoints(character: RPKCharacter): Int
    fun getAssignedStatPoints(character: RPKCharacter): Int

}