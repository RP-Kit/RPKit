package com.rpkit.chat.bukkit.servlet.websocket

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelProvider
import com.rpkit.permissions.bukkit.group.RPKGroupProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketListener


class ChatWebSocket(private val plugin: RPKChatBukkit, val profile: RPKProfile): WebSocketListener {

    var session: Session? = null

    override fun onWebSocketError(cause: Throwable) {

    }

    override fun onWebSocketClose(statusCode: Int, reason: String) {
        plugin.logger.info("${profile.name} disconnected from chat.")
        val webSocketProvider = plugin.core.serviceManager.getServiceProvider(RPKChatWebSocketProvider::class)
        webSocketProvider.unregisterWebSocket(this)
    }

    override fun onWebSocketConnect(session: Session) {
        this.session = session
        plugin.logger.info("${profile.name} connected to chat.")
    }

    override fun onWebSocketText(message: String) {
        val chatChannelProvider = plugin.core.serviceManager.getServiceProvider(RPKChatChannelProvider::class)
        val messageParts = message.split(":::")
        val chatChannel = chatChannelProvider.getChatChannel(messageParts[0]) ?: return
        val groupProvider = plugin.core.serviceManager.getServiceProvider(RPKGroupProvider::class)
        if (!groupProvider.hasPermission(profile, "rpkit.chat.command.chatchannel.${chatChannel.name}")) {
            session?.remote?.sendStringByFuture("${chatChannel.name}:::You do not have permission to speak in this channel.")
            return
        }
        chatChannel.sendMessage(profile, null, messageParts[1])
    }

    override fun onWebSocketBinary(payload: ByteArray, offset: Int, len: Int) {

    }
}