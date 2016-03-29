package com.seventh_root.elysium.api.chat

import com.seventh_root.elysium.api.player.ElysiumPlayer
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
    var matchPattern: String
    var isIRCEnabled: Boolean
    var ircChannel: String?
    var isIRCWhitelist: Boolean
    @Throws(IOException::class)
    fun log(message: String)
    var isJoinedByDefault: Boolean

}
