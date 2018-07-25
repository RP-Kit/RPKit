package com.rpkit.chat.bukkit.chatchannel.undirected

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.pipeline.UndirectedChatChannelPipelineComponent
import com.rpkit.chat.bukkit.context.UndirectedChatChannelMessageContext
import com.rpkit.chat.bukkit.servlet.websocket.RPKChatWebSocketProvider
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.configuration.serialization.ConfigurationSerializable


class WebComponent(private val plugin: RPKChatBukkit): UndirectedChatChannelPipelineComponent, ConfigurationSerializable {
    override fun process(context: UndirectedChatChannelMessageContext): UndirectedChatChannelMessageContext {
        if (context.isCancelled) return context
        plugin.core.serviceManager.getServiceProvider(RPKChatWebSocketProvider::class).sockets
                .filter { socket -> socket.value.session?.isOpen == true }
                .forEach { socket -> socket.value.session?.remote
                        ?.sendStringByFuture("${context.chatChannel.name}:::${ChatColor.stripColor(context.message)}") }
        return context
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf()
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): WebComponent {
            return WebComponent(
                    Bukkit.getPluginManager().getPlugin("rpk-chat-bukkit") as RPKChatBukkit
            )
        }
    }
}