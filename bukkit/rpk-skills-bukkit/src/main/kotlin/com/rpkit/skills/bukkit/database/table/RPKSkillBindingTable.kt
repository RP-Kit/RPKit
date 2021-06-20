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
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.extension.toByteArray
import com.rpkit.core.bukkit.extension.toItemStack
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.skills.bukkit.RPKSkillsBukkit
import com.rpkit.skills.bukkit.database.create
import com.rpkit.skills.bukkit.database.jooq.Tables.RPKIT_SKILL_BINDING
import com.rpkit.skills.bukkit.skills.RPKSkillBinding
import com.rpkit.skills.bukkit.skills.RPKSkillName
import com.rpkit.skills.bukkit.skills.RPKSkillService
import java.util.concurrent.CompletableFuture

class RPKSkillBindingTable(private val database: Database, private val plugin: RPKSkillsBukkit): Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_skill_binding.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-skills-bukkit.rpkit_skill_binding.id",
            Int::class.javaObjectType,
            RPKSkillBinding::class.java,
            plugin.config.getLong("caching.rpkit_skill_binding.id.size")
        )
    } else {
        null
    }

    fun insert(entity: RPKSkillBinding): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .insertInto(
                    RPKIT_SKILL_BINDING,
                    RPKIT_SKILL_BINDING.CHARACTER_ID,
                    RPKIT_SKILL_BINDING.ITEM,
                    RPKIT_SKILL_BINDING.SKILL_NAME
                )
                .values(
                    characterId.value,
                    entity.item.toByteArray(),
                    entity.skill.name.value
                )
                .execute()
        }
    }

    fun update(entity: RPKSkillBinding): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create.update(RPKIT_SKILL_BINDING)
                .set(RPKIT_SKILL_BINDING.CHARACTER_ID, characterId.value)
                .set(RPKIT_SKILL_BINDING.ITEM, entity.item.toByteArray())
                .set(RPKIT_SKILL_BINDING.SKILL_NAME, entity.skill.name.value)
                .where(RPKIT_SKILL_BINDING.ID.eq(entity.id))
                .execute()
        }
    }

    fun get(id: Int): CompletableFuture<RPKSkillBinding?> {
        if (cache?.containsKey(id) == true) {
            return CompletableFuture.completedFuture(cache[id])
        }
        return CompletableFuture.supplyAsync {
            val result = database.create
                .select(
                    RPKIT_SKILL_BINDING.CHARACTER_ID,
                    RPKIT_SKILL_BINDING.ITEM,
                    RPKIT_SKILL_BINDING.SKILL_NAME
                )
                .from(RPKIT_SKILL_BINDING)
                .where(RPKIT_SKILL_BINDING.ID.eq(id))
                .fetchOne() ?: return@supplyAsync null
            val characterService = Services[RPKCharacterService::class.java] ?: return@supplyAsync null
            val characterId = result[RPKIT_SKILL_BINDING.CHARACTER_ID]
            val character = characterService.getCharacter(RPKCharacterId(characterId)).join()
            val skillService = Services[RPKSkillService::class.java] ?: return@supplyAsync null
            val skillName = result[RPKIT_SKILL_BINDING.SKILL_NAME]
            val skill = skillService.getSkill(RPKSkillName(skillName))
            if (character != null && skill != null) {
                val skillBinding = RPKSkillBinding(
                    id,
                    character,
                    result[RPKIT_SKILL_BINDING.ITEM].toItemStack(),
                    skill
                )
                cache?.set(id, skillBinding)
                return@supplyAsync skillBinding
            } else {
                database.create
                    .deleteFrom(RPKIT_SKILL_BINDING)
                    .where(RPKIT_SKILL_BINDING.ID.eq(id))
                    .execute()
                cache?.remove(id)
                return@supplyAsync null
            }
        }
    }

    fun get(character: RPKCharacter): CompletableFuture<List<RPKSkillBinding>> {
        val characterId = character.id ?: return CompletableFuture.completedFuture(emptyList())
        return CompletableFuture.supplyAsync {
            val results = database.create
                .select(
                    RPKIT_SKILL_BINDING.ID,
                    RPKIT_SKILL_BINDING.CHARACTER_ID,
                    RPKIT_SKILL_BINDING.ITEM,
                    RPKIT_SKILL_BINDING.SKILL_NAME
                )
                .from(RPKIT_SKILL_BINDING)
                .where(RPKIT_SKILL_BINDING.CHARACTER_ID.eq(characterId.value))
                .fetch()
            val skillService = Services[RPKSkillService::class.java] ?: return@supplyAsync emptyList()
            return@supplyAsync results.mapNotNull { result ->
                val skillName = result[RPKIT_SKILL_BINDING.SKILL_NAME]
                val skill = skillService.getSkill(RPKSkillName(skillName))
                if (skill != null) {
                    val id = result[RPKIT_SKILL_BINDING.ID]
                    val skillBinding = RPKSkillBinding(
                        id,
                        character,
                        result[RPKIT_SKILL_BINDING.ITEM].toItemStack(),
                        skill
                    )
                    cache?.set(id, skillBinding)
                    skillBinding
                } else {
                    null
                }
            }
        }
    }

    fun delete(entity: RPKSkillBinding): CompletableFuture<Void> {
        val characterId = entity.character.id ?: return CompletableFuture.completedFuture(null)
        return CompletableFuture.runAsync {
            database.create
                .deleteFrom(RPKIT_SKILL_BINDING)
                .where(RPKIT_SKILL_BINDING.CHARACTER_ID.eq(characterId.value))
                .and(RPKIT_SKILL_BINDING.ITEM.eq(entity.item.toByteArray()))
                .execute()
        }
    }
}