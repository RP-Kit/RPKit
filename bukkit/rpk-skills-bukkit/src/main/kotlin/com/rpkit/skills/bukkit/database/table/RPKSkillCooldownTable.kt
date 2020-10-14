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
import com.rpkit.skills.bukkit.database.jooq.Tables.RPKIT_SKILL_COOLDOWN
import com.rpkit.skills.bukkit.skills.RPKSkill
import com.rpkit.skills.bukkit.skills.RPKSkillCooldown
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKSkillCooldownTable(private val database: Database, private val plugin: RPKSkillsBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_skill_cooldown.character_id.enabled")) {
        database.cacheManager.createCache("rpk-skills-bukkit.rpkit_skill_cooldown.character_id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, MutableMap::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_skill_cooldown.character_id.size"))).build())
    } else {
        null
    }

    fun insert(entity: RPKSkillCooldown) {
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
        if (cache != null) {
            val skillCooldowns = cache[entity.character.id] as? MutableMap<String, RPKSkillCooldown> ?: mutableMapOf()
            skillCooldowns[entity.skill.name] = entity
            cache.put(entity.character.id, skillCooldowns)
        }
    }

    fun update(entity: RPKSkillCooldown) {
        database.create
                .update(RPKIT_SKILL_COOLDOWN)
                .set(RPKIT_SKILL_COOLDOWN.CHARACTER_ID, entity.character.id)
                .set(RPKIT_SKILL_COOLDOWN.SKILL_NAME, entity.skill.name)
                .set(RPKIT_SKILL_COOLDOWN.COOLDOWN_TIMESTAMP, entity.cooldownTimestamp)
                .where(RPKIT_SKILL_COOLDOWN.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_SKILL_COOLDOWN.SKILL_NAME.eq(entity.skill.name))
                .execute()
        if (cache != null) {
            val skillCooldowns = cache[entity.character.id] as? MutableMap<String, RPKSkillCooldown> ?: mutableMapOf()
            skillCooldowns[entity.skill.name] = entity
            cache.put(entity.character.id, skillCooldowns)
        }
    }

    operator fun get(character: RPKCharacter, skill: RPKSkill): RPKSkillCooldown? {
        if (cache?.containsKey(character.id) == true) {
            val skillCooldowns = cache[character.id]
            if (skillCooldowns.containsKey(skill.name)) {
                return skillCooldowns[skill.name] as RPKSkillCooldown
            }
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
        if (cache != null) {
            val skillCooldowns = cache[character.id] as? MutableMap<String, RPKSkillCooldown> ?: mutableMapOf()
            skillCooldowns[skill.name] = skillCooldown
            cache.put(character.id, skillCooldowns)
        }
        return skillCooldown
    }

    fun delete(entity: RPKSkillCooldown) {
        database.create
                .deleteFrom(RPKIT_SKILL_COOLDOWN)
                .where(RPKIT_SKILL_COOLDOWN.CHARACTER_ID.eq(entity.character.id))
                .and(RPKIT_SKILL_COOLDOWN.SKILL_NAME.eq(entity.skill.name))
                .execute()
        if (cache != null) {
            val skillCooldowns = cache[entity.character.id] as? MutableMap<String, RPKSkillCooldown> ?: mutableMapOf()
            skillCooldowns.remove(entity.skill.name)
            cache.put(entity.character.id, skillCooldowns)
        }
    }

}