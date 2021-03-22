/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.chat.bukkit.chatchannel.pipeline

import com.rpkit.chat.bukkit.context.DirectedPreFormatMessageContext
import java.util.concurrent.CompletableFuture

/**
 * A directed chat channel pipeline component.
 * This is used in the directed pipeline for a channel, for messages that are directed towards a particular individual.
 * This means [process] is called for each recipient of the message, and the contents of the final message may be
 * different for each recipient.
 * Use cases include for message formatting for individuals, and for garbling text based on the distance of the
 * recipient from the sender.
 */
interface DirectedPreFormatPipelineComponent {

    /**
     * Processes a message with the given context.
     * This will be called once per recipient of a message.
     *
     * @param context The message context
     * @return The message context, after modifications performed by the component
     */
    fun process(context: DirectedPreFormatMessageContext): CompletableFuture<DirectedPreFormatMessageContext>

}