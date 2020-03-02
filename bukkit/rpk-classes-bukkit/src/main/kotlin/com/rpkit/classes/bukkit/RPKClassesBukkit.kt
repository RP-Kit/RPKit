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

package com.rpkit.classes.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldProvider
import com.rpkit.classes.bukkit.character.ClassField
import com.rpkit.classes.bukkit.classes.RPKClass
import com.rpkit.classes.bukkit.classes.RPKClassProvider
import com.rpkit.classes.bukkit.classes.RPKClassProviderImpl
import com.rpkit.classes.bukkit.command.`class`.ClassCommand
import com.rpkit.classes.bukkit.database.table.RPKCharacterClassTable
import com.rpkit.classes.bukkit.database.table.RPKClassExperienceTable
import com.rpkit.classes.bukkit.listener.PluginEnableListener
import com.rpkit.classes.bukkit.skillpoint.RPKSkillPointProviderImpl
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.stats.bukkit.stat.RPKStatVariable
import com.rpkit.stats.bukkit.stat.RPKStatVariableProvider
import org.bstats.bukkit.Metrics


class RPKClassesBukkit: RPKBukkitPlugin() {

    private var statsInitialized = false
    private var characterCardFieldsInitialized = false

    override fun onEnable() {
        Metrics(this, 4386)
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKClassProviderImpl(this),
                RPKSkillPointProviderImpl(this)
        )
    }

    override fun onPostEnable() {
        attemptStatRegistration()
        attemptCharacterCardFieldRegistration()
    }

    override fun registerCommands() {
        getCommand("class")?.setExecutor(ClassCommand(this))
    }

    override fun registerListeners() {
        registerListeners(
                PluginEnableListener(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKCharacterClassTable(database, this))
        database.addTable(RPKClassExperienceTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("class-usage", "&cUsage: /class [set|list]")
        messages.setDefault("no-permission-class-set", "&cYou do not have permission to set your class.")
        messages.setDefault("class-set-usage", "&cUsage: /class set [class]")
        messages.setDefault("not-from-console", "&cYou must be a player to perform this command.")
        messages.setDefault("no-character", "&cYou require a character to perform that command.")
        messages.setDefault("class-set-invalid-class", "&cThat class is invalid.")
        messages.setDefault("class-set-invalid-prerequisites", "&cYou do not have the prerequisites for that class.")
        messages.setDefault("class-set-valid", "&aClass set to \$class.")
        messages.setDefault("no-permission-class-list", "&cYou do not have permission to list classes.")
        messages.setDefault("class-list-title", "&fClasses:")
        messages.setDefault("class-list-item", "&f- &7\$class")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
    }

    fun attemptStatRegistration() {
        if (statsInitialized) return
        try {
            val statVariableProvider = core.serviceManager.getServiceProvider(RPKStatVariableProvider::class)
            val classProvider = core.serviceManager.getServiceProvider(RPKClassProvider::class)
            statVariableProvider.addStatVariable(object: RPKStatVariable {

                override val name = "classLevels"

                override fun get(character: RPKCharacter): Map<RPKClass, Int>? {
                    return classProvider.classes
                            .map { `class` ->
                                Pair(`class`, classProvider.getLevel(character, `class`))
                            }
                            .toMap()
                }

            })
            statVariableProvider.addStatVariable(object: RPKStatVariable {

                override val name = "class"

                override fun get(character: RPKCharacter): Any? {
                    return classProvider.getClass(character)
                }

            })
            statsInitialized = true
        } catch (ignore: UnregisteredServiceException) {}
    }

    fun attemptCharacterCardFieldRegistration() {
        if (characterCardFieldsInitialized) return
        try {
            val characterCardFieldProvider = core.serviceManager.getServiceProvider(RPKCharacterCardFieldProvider::class)
            characterCardFieldProvider.characterCardFields.add(ClassField(this))
            characterCardFieldsInitialized = true
        } catch (ignore: UnregisteredServiceException) {}
    }

}