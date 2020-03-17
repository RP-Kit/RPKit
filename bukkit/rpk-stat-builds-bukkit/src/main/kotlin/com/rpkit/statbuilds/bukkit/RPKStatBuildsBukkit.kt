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

package com.rpkit.statbuilds.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.statbuilds.bukkit.command.statattribute.StatAttributeCommand
import com.rpkit.statbuilds.bukkit.command.statbuild.StatBuildCommand
import com.rpkit.statbuilds.bukkit.database.table.RPKCharacterStatPointsTable
import com.rpkit.statbuilds.bukkit.listener.RPKServiceProviderReadyListener
import com.rpkit.statbuilds.bukkit.skillpoint.RPKSkillPointProviderImpl
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeProvider
import com.rpkit.statbuilds.bukkit.statattribute.RPKStatAttributeProviderImpl
import com.rpkit.statbuilds.bukkit.statbuild.RPKStatBuildProvider
import com.rpkit.statbuilds.bukkit.statbuild.RPKStatBuildProviderImpl
import com.rpkit.stats.bukkit.stat.RPKStatVariable
import com.rpkit.stats.bukkit.stat.RPKStatVariableProvider
import org.bstats.bukkit.Metrics

class RPKStatBuildsBukkit: RPKBukkitPlugin() {

    private var statsInitialized = false

    override fun onEnable() {
        Metrics(this, 6663)
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKStatAttributeProviderImpl(this),
                RPKStatBuildProviderImpl(this),
                RPKSkillPointProviderImpl(this)
        )
    }

    override fun onPostEnable() {
        attemptStatRegistration()
    }

    override fun registerCommands() {
        getCommand("statbuild")?.setExecutor(StatBuildCommand(this))
        getCommand("statattribute")?.setExecutor(StatAttributeCommand(this))
    }

    override fun registerListeners() {
        registerListeners(
                RPKServiceProviderReadyListener(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKCharacterStatPointsTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("no-permission-stat-build-assign-point", "&cYou do not have permission to assign stat points.")
        messages.setDefault("not-from-console", "&cYou must be a player to perform that command.")
        messages.setDefault("stat-build-assign-point-usage", "&cUsage: /statbuild assignpoint [stat] (points)")
        messages.setDefault("stat-build-assign-point-invalid-stat-attribute", "&cInvalid stat attribute. Use /statattribute list to list stat attributes.")
        messages.setDefault("stat-build-assign-point-invalid-points-integer", "&cThe amount of points to assign must be an integer.")
        messages.setDefault("stat-build-assign-point-invalid-points-not-enough", "&cYou do not have enough points to assign.")
        messages.setDefault("stat-build-assign-point-invalid-points-too-many-in-stat", "&cYou may not assign any more stat points to that stat right now.")
        messages.setDefault("no-minecraft-profile-self", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-character-self", "&cYou do not currently have an active character. Please create one with /character new, or switch to an old one using /character switch.")
        messages.setDefault("stat-build-assign-point-valid", "&a\$points point(s) assigned to \$stat-attribute (total \$total-points, max \$max-points)")
        messages.setDefault("stat-build-usage", "&cUsage: /statbuild [view|assignpoint]")
        messages.setDefault("no-permission-stat-build-view", "&cYou do not have permission to view your stat build.")
        messages.setDefault("stat-build-view-title", "&fStat build:")
        messages.setDefault("stat-build-view-points-assignment-count", "&fTotal stat points: &7\$total&f, Unassigned: &7\$unassigned&f, Assigned: &7\$assigned")
        messages.setDefault("stat-build-view-item", "&f\$stat-attribute &e\$points&7/&f\$max-points")
        messages.setDefault("stat-attribute-usage", "&cUsage: /statattribute [list]")
        messages.setDefault("no-permission-stat-attribute-list", "&cYou do not have permission to list stat attributes.")
        messages.setDefault("stat-attribute-list-title", "&fStat attributes:")
        messages.setDefault("stat-attribute-list-item", "&7 - &f\$stat-attribute")
    }

    fun attemptStatRegistration() {
        if (statsInitialized) return
        try {
            val statVariableProvider = core.serviceManager.getServiceProvider(RPKStatVariableProvider::class)
            val statAttributeProvider = core.serviceManager.getServiceProvider(RPKStatAttributeProvider::class)
            val statBuildProvider = core.serviceManager.getServiceProvider(RPKStatBuildProvider::class)
            statAttributeProvider.statAttributes.forEach { statAttribute ->
                statVariableProvider.addStatVariable(object: RPKStatVariable {
                    override val name = statAttribute.name
                    override fun get(character: RPKCharacter): Any? {
                        return statBuildProvider.getStatPoints(character, statAttribute)
                    }
                })
            }
        } catch (ignore: UnregisteredServiceException) {}
    }

}