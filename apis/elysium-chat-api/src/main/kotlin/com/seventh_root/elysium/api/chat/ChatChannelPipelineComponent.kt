package com.seventh_root.elysium.api.chat

import com.seventh_root.elysium.api.chat.exception.ChatChannelMessageFormattingFailureException

abstract class ChatChannelPipelineComponent : Comparable<ChatChannelPipelineComponent> {

    enum class Type private constructor(val priority: Int) {

        PRE_PROCESSOR(0),
        FORMATTER(1),
        POST_PROCESSOR(2)

    }

    override fun compareTo(other: ChatChannelPipelineComponent): Int {
        return type.priority - other.type.priority
    }

    abstract val type: Type

    @Throws(ChatChannelMessageFormattingFailureException::class)
    abstract fun process(message: String, context: ChatMessageContext): String?

    @Throws(ChatChannelMessageFormattingFailureException::class)
    abstract fun postProcess(message: String, context: ChatMessagePostProcessContext): String?

}
