package com.seventh_root.elysium.chat.bukkit.chatchannel

import com.seventh_root.elysium.core.database.TableRow
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer


class ChatChannelSpeaker(
        override var id: Int = 0,
        val chatChannel: ElysiumChatChannel,
        val player: ElysiumPlayer
): TableRow