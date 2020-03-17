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

package com.rpkit.experience.bukkit

import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldProvider
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.experience.bukkit.character.ExperienceField
import com.rpkit.experience.bukkit.character.LevelField
import com.rpkit.experience.bukkit.command.experience.ExperienceCommand
import com.rpkit.experience.bukkit.database.table.RPKExperienceTable
import com.rpkit.experience.bukkit.experience.RPKExperienceProviderImpl
import com.rpkit.experience.bukkit.listener.PlayerExpChangeListener
import com.rpkit.experience.bukkit.listener.PlayerJoinListener
import com.rpkit.experience.bukkit.listener.RPKServiceProviderReadyListener
import org.bstats.bukkit.Metrics


class RPKExperienceBukkit: RPKBukkitPlugin() {

    private var experienceFieldsInitialised = false

    override fun onEnable() {
        Metrics(this, 4393)
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKExperienceProviderImpl(this)
        )
    }

    override fun onPostEnable() {
        attemptCharacterCardFieldInitialisation()
    }

    override fun registerListeners() {
        registerListeners(PlayerExpChangeListener(), PlayerJoinListener(this), RPKServiceProviderReadyListener(this))
    }

    override fun registerCommands() {
        getCommand("experience")?.setExecutor(ExperienceCommand(this))
    }

    override fun createTables(database: Database) {
        database.addTable(RPKExperienceTable(database, this))
    }

    fun attemptCharacterCardFieldInitialisation() {
        if (!experienceFieldsInitialised) {
            try {
                val characterCardFieldProvider = core.serviceManager.getServiceProvider(RPKCharacterCardFieldProvider::class)
                characterCardFieldProvider.characterCardFields.add(ExperienceField(this))
                characterCardFieldProvider.characterCardFields.add(LevelField(this))
                experienceFieldsInitialised = true
            } catch (ignore: UnregisteredServiceException) {}
        }
    }

    override fun setDefaultMessages() {
        messages.setDefault("experience-usage", "&cUsage: /experience [add|set]")
        messages.setDefault("experience-set-usage", "&cUsage: /experience set [player] [value]")
        messages.setDefault("experience-set-experience-invalid-number", "&cYou must specify a number for the amount of experience to set.")
        messages.setDefault("experience-set-player-invalid-player", "&cNo player by that name is online.")
        messages.setDefault("experience-set-valid", "&aExperience set.")
        messages.setDefault("experience-setlevel-usage", "&cUsage: /experience setlevel [player] [value]")
        messages.setDefault("experience-setlevel-level-invalid-number", "&cYou must specify a number for the level to set.")
        messages.setDefault("experience-setlevel-player-invalid-player", "&cNo player by that name is online.")
        messages.setDefault("experience-setlevel-valid", "&aLevel set.")
        messages.setDefault("experience-add-usage", "&cUsage: /experience add [player] [value]")
        messages.setDefault("experience-add-experience-invalid-number", "&cYou must specify a number for the amount of experience to add.")
        messages.setDefault("experience-add-experience-invalid-negative", "&cYou may not add negative experience.")
        messages.setDefault("experience-add-player-invalid-player", "&cNo player by that name is online.")
        messages.setDefault("experience-add-valid", "&aExperience added.")
        messages.setDefault("no-character-other", "&cThat player does not currently have a character.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-permission-experience-set", "&cYou do not have permission to set experience.")
        messages.setDefault("no-permission-experience-setlevel", "&cYou do not have permission to set level.")
        messages.setDefault("no-permission-experience-add", "&cYou do not have permission to add experience.")
    }

}