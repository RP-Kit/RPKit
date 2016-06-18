package com.seventh_root.elysium.chat.bukkit.context

import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannel
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer


class ChatMessagePostProcessContext(
        val chatChannel: ElysiumChatChannel,
        val sender: ElysiumPlayer
)