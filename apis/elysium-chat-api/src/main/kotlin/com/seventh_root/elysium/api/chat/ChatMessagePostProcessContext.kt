package com.seventh_root.elysium.api.chat

import com.seventh_root.elysium.api.player.ElysiumPlayer


interface ChatMessagePostProcessContext {

    val chatChannel: ElysiumChatChannel
    val sender: ElysiumPlayer

}