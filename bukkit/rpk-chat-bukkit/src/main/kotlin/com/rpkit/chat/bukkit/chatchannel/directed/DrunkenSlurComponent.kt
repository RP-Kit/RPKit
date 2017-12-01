package com.rpkit.chat.bukkit.chatchannel.directed

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedChatChannelPipelineComponent
import com.rpkit.chat.bukkit.context.DirectedChatChannelMessageContext
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.drink.bukkit.drink.RPKDrinkProvider
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.logging.Level

@SerializableAs("DrunkenSlurComponent")
class DrunkenSlurComponent(private val plugin: RPKChatBukkit, val drunkenness: Int): DirectedChatChannelPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedChatChannelMessageContext): DirectedChatChannelMessageContext {
        val minecraftProfile = context.senderMinecraftProfile ?: return context
        try {
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val drinkProvider = plugin.core.serviceManager.getServiceProvider(RPKDrinkProvider::class)
            val character = characterProvider.getActiveCharacter(minecraftProfile) ?: return context
            if (drinkProvider.getDrunkenness(character) >= drunkenness) {
                context.message = context.message.replace(Regex("s([^h])"), "sh$1")
            }
        } catch (exception: UnregisteredServiceException) {
            plugin.logger.log(Level.SEVERE, "Failed to retrieve drink provider. Is a plugin with drinks functionality installed?", exception)
        }
        return context
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
                Pair("drunkenness", drunkenness)
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): DrunkenSlurComponent {
            return DrunkenSlurComponent(
                    Bukkit.getPluginManager().getPlugin("rpk-chat-bukkit") as RPKChatBukkit,
                    serialized["drunkenness"] as Int
            )
        }
    }

}