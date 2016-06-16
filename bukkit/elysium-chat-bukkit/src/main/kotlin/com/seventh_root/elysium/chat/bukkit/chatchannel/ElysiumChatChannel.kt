package com.seventh_root.elysium.chat.bukkit.chatchannel

import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.ChatChannelPipelineComponent
import com.seventh_root.elysium.chat.bukkit.context.ChatMessageContext
import com.seventh_root.elysium.chat.bukkit.context.ChatMessagePostProcessContext
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import com.seventh_root.elysium.core.database.TableRow
import java.awt.Color
import java.io.IOException

interface ElysiumChatChannel : TableRow {

    var name: String
    var color: Color
    var formatString: String
    var radius: Int
    var clearRadius: Int
    val speakers: Collection<ElysiumPlayer>
    fun addSpeaker(speaker: ElysiumPlayer)
    fun removeSpeaker(speaker: ElysiumPlayer)
    val listeners: Collection<ElysiumPlayer>
    fun addListener(listener: ElysiumPlayer)
    fun removeListener(listener: ElysiumPlayer)
    val pipeline: List<ChatChannelPipelineComponent>
    fun processMessage(message: String?, context: ChatMessageContext): String?
    fun postProcess(message: String?, context: ChatMessagePostProcessContext)
    var matchPattern: String
    var isIRCEnabled: Boolean
    var ircChannel: String
    var isIRCWhitelist: Boolean
    @Throws(IOException::class)
    fun log(message: String)
    var isJoinedByDefault: Boolean

}
