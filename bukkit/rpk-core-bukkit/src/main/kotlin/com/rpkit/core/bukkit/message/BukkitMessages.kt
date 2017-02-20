package com.rpkit.core.bukkit.message

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.message.Messages
import org.bukkit.ChatColor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.util.logging.Level.SEVERE


class BukkitMessages(private val plugin: RPKBukkitPlugin): Messages {

    private val messagesConfigFile = File(plugin.dataFolder, "messages.yml")
    val messagesConfig: FileConfiguration

    init {
        val finalMessagesConfig = YamlConfiguration.loadConfiguration(messagesConfigFile)
        messagesConfig = finalMessagesConfig
        saveDefaultMessagesConfig()
    }

    fun saveMessagesConfig() {
        try {
            messagesConfig.save(messagesConfigFile)
        } catch (exception: IOException) {
            plugin.logger.log(SEVERE, "Could not save messages config to $messagesConfigFile", exception)
        }
    }

    fun saveDefaultMessagesConfig() {
        if (!messagesConfigFile.exists()) {
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }
            messagesConfigFile.createNewFile()
        }
    }

    override fun get(key: String, vars: Map<String, String>): String {
        var message = ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(key))
        vars.forEach { pair ->
            message = message.replace("\$${pair.key}", pair.value)
        }
        return message
    }

    override fun get(key: String): String {
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(key))
    }

    override fun getList(key: String, vars: Map<String, String>): List<String> {
        return messagesConfig.getStringList(key).map { message ->
            var updatedMessage = message
            vars.forEach { pair ->
                updatedMessage = updatedMessage.replace("\$${pair.key}", pair.value)
            }
            ChatColor.translateAlternateColorCodes('&', updatedMessage)
        }
    }

    override fun getList(key: String): List<String> {
        return messagesConfig.getStringList(key).map { ChatColor.translateAlternateColorCodes('&', it) }
    }

    override fun set(key: String, value: String) {
        messagesConfig.set(key, value)
        saveMessagesConfig()
    }

    override fun setDefault(key: String, value: String) {
        if (!messagesConfig.contains(key, true)) {
            messagesConfig.set(key, value)
            saveMessagesConfig()
        }
    }

    override fun setDefault(key: String, value: List<String>) {
        if (!messagesConfig.contains(key, true)) {
            messagesConfig.set(key, value)
            saveMessagesConfig()
        }
    }

}