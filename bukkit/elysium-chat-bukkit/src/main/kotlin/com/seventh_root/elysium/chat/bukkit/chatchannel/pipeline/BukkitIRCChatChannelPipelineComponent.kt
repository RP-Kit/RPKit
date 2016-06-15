package com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline

import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent
import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent.Type.POST_PROCESSOR
import com.seventh_root.elysium.api.chat.ChatMessageContext
import com.seventh_root.elysium.api.chat.ChatMessagePostProcessContext

class BukkitIRCChatChannelPipelineComponent(var ircChannel: String?, var isWhitelist: Boolean) : ChatChannelPipelineComponent() {

    override val type: ChatChannelPipelineComponent.Type
        get() = POST_PROCESSOR

    override fun process(message: String, context: ChatMessageContext): String {
        return message
    }

    override fun postProcess(message: String, context: ChatMessagePostProcessContext): String {
        return message
    }

}
