/*
 * Copyright 2016 Ross Binden
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

package com.seventh_root.elysium.chat.bukkit.chatchannel

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.database.table.ChatChannelSpeakerTable
import com.seventh_root.elysium.chat.bukkit.database.table.ElysiumChatChannelTable
import com.seventh_root.elysium.chat.bukkit.irc.ElysiumIRCProvider
import com.seventh_root.elysium.core.service.ServiceProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer

class ElysiumChatChannelProvider(private val plugin: ElysiumChatBukkit): ServiceProvider {

    val chatChannels: Collection<ElysiumChatChannel>
        get() {
            return (plugin.core.database.getTable(ElysiumChatChannel::class.java) as ElysiumChatChannelTable).getAll()
        }

    fun getChatChannel(id: Int): ElysiumChatChannel? {
        return plugin.core.database.getTable(ElysiumChatChannel::class.java)!![id]
    }

    fun getChatChannel(name: String): ElysiumChatChannel? {
        return (plugin.core.database.getTable(ElysiumChatChannel::class.java) as ElysiumChatChannelTable).get(name)
    }

    fun addChatChannel(chatChannel: ElysiumChatChannel) {
        plugin.core.database.getTable(ElysiumChatChannel::class.java)!!.insert(chatChannel)
        if (chatChannel.isIRCEnabled) {
            val ircProvider = plugin.core.serviceManager.getServiceProvider(ElysiumIRCProvider::class.java)
            ircProvider.ircBot.sendIRC().joinChannel(chatChannel.ircChannel)
        }
    }

    fun removeChatChannel(chatChannel: ElysiumChatChannel) {
        plugin.core.database.getTable(ElysiumChatChannel::class.java)!!.delete(chatChannel)
        if (chatChannel.isIRCEnabled) {
            val ircProvider = plugin.core.serviceManager.getServiceProvider(ElysiumIRCProvider::class.java)
            ircProvider.ircBot.sendRaw().rawLine("PART ${chatChannel.ircChannel}")
        }
    }

    fun updateChatChannel(chatChannel: ElysiumChatChannel) {
        plugin.core.database.getTable(ElysiumChatChannel::class.java)!!.update(chatChannel)
    }

    fun getPlayerChannel(player: ElysiumPlayer): ElysiumChatChannel? {
        return (plugin.core.database.getTable(ChatChannelSpeaker::class.java) as? ChatChannelSpeakerTable)?.get(player)?.chatChannel as? ElysiumChatChannel
    }

    fun setPlayerChannel(player: ElysiumPlayer, channel: ElysiumChatChannel) {
        val oldChannel = getPlayerChannel(player)
        if (oldChannel != null) {
            oldChannel.removeSpeaker(player)
            updateChatChannel(oldChannel)
        }
        channel.addSpeaker(player)
        updateChatChannel(channel)
    }

    fun getChatChannelFromIRCChannel(ircChannel: String): ElysiumChatChannel? {
        return chatChannels.filter { chatChannel -> chatChannel.ircChannel == ircChannel }.firstOrNull()
    }

}
