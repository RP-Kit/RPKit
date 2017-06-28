package com.rpkit.chat.bukkit.servlet.websocket

import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.profile.RPKProfile


class RPKChatWebSocketProvider : ServiceProvider {

    val sockets = mutableMapOf<Int, ChatWebSocket>()

    fun getWebSocket(profile: RPKProfile): ChatWebSocket? {
        return sockets[profile.id]
    }

    fun registerWebSocket(webSocket: ChatWebSocket) {
        sockets[webSocket.profile.id] = webSocket
    }

    fun unregisterWebSocket(webSocket: ChatWebSocket) {
        sockets.remove(webSocket.profile.id)
    }

}