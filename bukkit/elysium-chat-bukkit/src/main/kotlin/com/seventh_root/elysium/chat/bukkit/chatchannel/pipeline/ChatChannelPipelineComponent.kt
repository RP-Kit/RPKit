/*
 * Copyright 2016 Ross Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline

import com.seventh_root.elysium.chat.bukkit.context.ChatMessageContext
import com.seventh_root.elysium.chat.bukkit.context.ChatMessagePostProcessContext
import com.seventh_root.elysium.chat.bukkit.exception.ChatChannelMessageFormattingFailureException

abstract class ChatChannelPipelineComponent: Comparable<ChatChannelPipelineComponent> {

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
