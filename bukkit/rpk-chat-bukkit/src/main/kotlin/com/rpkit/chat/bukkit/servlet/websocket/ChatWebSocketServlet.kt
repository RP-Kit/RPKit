package com.rpkit.chat.bukkit.servlet.websocket

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.eclipse.jetty.websocket.servlet.WebSocketServlet
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory

class ChatWebSocketServlet(private val plugin: RPKChatBukkit): WebSocketServlet() {
    override fun configure(factory: WebSocketServletFactory) {
        factory.policy.idleTimeout = 1800000
        factory.setCreator({ req, resp ->
            val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
            val profile = profileProvider.getActiveProfile(req.httpServletRequest)
            if (profile != null) {
                val socketProvider = plugin.core.serviceManager.getServiceProvider(RPKChatWebSocketProvider::class)
                val socket = ChatWebSocket(plugin, profile)
                socketProvider.registerWebSocket(socket)
                return@setCreator socket
            }
            return@setCreator null
        })
    }
}