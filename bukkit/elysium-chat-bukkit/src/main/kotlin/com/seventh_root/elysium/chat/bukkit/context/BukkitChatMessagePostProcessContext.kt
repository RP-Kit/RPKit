package com.seventh_root.elysium.chat.bukkit.context

import com.seventh_root.elysium.api.chat.ChatMessagePostProcessContext
import com.seventh_root.elysium.api.chat.ElysiumChatChannel
import com.seventh_root.elysium.api.player.ElysiumPlayer


class BukkitChatMessagePostProcessContext(
        override val chatChannel: ElysiumChatChannel,
        override val sender: ElysiumPlayer
) : ChatMessagePostProcessContext