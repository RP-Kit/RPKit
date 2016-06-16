package com.seventh_root.elysium.chat.bukkit.context

import com.seventh_root.elysium.chat.bukkit.context.ChatMessagePostProcessContext
import com.seventh_root.elysium.chat.bukkit.chatchannel.ElysiumChatChannel
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer


class BukkitChatMessagePostProcessContext(
        override val chatChannel: ElysiumChatChannel,
        override val sender: ElysiumPlayer
) : ChatMessagePostProcessContext