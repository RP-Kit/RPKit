/*
 * Copyright 2019 Ren Binden
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

package com.rpkit.skills.bukkit.skills

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.ServiceProvider


interface RPKSkillPointProvider: ServiceProvider {

    fun getSkillPoints(character: RPKCharacter, skillType: RPKSkillType): Int

    @Deprecated("Skill point providers should not need to allow plugins to set skill points. Some already do not support this.")
    fun setSkillPoints(character: RPKCharacter, skillType: RPKSkillType, points: Int)

}