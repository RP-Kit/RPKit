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

package com.rpkit.professions.bukkit

import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldProvider
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.professions.bukkit.character.ProfessionField
import com.rpkit.professions.bukkit.command.profession.ProfessionCommand
import com.rpkit.professions.bukkit.database.table.RPKCharacterProfessionChangeCooldownTable
import com.rpkit.professions.bukkit.database.table.RPKCharacterProfessionExperienceTable
import com.rpkit.professions.bukkit.database.table.RPKCharacterProfessionTable
import com.rpkit.professions.bukkit.database.table.RPKProfessionHiddenTable
import com.rpkit.professions.bukkit.listener.*
import com.rpkit.professions.bukkit.profession.RPKProfessionProviderImpl
import org.bstats.bukkit.Metrics


class RPKProfessionsBukkit: RPKBukkitPlugin() {

    private var professionFieldInitialised = false

    override fun onEnable() {
        Metrics(this, 5352)
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKProfessionProviderImpl(this)
        )
    }

    override fun onPostEnable() {
        attemptCharacterCardFieldInitialisation()
    }

    override fun registerCommands() {
        getCommand("profession")?.setExecutor(ProfessionCommand(this))
    }

    override fun registerListeners() {
        registerListeners(
                PluginEnableListener(this),
                BlockBreakListener(this),
                CraftItemListener(this),
                InventoryClickListener(this),
                PrepareItemCraftListener(this),
                RPKBukkitCharacterDeleteListener(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKCharacterProfessionChangeCooldownTable(database, this))
        database.addTable(RPKCharacterProfessionExperienceTable(database, this))
        database.addTable(RPKCharacterProfessionTable(database, this))
        database.addTable(RPKProfessionHiddenTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("profession-usage", "&cUsage: /profession [list|set|unset|view|experience]")
        messages.setDefault("profession-experience-usage", "&cUsage: /profession experience [add|remove|set|view]")
        messages.setDefault("no-permission-profession-list", "&cYou do not have permission to list professions.")
        messages.setDefault("profession-list-title", "&fProfessions:")
        messages.setDefault("profession-list-item", "&7- &f\$profession")
        messages.setDefault("no-permission-profession-set", "&cYou do not have permission to set profession.")
        messages.setDefault("profession-set-invalid-player-not-online", "&cThat player is not online.")
        messages.setDefault("profession-set-invalid-player-please-specify-from-console", "&cPlease specify which player when using this command from console.")
        messages.setDefault("profession-set-usage", "&cUsage: /profession set (player) [profession]")
        messages.setDefault("no-minecraft-profile-self", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-minecraft-profile-other", "&c\$player does not have a Minecraft profile.")
        messages.setDefault("no-character-self", "&cYou do not currently have an active character. Please create one with /character new, or switch to an old one using /character switch.")
        messages.setDefault("no-character-other", "&c\$player does not currently have an active character.")
        messages.setDefault("profession-set-invalid-profession", "&cThat is not a valid profession. Please look at the list of available professions with /profession list.")
        messages.setDefault("profession-set-invalid-already-using-profession", "&cYou are already practicing that profession.")
        messages.setDefault("profession-set-invalid-too-many-professions", "&cYou have too many professions to choose another.")
        messages.setDefault("profession-set-valid", "&aProfession set to \$profession.")
        messages.setDefault("no-permission-profession-unset", "&cYou do not have permission to unset profession.")
        messages.setDefault("profession-unset-invalid-player-not-online", "&cThat player is not online.")
        messages.setDefault("profession-unset-invalid-player-please-specify-from-console", "&cPlease specify which player when using this command from console.")
        messages.setDefault("profession-unset-usage", "&cUsage: /profession unset (player) [profession]")
        messages.setDefault("profession-unset-invalid-profession", "&cThat is not a valid profession. Please look at the list of available professions with /profession list.")
        messages.setDefault("profession-unset-invalid-not-using-profession", "&cYou are not currently practicing that profession.")
        messages.setDefault("profession-unset-invalid-on-cooldown", "&cYou must wait \$cooldown-days days, \$cooldown-hours hours, \$cooldown-minutes minutes and \$cooldown-seconds seconds before unsetting your profession.")
        messages.setDefault("profession-unset-valid", "&aProfession \$profession unset.")
        messages.setDefault("no-permission-profession-view", "&cYou do not have permission to view profession.")
        messages.setDefault("profession-view-invalid-player-not-online", "&cThat player is not online.")
        messages.setDefault("profession-view-invalid-player-please-specify-from-console", "&cPlease specify which player when using this command from console.")
        messages.setDefault("profession-view-valid-title", "&fProfession levels:")
        messages.setDefault("profession-view-valid-item", "&7- &7Lv&e\$level &f\$profession &7(&e\$experience&7/&e\$next-level-experience&7)")
        messages.setDefault("no-permission-profession-experience-add", "&cYou do not have permission to add profession experience.")
        messages.setDefault("profession-experience-add-invalid-player-not-online", "&cThat player is not online.")
        messages.setDefault("profession-experience-add-invalid-player-please-specify-from-console", "&cPlease specify which player when using this command from console.")
        messages.setDefault("profession-experience-add-usage", "&cUsage: /profession experience add (player) [profession] [experience]")
        messages.setDefault("profession-experience-add-invalid-exp-not-a-number", "&cExperience must be an integer value.")
        messages.setDefault("profession-experience-add-invalid-profession", "&cThat is not a valid profession. Please look at the list of available professions with /profession list.")
        messages.setDefault("profession-experience-add-valid", "&a\$player/\$character received \$received-experience in \$profession &7(total: \$total-experience)")
        messages.setDefault("no-permission-profession-experience-remove", "&cYou do not have permission to remove profession experience.")
        messages.setDefault("profession-experience-remove-invalid-player-not-online", "&cThat player is not online.")
        messages.setDefault("profession-experience-remove-invalid-player-please-specify-from-console", "&cPlease specify which player when using this command from console.")
        messages.setDefault("profession-experience-remove-usage", "&cUsage: /profession experience remove (player) [profession] [experience]")
        messages.setDefault("profession-experience-remove-invalid-exp-not-a-number", "&cExperience must be an integer value.")
        messages.setDefault("profession-experience-remove-invalid-profession", "&cThat is not a valid profession. Please look at the list of available professions with /profession list.")
        messages.setDefault("profession-experience-remove-valid", "&a\$player/\$character lost \$removed-experience in \$profession &7(total: \$total-experience)")
        messages.setDefault("no-permission-profession-experience-set", "&cYou do not have permission to set profession experience.")
        messages.setDefault("profession-experience-set-invalid-player-not-online", "&cThat player is not online.")
        messages.setDefault("profession-experience-set-invalid-player-please-specify-from-console", "&cPlease specify which player when using this command from console.")
        messages.setDefault("profession-experience-set-usage", "&cUsage: /profession experience set (player) [profession] [experience]")
        messages.setDefault("profession-experience-set-invalid-exp-not-a-number", "&cExperience must be an integer value.")
        messages.setDefault("profession-experience-set-invalid-profession", "&cThat is not a valid profession. Please look at the list of available professions with /profession list.")
        messages.setDefault("profession-experience-set-valid", "&a\$player/\$character had their experience in \$profession set to \$total-experience.")
        messages.setDefault("no-permission-profession-experience-view", "&cYou do not have permission to view profession experience.")
        messages.setDefault("profession-experience-view-invalid-player-not-online", "&cThat player is not online.")
        messages.setDefault("profession-experience-view-invalid-player-please-specify-from-console", "&cPlease specify which player when using this command from console.")
        messages.setDefault("profession-experience-view-usage", "&cUsage: /profession experience view (player) [profession]")
        messages.setDefault("profession-experience-view-invalid-profession", "&cThat is not a valid profession. Please look at the list of available professions with /profession list.")
        messages.setDefault("profession-experience-view-valid", "&aProfession experience in &f\$profession&a: &e\$experience&7/&e\$next-level-experience &7(Level \$level, total experience \$total-experience)")
        messages.setDefault("mine-experience", "&aMining experience gained: &e\$received-experience &ain \$profession &7(Lv&e\$level&7, &e\$experience&7/&e\$next-level-experience&7exp)")
        messages.setDefault("craft-experience", "&aCrafting experience gained: &e\$received-experience &ain \$profession &7(Lv&e\$level&7, &e\$experience&7/&e\$next-level-experience&7exp)")
        messages.setDefault("smelt-experience", "&aSmelting experience gained: &e\$received-experience &ain \$profession &7(Lv&e\$level&7, &e\$experience&7/&e\$next-level-experience&7exp)")
    }

    fun attemptCharacterCardFieldInitialisation() {
        if (!professionFieldInitialised) {
            try {
                core.serviceManager.getServiceProvider(RPKCharacterCardFieldProvider::class)
                        .characterCardFields.add(ProfessionField(this))
                professionFieldInitialised = true
            } catch (ignore: UnregisteredServiceException) {}
        }
    }

}