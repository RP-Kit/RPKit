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

package com.rpkit.craftingskill.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.craftingskill.bukkit.RPKCraftingSkillBukkit
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingAction
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingExperienceValue
import com.rpkit.craftingskill.bukkit.database.create
import com.rpkit.craftingskill.bukkit.database.jooq.Tables.RPKIT_CRAFTING_EXPERIENCE
import org.bukkit.Material


class RPKCraftingExperienceTable(private val database: Database, private val plugin: RPKCraftingSkillBukkit) : Table {

    fun insert(entity: RPKCraftingExperienceValue) {
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
    }

    fun update(entity: RPKCraftingExperienceValue) {
        database.create.update(RPKIT_CRAFTING_EXPERIENCE)
                .set(RPKIT_CRAFTING_EXPERIENCE.ACTION, entity.action.toString())
                .set(RPKIT_CRAFTING_EXPERIENCE.MATERIAL, entity.material.toString())
                .set(RPKIT_CRAFTING_EXPERIENCE.EXPERIENCE, entity.experience)
                .where(RPKIT_CRAFTING_EXPERIENCE.CHARACTER_ID.eq(entity.character.id))
                .execute()
    }

    operator fun get(character: RPKCharacter, action: RPKCraftingAction, material: Material): RPKCraftingExperienceValue? {
        val result = database.create
                .select(RPKIT_CRAFTING_EXPERIENCE.EXPERIENCE)
                .from(RPKIT_CRAFTING_EXPERIENCE)
                .where(RPKIT_CRAFTING_EXPERIENCE.CHARACTER_ID.eq(character.id))
                .and(RPKIT_CRAFTING_EXPERIENCE.ACTION.eq(action.toString()))
                .and(RPKIT_CRAFTING_EXPERIENCE.MATERIAL.eq(material.toString()))
                .fetchOne() ?: return null
        val experience = result[RPKIT_CRAFTING_EXPERIENCE.EXPERIENCE]
        return RPKCraftingExperienceValue(
                character,
                action,
                material,
                experience
        )
    }

    fun delete(entity: RPKCraftingExperienceValue) {
        database.create
                .deleteFrom(RPKIT_CRAFTING_EXPERIENCE)
                .where(RPKIT_CRAFTING_EXPERIENCE.CHARACTER_ID.eq(entity.character.id))
                .execute()
    }

    fun get(character: RPKCharacter): List<RPKCraftingExperienceValue> {
        return database.create
                .select(
                        RPKIT_CRAFTING_EXPERIENCE.ACTION,
                        RPKIT_CRAFTING_EXPERIENCE.MATERIAL,
                        RPKIT_CRAFTING_EXPERIENCE.EXPERIENCE
                )
                .from(RPKIT_CRAFTING_EXPERIENCE)
                .where(RPKIT_CRAFTING_EXPERIENCE.CHARACTER_ID.eq(character.id))
                .fetch()
                .mapNotNull { result ->
                    val action = result[RPKIT_CRAFTING_EXPERIENCE.ACTION]?.let(RPKCraftingAction::valueOf)
                            ?: return@mapNotNull null
                    val material = result[RPKIT_CRAFTING_EXPERIENCE.MATERIAL]?.let {
                        Material.matchMaterial(it) ?: Material.matchMaterial(it, true)
                    } ?: return@mapNotNull null
                    val experience = result[RPKIT_CRAFTING_EXPERIENCE.EXPERIENCE]
                    RPKCraftingExperienceValue(
                            character,
                            action,
                            material,
                            experience
                    )
                }
    }

}