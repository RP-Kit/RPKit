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

package com.rpkit.skills.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.skills.bukkit.RPKSkillsBukkit
import com.rpkit.skills.bukkit.database.create
import com.rpkit.skills.bukkit.database.jooq.Tables.RPKIT_SKILL_COOLDOWN
import com.rpkit.skills.bukkit.skills.RPKSkill
import com.rpkit.skills.bukkit.skills.RPKSkillCooldown


class RPKSkillCooldownTable(private val database: Database, private val plugin: RPKSkillsBukkit) : Table {

    private data class CharacterSkillCacheKey(
        val characterId: Int,
        val skillName: String
    )

    private val cache = if (plugin.config.getBoolean("caching.rpkit_skill_cooldown.character_id.enabled")) {
        database.cacheManager.createCache(
            "rpk-skills-bukkit.rpkit_skill_cooldown.character_id",
            CharacterSkillCacheKey::class.java,
            RPKSkillCooldown::class.java,
            plugin.config.getLong("caching.rpkit_skill_cooldown.character_id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKSkillCooldown) {
        val characterId = entity.character.id ?: return
        val skillName = entity.skill.name
        database.create
                .insertInto(
                        RPKIT_SKILL_COOLDOWN,
                        RPKIT_SKILL_COOLDOWN.CHARACTER_ID,
                        RPKIT_SKILL_COOLDOWN.SKILL_NAME,
                        RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP
                )
                .values(
                        entity.character.id,
                        entity.skill.name,
                        entity.cooldownTimestamp
                )
                .execute()
        cache?.set(CharacterSkillCacheKey(characterId, skillName), entity)
    }

    fun update(entity: RPKSkillCooldown) {
        val characterId = entity.character.id ?: return
        val skillName = entity.skill.name
        database.create
                .update(RPKIT_SKILL_COOLDOWN)
                .set(RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP, entity.cooldownTimestamp)
                .where(RPKIT_SKILL_COOLDOWN.CHARACTER_ID.eq(characterId))
                .and(RPKIT_SKILL_COOLDOWN.SKILL_NAME.eq(skillName))
                .execute()
        cache?.set(CharacterSkillCacheKey(characterId, skillName), entity)
    }

    operator fun get(character: RPKCharacter, skill: RPKSkill): RPKSkillCooldown? {
        val characterId = character.id ?: return null
        val skillName = skill.name
        val cacheKey = CharacterSkillCacheKey(characterId, skillName)
        if (cache?.containsKey(cacheKey) == true) {
            return cache[cacheKey]
        }
        val result = database.create
                .select(RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP)
                .from(RPKIT_SKILL_COOLDOWN)
                .where(RPKIT_SKILL_COOLDOWN.CHARACTER_ID.eq(character.id))
                .and(RPKIT_SKILL_COOLDOWN.SKILL_NAME.eq(skill.name))
                .fetchOne() ?: return null
        val skillCooldown = RPKSkillCooldown(
                character,
                skill,
                result.get(RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP)
        )
        cache?.set(cacheKey, skillCooldown)
        return skillCooldown
    }

    fun delete(entity: RPKSkillCooldown) {
        val characterId = entity.character.id ?: return
        val skillName = entity.skill.name
        database.create
                .deleteFrom(RPKIT_SKILL_COOLDOWN)
                .where(RPKIT_SKILL_COOLDOWN.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_SKILL_COOLDOWN.SKILL_NAME.eq(entity.skill.name))
                .execute()
        cache?.remove(CharacterSkillCacheKey(characterId, skillName))
    }

}