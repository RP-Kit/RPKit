package com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline

import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.ChatChannelPipelineComponent.Type.POST_PROCESSOR
import com.seventh_root.elysium.chat.bukkit.context.ChatMessageContext
import com.seventh_root.elysium.chat.bukkit.context.ChatMessagePostProcessContext

class IRCChatChannelPipelineComponent(var ircChannel: String?, var isWhitelist: Boolean): ChatChannelPipelineComponent() {

    override val type: ChatChannelPipelineComponent.Type
        get() = POST_PROCESSOR

    override fun process(message: String, context: ChatMessageContext): String {
        return message
    }

    override fun postProcess(message: String, context: ChatMessagePostProcessContext): String {
        return message
    }

}
