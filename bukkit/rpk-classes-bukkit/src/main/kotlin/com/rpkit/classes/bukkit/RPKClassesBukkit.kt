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
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.stats.bukkit.stat.RPKStatVariable
import com.rpkit.stats.bukkit.stat.RPKStatVariableProvider


class RPKClassesBukkit: RPKBukkitPlugin() {

    private var statsInitialized = false
    private var characterCardFieldsInitialized = false

    override fun onEnable() {
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKClassProviderImpl(this)
        )
    }

    override fun onPostEnable() {
        attemptStatRegistration()
        attemptCharacterCardFieldRegistration()
    }

    override fun registerCommands() {
        getCommand("class").executor = ClassCommand(this)
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
                            .map { clazz ->
                                Pair(clazz, classProvider.getLevel(character, clazz))
                            }
                            .toMap()
                }

            })
            statVariableProvider.addStatVariable(object: RPKStatVariable {

                override val name = "clazz"

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