/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.monsters.bukkit

import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.database.Database
import com.rpkit.core.database.DatabaseConnectionProperties
import com.rpkit.core.database.DatabaseMigrationProperties
import com.rpkit.core.database.UnsupportedDatabaseDialectException
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import com.rpkit.monsters.bukkit.command.monsterspawnarea.MonsterSpawnAreaCommand
import com.rpkit.monsters.bukkit.database.table.RPKMonsterSpawnAreaMonsterTable
import com.rpkit.monsters.bukkit.database.table.RPKMonsterSpawnAreaTable
import com.rpkit.monsters.bukkit.listener.CreatureSpawnListener
import com.rpkit.monsters.bukkit.listener.EntityDamageByEntityListener
import com.rpkit.monsters.bukkit.listener.EntityDamageListener
import com.rpkit.monsters.bukkit.listener.EntityDeathListener
import com.rpkit.monsters.bukkit.messages.MonstersMessages
import com.rpkit.monsters.bukkit.monsterexperience.RPKMonsterExperienceService
import com.rpkit.monsters.bukkit.monsterexperience.RPKMonsterExperienceServiceImpl
import com.rpkit.monsters.bukkit.monsterlevel.RPKMonsterLevelService
import com.rpkit.monsters.bukkit.monsterlevel.RPKMonsterLevelServiceImpl
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaService
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaServiceImpl
import com.rpkit.monsters.bukkit.monsterstat.RPKMonsterStatService
import com.rpkit.monsters.bukkit.monsterstat.RPKMonsterStatServiceImpl
import org.bstats.bukkit.Metrics
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File


class RPKMonstersBukkit : JavaPlugin(), RPKPlugin {

    lateinit var database: Database
    lateinit var messages: MonstersMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.monsters.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.monsters.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 6661)
        saveDefaultConfig()

        messages = MonstersMessages(this)

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
                            "MYSQL" -> "com/rpkit/monsters/migrations/mysql"
                            "SQLITE" -> "com/rpkit/monsters/migrations/sqlite"
                            else -> throw UnsupportedDatabaseDialectException("Unsupported database dialect $databaseSqlDialect")
                        },
                        "flyway_schema_history_monsters"
                ),
                classLoader
        )
        database.addTable(RPKMonsterSpawnAreaMonsterTable(database, this))
        database.addTable(RPKMonsterSpawnAreaTable(database, this))

        Services[RPKMonsterExperienceService::class.java] = RPKMonsterExperienceServiceImpl(this)
        Services[RPKMonsterLevelService::class.java] = RPKMonsterLevelServiceImpl(this)
        val monsterSpawnAreaService = RPKMonsterSpawnAreaServiceImpl(this)
        monsterSpawnAreaService.loadSpawnAreas()
        Services[RPKMonsterSpawnAreaService::class.java] = monsterSpawnAreaService
        val monsterStatService = RPKMonsterStatServiceImpl(this)
        Services[RPKMonsterStatService::class.java] = monsterStatService
        Services[RPKMonsterStatServiceImpl::class.java] = monsterStatService

        registerCommands()
        registerListeners()
    }

    fun registerCommands() {
        getCommand("monsterspawnarea")?.setExecutor(MonsterSpawnAreaCommand(this))
    }

    fun registerListeners() {
        registerListeners(
                CreatureSpawnListener(this),
                EntityDamageByEntityListener(this),
                EntityDamageListener(this),
                EntityDeathListener(this)
        )
    }

}