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

import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.ChatChannelPipelineComponent.Type.POST_PROCESSOR
import com.seventh_root.elysium.chat.bukkit.context.ChatMessageContext
import com.seventh_root.elysium.chat.bukkit.context.ChatMessagePostProcessContext
import java.io.IOException

class LogChatChannelPipelineComponent: ChatChannelPipelineComponent() {

    override val type: ChatChannelPipelineComponent.Type
        get() = POST_PROCESSOR

    override fun process(message: String, context: ChatMessageContext): String {
        return message
    }

    override fun postProcess(message: String, context: ChatMessagePostProcessContext): String {
        try {
            context.chatChannel.log(message)
        } catch (exception: IOException) {
            exception.printStackTrace()
        }
        return message
    }

}
