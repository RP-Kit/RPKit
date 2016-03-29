package com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline

import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent
import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent.Type.POST_PROCESSOR
import com.seventh_root.elysium.api.chat.ChatMessageContext
import java.io.IOException

class BukkitLogChatChannelPipelineComponent : ChatChannelPipelineComponent() {

    override val type: ChatChannelPipelineComponent.Type
        get() = POST_PROCESSOR

    override fun process(message: String, context: ChatMessageContext): String {
        try {
            context.chatChannel.log(message)
        } catch (exception: IOException) {
            exception.printStackTrace()
        }

        return message
    }

}
