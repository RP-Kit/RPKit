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

package com.rpkit.skills.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.bukkit.util.toByteArray
import com.rpkit.core.bukkit.util.toItemStack
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.skills.bukkit.RPKSkillsBukkit
import com.rpkit.skills.bukkit.database.jooq.rpkit.Tables.RPKIT_SKILL_BINDING
import com.rpkit.skills.bukkit.skills.RPKSkillBinding
import com.rpkit.skills.bukkit.skills.RPKSkillProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType


class RPKSkillBindingTable(database: Database, private val plugin: RPKSkillsBukkit): Table<RPKSkillBinding>(database, RPKSkillBinding::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_skill_binding.id.enabled")) {
        database.cacheManager.createCache("rpk-skills-bukkit.rpkit_skill_binding.id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKSkillBinding::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_skill_binding.id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_SKILL_BINDING)
                .column(RPKIT_SKILL_BINDING.ID, SQLDataType.INTEGER.identity(true))
                .column(RPKIT_SKILL_BINDING.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_SKILL_BINDING.ITEM, SQLDataType.BLOB)
                .column(RPKIT_SKILL_BINDING.SKILL_NAME, SQLDataType.VARCHAR(256))
                .constraints(
                        constraint("pk_rpkit_skill_binding").primaryKey(RPKIT_SKILL_BINDING.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.8.0")
        }
    }

    override fun insert(entity: RPKSkillBinding): Int {
        database.create
                .insertInto(
                        RPKIT_SKILL_BINDING,
                        RPKIT_SKILL_BINDING.CHARACTER_ID,
                        RPKIT_SKILL_BINDING.ITEM,
                        RPKIT_SKILL_BINDING.SKILL_NAME
                )
                .values(
                        entity.character.id,
                        entity.item.toByteArray(),
                        entity.skill.name
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKSkillBinding) {
        database.create.update(RPKIT_SKILL_BINDING)
                .set(RPKIT_SKILL_BINDING.CHARACTER_ID, entity.character.id)
                .set(RPKIT_SKILL_BINDING.ITEM, entity.item.toByteArray())
                .set(RPKIT_SKILL_BINDING.SKILL_NAME, entity.skill.name)
                .where(RPKIT_SKILL_BINDING.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKSkillBinding? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        }
        val result = database.create
                .select(
                        RPKIT_SKILL_BINDING.CHARACTER_ID,
                        RPKIT_SKILL_BINDING.ITEM,
                        RPKIT_SKILL_BINDING.SKILL_NAME
                )
                .from(RPKIT_SKILL_BINDING)
                .where(RPKIT_SKILL_BINDING.ID.eq(id))
                .fetchOne() ?: return null
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val characterId = result[RPKIT_SKILL_BINDING.CHARACTER_ID]
        val character = characterProvider.getCharacter(characterId)
        val skillProvider = plugin.core.serviceManager.getServiceProvider(RPKSkillProvider::class)
        val skillName = result[RPKIT_SKILL_BINDING.SKILL_NAME]
        val skill = skillProvider.getSkill(skillName)
        if (character != null && skill != null) {
            val skillBinding = RPKSkillBinding(
                    id,
                    character,
                    result[RPKIT_SKILL_BINDING.ITEM].toItemStack(),
                    skill
            )
            cache?.put(id, skillBinding)
            return skillBinding
        } else {
            database.create
                    .deleteFrom(RPKIT_SKILL_BINDING)
                    .where(RPKIT_SKILL_BINDING.ID.eq(id))
                    .execute()
            cache?.remove(id)
            return null
        }
    }

    fun get(character: RPKCharacter): List<RPKSkillBinding> {
        val results = database.create
                .select(RPKIT_SKILL_BINDING.ID)
                .from(RPKIT_SKILL_BINDING)
                .where(RPKIT_SKILL_BINDING.CHARACTER_ID.eq(character.id))
                .fetch()
        return results.mapNotNull { result -> get(result[RPKIT_SKILL_BINDING.ID]) }
    }

    override fun delete(entity: RPKSkillBinding) {
        database.create
                .deleteFrom(RPKIT_SKILL_BINDING)
                .where(RPKIT_SKILL_BINDING.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }
}