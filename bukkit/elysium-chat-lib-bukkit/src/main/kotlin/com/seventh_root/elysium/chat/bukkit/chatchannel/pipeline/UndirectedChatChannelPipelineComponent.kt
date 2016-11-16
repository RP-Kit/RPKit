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

import com.seventh_root.elysium.chat.bukkit.context.UndirectedChatChannelMessageContext

/**
 * An undirected chat channel pipeline component.
 * This is used in the undirected pipeline for a channel, for messages that are not directed towards a particular
 * individual.
 * This means [process] is called once for the message, and the contents of the message are the same for each recipient.
 * Use cases include sending messages to an IRC channel, and for writing messages to a log file.
 */
interface UndirectedChatChannelPipelineComponent {

    /**
     * Processes a message with the given context.
     * This will be called once per message.
     *
     * @param context The message context
     * @return The message context, after modifications performed by the component
     */
    fun process(context: UndirectedChatChannelMessageContext): UndirectedChatChannelMessageContext

}