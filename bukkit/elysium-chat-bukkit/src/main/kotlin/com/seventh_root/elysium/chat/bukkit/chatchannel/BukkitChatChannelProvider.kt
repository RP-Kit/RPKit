package com.seventh_root.elysium.chat.bukkit.chatchannel

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitIRCChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.database.table.BukkitChatChannelTable
import com.seventh_root.elysium.chat.bukkit.database.table.ChatChannelSpeakerTable
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer

class BukkitChatChannelProvider(private val plugin: ElysiumChatBukkit) : ChatChannelProvider<BukkitChatChannel> {

    override val chatChannels: Collection<BukkitChatChannel>
        get() {
            return (plugin.core.database.getTable(BukkitChatChannel::class.java) as BukkitChatChannelTable).getAll()
        }

    override fun getChatChannel(id: Int): BukkitChatChannel? {
        return plugin.core.database.getTable(BukkitChatChannel::class.java)!![id]
    }

    override fun getChatChannel(name: String): BukkitChatChannel? {
        return (plugin.core.database.getTable(BukkitChatChannel::class.java) as BukkitChatChannelTable).get(name)
    }

    override fun addChatChannel(chatChannel: BukkitChatChannel) {
        plugin.core.database.getTable(BukkitChatChannel::class.java)!!.insert(chatChannel)
    }

    override fun removeChatChannel(chatChannel: BukkitChatChannel) {
        plugin.core.database.getTable(BukkitChatChannel::class.java)!!.delete(chatChannel)
    }

    override fun updateChatChannel(chatChannel: BukkitChatChannel) {
        plugin.core.database.getTable(BukkitChatChannel::class.java)!!.update(chatChannel)
    }

    override fun getPlayerChannel(player: ElysiumPlayer): BukkitChatChannel? {
        return (plugin.core.database.getTable(ChatChannelSpeaker::class.java) as? ChatChannelSpeakerTable)?.get(player)?.chatChannel as? BukkitChatChannel
    }

    override fun setPlayerChannel(player: ElysiumPlayer, channel: BukkitChatChannel) {
        val oldChannel = getPlayerChannel(player)
        if (oldChannel != null) {
            oldChannel.removeSpeaker(player)
            updateChatChannel(oldChannel)
        }
        channel.addSpeaker(player)
        updateChatChannel(channel)
    }

    override fun getChatChannelFromIRCChannel(ircChannel: String): BukkitChatChannel? {
        for (channel in chatChannels) {
            val pipeline = channel.pipeline
            for (component in pipeline) {
                if (component is BukkitIRCChatChannelPipelineComponent) {
                    if (component.ircChannel == ircChannel) {
                        return channel
                    }
                }
            }
        }
        return null
    }

}
