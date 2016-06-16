package com.seventh_root.elysium.chat.bukkit.context

import com.seventh_root.elysium.chat.bukkit.context.ChatMessageContext
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannel
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer

class BukkitChatMessageContext(
        override val chatChannel: ElysiumChatChannel,
        override val sender: ElysiumPlayer,
        override val receiver: ElysiumPlayer
) : ChatMessageContext
