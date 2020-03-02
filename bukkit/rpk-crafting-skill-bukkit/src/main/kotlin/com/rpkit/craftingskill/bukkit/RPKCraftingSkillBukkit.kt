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

package com.rpkit.craftingskill.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.craftingskill.bukkit.command.craftingskill.CraftingSkillCommand
import com.rpkit.craftingskill.bukkit.craftingskill.RPKCraftingSkillProviderImpl
import com.rpkit.craftingskill.bukkit.database.table.RPKCraftingExperienceTable
import com.rpkit.craftingskill.bukkit.listener.*
import org.bstats.bukkit.Metrics


class RPKCraftingSkillBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        Metrics(this, 5350)
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKCraftingSkillProviderImpl(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKCraftingExperienceTable(database, this))
    }

    override fun registerListeners() {
        registerListeners(
                RPKBukkitCharacterDeleteListener(this),
                BlockBreakListener(this),
                CraftItemListener(this),
                PrepareItemCraftListener(this),
                InventoryClickListener(this)
        )
    }

    override fun registerCommands() {
        getCommand("craftingskill")?.setExecutor(CraftingSkillCommand(this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("no-character", "&cYou need to have a character to perform this action.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("not-from-console", "&cYou may not use this command from console.")
        messages.setDefault("craft-experience", "&aCrafting experience gained: &e\$received-experience &7(Total: \$total-experience)")
        messages.setDefault("mine-experience", "&aMining experience gained: &e\$received-experience &7(Total: \$total-experience)")
        messages.setDefault("smelt-experience", "&aSmelting experience gained: &e\$received-experience &7(Total: \$total-experience)")
        messages.setDefault("no-permission-crafting-skill", "&cYou do not have permission to view your crafting skill.")
        messages.setDefault("crafting-skill-usage", "&cUsage: /craftingskill [craft|smelt|mine] [material]")
        messages.setDefault("crafting-skill-actions-title", "&7Actions:")
        messages.setDefault("crafting-skill-actions-item", "&7 - &f\$action")
        messages.setDefault("crafting-skill-invalid-material", "&cInvalid material")
        messages.setDefault("crafting-skill-valid", "&aCrafting skill for &7\$action &a- &7\$material &a- &e\$total-experience/\$max-experience")
    }

}