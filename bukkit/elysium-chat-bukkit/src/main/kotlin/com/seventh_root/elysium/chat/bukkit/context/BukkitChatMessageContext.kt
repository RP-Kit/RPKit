package com.seventh_root.elysium.chat.bukkit.context

import com.seventh_root.elysium.api.chat.ChatMessageContext
import com.seventh_root.elysium.api.chat.ElysiumChatChannel
import com.seventh_root.elysium.api.player.ElysiumPlayer

class BukkitChatMessageContext(override val chatChannel: ElysiumChatChannel, override val sender: ElysiumPlayer, override val receiver: ElysiumPlayer) : ChatMessageContext
