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

package com.rpkit.skills.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.skills.bukkit.RPKSkillsBukkit
import com.rpkit.skills.bukkit.database.create
import com.rpkit.skills.bukkit.database.jooq.Tables.RPKIT_SKILL_COOLDOWN
import com.rpkit.skills.bukkit.skills.RPKSkill
import com.rpkit.skills.bukkit.skills.RPKSkillCooldown
import com.rpkit.skills.bukkit.skills.RPKSkillName
import com.rpkit.skills.bukkit.skills.RPKSkillService
import java.util.concurrent.CompletableFuture


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

    fun insert(entity: RPKSkillCooldown): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val skillName = entity.skill.name
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_SKILL_COOLDOWN,
                    RPKIT_SKILL_COOLDOWN.CHARACTER_ID,
                    RPKIT_SKILL_COOLDOWN.SKILL_NAME,
                    RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP
                )
                .values(
                    characterId.value,
                    entity.skill.name.value,
                    entity.cooldownTimestamp
                )
                .execute()
            cache?.set(CharacterSkillCacheKey(characterId.value, skillName.value), entity)
        }
    }

    fun update(entity: RPKSkillCooldown): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val skillName = entity.skill.name
        return CompletableFuture.runAsync {
            database.create
                .update(RPKIT_SKILL_COOLDOWN)
                .set(RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP, entity.cooldownTimestamp)
                .where(RPKIT_SKILL_COOLDOWN.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_SKILL_COOLDOWN.SKILL_NAME.eq(skillName.value))
                .execute()
            cache?.set(CharacterSkillCacheKey(characterId.value, skillName.value), entity)
        }
    }

    operator fun get(character: RPKCharacter, skill: RPKSkill): CompletableFuture<RPKSkillCooldown?> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        val skillName = skill.name
        val cacheKey = CharacterSkillCacheKey(characterId.value, skillName.value)
        if (cache?.containsKey(cacheKey) == true) {
            return CompletableFuture.completedFuture(cache[cacheKey])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP)
                .from(RPKIT_SKILL_COOLDOWN)
                .where(RPKIT_SKILL_COOLDOWN.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_SKILL_COOLDOWN.SKILL_NAME.eq(skill.name.value))
                .fetchOne() ?: return@supplyAsync null
            val skillCooldown = RPKSkillCooldown(
                character,
                skill,
                result.get(RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP)
            )
            cache?.set(cacheKey, skillCooldown)
            return@supplyAsync skillCooldown
        }
    }

    operator fun get(character: RPKCharacter): CompletableFuture<List<RPKSkillCooldown>> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(null)
        val skillService = Services[RPKSkillService::class.java] ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.supplyAsync {
            database.create
                .selectFrom(RPKIT_SKILL_COOLDOWN)
                .where(RPKIT_SKILL_COOLDOWN.CHARACTER_ID.eq(characterId.value))
                .fetch()
                .mapNotNull { result ->
                    val skill = skillService.getSkill(RPKSkillName(result.skillName)) ?: return@mapNotNull null
                    RPKSkillCooldown(
                        character,
                        skill,
                        result.cooldownTimestamp
                    )
                }
        }
    }

    fun delete(entity: RPKSkillCooldown): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        val skillName = entity.skill.name
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_SKILL_COOLDOWN)
                .where(RPKIT_SKILL_COOLDOWN.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_SKILL_COOLDOWN.SKILL_NAME.eq(entity.skill.name.value))
                .execute()
            cache?.remove(CharacterSkillCacheKey(characterId.value, skillName.value))
        }
    }

}