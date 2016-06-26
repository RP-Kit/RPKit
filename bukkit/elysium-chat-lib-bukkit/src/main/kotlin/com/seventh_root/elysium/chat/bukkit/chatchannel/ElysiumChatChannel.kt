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

package com.seventh_root.elysium.chat.bukkit.chatchannel

import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.ChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.context.ChatMessageContext
import com.seventh_root.elysium.chat.bukkit.context.ChatMessagePostProcessContext
import com.seventh_root.elysium.core.database.Entity
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import java.awt.Color


interface ElysiumChatChannel: Entity {
    var name: String
    var color: Color
    var formatString: String
    var radius: Int
    var clearRadius: Int
    val speakers: List<ElysiumPlayer>
    val listeners: List<ElysiumPlayer>
    val pipeline: MutableList<ChatChannelPipelineComponent>
    var matchPattern: String
    var isIRCEnabled: Boolean
    var ircChannel: String
    var isIRCWhitelist: Boolean
    var isJoinedByDefault: Boolean
    fun addSpeaker(speaker: ElysiumPlayer)
    fun removeSpeaker(speaker: ElysiumPlayer)
    fun addListener(listener: ElysiumPlayer)
    fun removeListener(listener: ElysiumPlayer)
    fun processMessage(message: String?, context: ChatMessageContext): String?
    fun postProcess(message: String?, context: ChatMessagePostProcessContext): String?
    fun log(message: String)
}