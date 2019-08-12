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

package com.rpkit.craftingskill.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.craftingskill.bukkit.RPKCraftingSkillBukkit
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingAction
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingExperienceValue
import com.rpkit.craftingskill.bukkit.database.jooq.rpkit.Tables.RPKIT_CRAFTING_EXPERIENCE
import org.bukkit.Material
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType.INTEGER
import org.jooq.impl.SQLDataType.VARCHAR


class RPKCraftingExperienceTable(database: Database, private val plugin: RPKCraftingSkillBukkit): Table<RPKCraftingExperienceValue>(database, RPKCraftingExperienceValue::class) {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_crafting_experience_value.id.enabled")) {
        database.cacheManager.createCache("rpk-crafting-skill-bukkit.rpkit_crafting_experience_value.id", CacheConfigurationBuilder
                .newCacheConfigurationBuilder(Int::class.javaObjectType, RPKCraftingExperienceValue::class.java,
                        ResourcePoolsBuilder.heap(plugin.config.getLong("caching.rpkit_crafting_experience_value.id.size"))).build())
    } else {
        null
    }

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_CRAFTING_EXPERIENCE)
                .column(RPKIT_CRAFTING_EXPERIENCE.ID, INTEGER.identity(true))
                .column(RPKIT_CRAFTING_EXPERIENCE.CHARACTER_ID, INTEGER)
                .column(RPKIT_CRAFTING_EXPERIENCE.ACTION, VARCHAR(8))
                .column(RPKIT_CRAFTING_EXPERIENCE.MATERIAL, VARCHAR(255))
                .column(RPKIT_CRAFTING_EXPERIENCE.EXPERIENCE, INTEGER)
                .constraints(
                        constraint("pk_rpkit_crafting_experience").primaryKey(RPKIT_CRAFTING_EXPERIENCE.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.7.0")
        }
    }

    override fun insert(entity: RPKCraftingExperienceValue): Int {
        database.create.insertInto(
                        RPKIT_CRAFTING_EXPERIENCE,
                        RPKIT_CRAFTING_EXPERIENCE.CHARACTER_ID,
                        RPKIT_CRAFTING_EXPERIENCE.ACTION,
                        RPKIT_CRAFTING_EXPERIENCE.MATERIAL,
                        RPKIT_CRAFTING_EXPERIENCE.EXPERIENCE
                )
                .values(
                        entity.character.id,
                        entity.action.toString(),
                        entity.material.toString(),
                        entity.experience
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache?.put(id, entity)
        return id
    }

    override fun update(entity: RPKCraftingExperienceValue) {
        database.create.update(RPKIT_CRAFTING_EXPERIENCE)
                .set(RPKIT_CRAFTING_EXPERIENCE.CHARACTER_ID, entity.character.id)
                .set(RPKIT_CRAFTING_EXPERIENCE.ACTION, entity.action.toString())
                .set(RPKIT_CRAFTING_EXPERIENCE.MATERIAL, entity.material.toString())
                .set(RPKIT_CRAFTING_EXPERIENCE.EXPERIENCE, entity.experience)
                .where(RPKIT_CRAFTING_EXPERIENCE.ID.eq(entity.id))
                .execute()
        cache?.put(entity.id, entity)
    }

    override fun get(id: Int): RPKCraftingExperienceValue? {
        if (cache?.containsKey(id) == true) {
            return cache[id]
        }
        val result = database.create
                .select(
                        RPKIT_CRAFTING_EXPERIENCE.CHARACTER_ID,
                        RPKIT_CRAFTING_EXPERIENCE.ACTION,
                        RPKIT_CRAFTING_EXPERIENCE.MATERIAL,
                        RPKIT_CRAFTING_EXPERIENCE.EXPERIENCE
                )
                .from(RPKIT_CRAFTING_EXPERIENCE)
                .where(RPKIT_CRAFTING_EXPERIENCE.ID.eq(id))
                .fetchOne() ?: return null
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val characterId = result[RPKIT_CRAFTING_EXPERIENCE.CHARACTER_ID]
        val character = characterProvider.getCharacter(characterId)
        val action = try {
            RPKCraftingAction.valueOf(result[RPKIT_CRAFTING_EXPERIENCE.ACTION])
        } catch (exception: IllegalArgumentException) {
            null
        }
        val material = Material.getMaterial(result[RPKIT_CRAFTING_EXPERIENCE.MATERIAL])
        val experience = result[RPKIT_CRAFTING_EXPERIENCE.EXPERIENCE]
        if (character == null || action == null || material == null) {
            database.create
                    .deleteFrom(RPKIT_CRAFTING_EXPERIENCE)
                    .where(RPKIT_CRAFTING_EXPERIENCE.ID.eq(id))
                    .execute()
            cache?.remove(id)
            return null
        }
        val craftingExperienceValue = RPKCraftingExperienceValue(
                id,
                character,
                action,
                material,
                experience
        )
        cache?.put(id, craftingExperienceValue)
        return craftingExperienceValue
    }

    override fun delete(entity: RPKCraftingExperienceValue) {
        database.create
                .deleteFrom(RPKIT_CRAFTING_EXPERIENCE)
                .where(RPKIT_CRAFTING_EXPERIENCE.ID.eq(entity.id))
                .execute()
        cache?.remove(entity.id)
    }

    fun get(character: RPKCharacter, action: RPKCraftingAction, material: Material): RPKCraftingExperienceValue? {
        val result = database.create
                .select(RPKIT_CRAFTING_EXPERIENCE.ID)
                .from(RPKIT_CRAFTING_EXPERIENCE)
                .where(RPKIT_CRAFTING_EXPERIENCE.CHARACTER_ID.eq(character.id))
                .and(RPKIT_CRAFTING_EXPERIENCE.ACTION.eq(action.toString()))
                .and(RPKIT_CRAFTING_EXPERIENCE.MATERIAL.eq(material.toString()))
                .fetchOne() ?: return null
        return get(result[RPKIT_CRAFTING_EXPERIENCE.ID])
    }

    fun get(character: RPKCharacter): List<RPKCraftingExperienceValue> {
        return database.create
                .select(RPKIT_CRAFTING_EXPERIENCE.ID)
                .from(RPKIT_CRAFTING_EXPERIENCE)
                .where(RPKIT_CRAFTING_EXPERIENCE.CHARACTER_ID.eq(character.id))
                .fetch()
                .mapNotNull { result -> get(result[RPKIT_CRAFTING_EXPERIENCE.ID]) }
    }

}