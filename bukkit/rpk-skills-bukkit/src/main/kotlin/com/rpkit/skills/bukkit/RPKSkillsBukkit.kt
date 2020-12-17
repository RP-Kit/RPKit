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

package com.rpkit.skills.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.service.Services
import com.rpkit.skills.bukkit.command.BindSkillCommand
import com.rpkit.skills.bukkit.command.SkillCommand
import com.rpkit.skills.bukkit.command.UnbindSkillCommand
import com.rpkit.skills.bukkit.database.table.RPKSkillBindingTable
import com.rpkit.skills.bukkit.database.table.RPKSkillCooldownTable
import com.rpkit.skills.bukkit.listener.PlayerInteractListener
import com.rpkit.skills.bukkit.skills.RPKSkillService
import com.rpkit.skills.bukkit.skills.RPKSkillServiceImpl
import com.rpkit.skills.bukkit.skills.RPKSkillTypeService
import com.rpkit.skills.bukkit.skills.RPKSkillTypeServiceImpl
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File


class RPKSkillsBukkit : RPKBukkitPlugin() {

    lateinit var database: Database

    override fun onEnable() {
        System.setProperty("com.rpkit.skills.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4417)
        saveDefaultConfig()

        val databaseConfigFile = File(dataFolder, "database.yml")
        if (!databaseConfigFile.exists()) {
            saveResource("database.yml", false)
        }
        val databaseConfig = YamlConfiguration.loadConfiguration(databaseConfigFile)
        val databaseUrl = databaseConfig.getString("database.url")
        if (databaseUrl == null) {
            logger.severe("Database URL not set!")
            isEnabled = false
            return
        }
        val databaseUsername = databaseConfig.getString("database.username")
        val databasePassword = databaseConfig.getString("database.password")
        val databaseSqlDialect = databaseConfig.getString("database.dialect")
        val databaseMaximumPoolSize = databaseConfig.getInt("database.maximum-pool-size", 3)
        val databaseMinimumIdle = databaseConfig.getInt("database.minimum-idle", 3)
        if (databaseSqlDialect == null) {
            logger.severe("Database SQL dialect not set!")
            isEnabled = false
            return
        }
        database = Database(
                DatabaseConnectionProperties(
                        databaseUrl,
                        databaseUsername,
                        databasePassword,
                        databaseSqlDialect,
                        databaseMaximumPoolSize,
                        databaseMinimumIdle
                ),
                DatabaseMigrationProperties(
                        when (databaseSqlDialect) {
                            "MYSQL" -> "com/rpkit/skills/migrations/mysql"
                            "SQLITE" -> "com/rpkit/skills/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_skills"
                ),
                classLoader
        )

        database.addTable(RPKSkillCooldownTable(database, this))
        database.addTable(RPKSkillBindingTable(database, this))

        Services[RPKSkillTypeService::class.java] = RPKSkillTypeServiceImpl(this)
        Services[RPKSkillService::class.java] = RPKSkillServiceImpl(this)
    }

    override fun registerCommands() {
        getCommand("skill")?.setExecutor(SkillCommand(this))
        getCommand("bindskill")?.setExecutor(BindSkillCommand(this))
        getCommand("unbindskill")?.setExecutor(UnbindSkillCommand(this))
    }

    override fun registerListeners() {
        registerListeners(
                PlayerInteractListener(this)
        )
    }

    override fun setDefaultMessages() {
        messages.setDefault("skill-valid", "&aUsed \$skill.")
        messages.setDefault("skill-invalid-on-cooldown", "&c\$skill is on cooldown for \$cooldown seconds.")
        messages.setDefault("skill-invalid-not-enough-mana", "&c\$skill requires \$mana-cost mana, you have \$mana/\$max-mana")
        messages.setDefault("skill-invalid-unmet-prerequisites", "&cYou do not meet the prerequisites for \$skill.")
        messages.setDefault("skill-invalid-skill", "&cThere is no skill by that name.")
        messages.setDefault("skill-list-title", "&fSkills: ")
        messages.setDefault("skill-list-item", "&f- &7\$skill")
        messages.setDefault("bind-skill-usage", "&cUsage: /bindskill [skill]")
        messages.setDefault("bind-skill-invalid-skill", "&cInvalid skill.")
        messages.setDefault("bind-skill-invalid-binding-already-exists", "&cA binding already exists for that item.")
        messages.setDefault("bind-skill-valid", "&aBound \$skill to \$item.")
        messages.setDefault("unbind-skill-invalid-no-binding", "&cNo skill was bound to that item.")
        messages.setDefault("unbind-skill-valid", "&aUnbound \$skill from \$item.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
        messages.setDefault("no-character", "&cYou need a character to perform that command.")
        messages.setDefault("not-from-console", "&cYou must be a player to perform that command.")
        messages.setDefault("no-permission-skill", "&cYou do not have permission to use skills.")
        messages.setDefault("no-permission-bind-skill", "&cYou do not have permission to bind skills.")
        messages.setDefault("no-permission-unbind-skill", "&cYou do not have permission to unbind skills.")
        messages.setDefault("no-skill-service", "&cThere is no skill service available.")
        messages.setDefault("no-minecraft-profile-service", "&cThere is no Minecraft profile service available.")
        messages.setDefault("no-character-service", "&cThere is no character service available.")
        messages.setDefault("no-skill-service", "&cThere is no skill service available.")
    }

}