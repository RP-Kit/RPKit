package com.rpkit.chat.bukkit.chatnamecolor

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.chat.bukkit.database.table.RPKChatNameColorTable

/**
 * Chat name color service implementation
 */
class RPKChatNameColorServiceImpl(override val plugin: RPKChatBukkit) : RPKChatNameColorService {
    
    override fun getChatNameColor(minecraftProfile: RPKMinecraftProfile?): String? {
        val minecraftProfileId = minecraftProfile?.id
        if (minecraftProfileId != null) {
            val chatNameColorRecord = plugin.database.getTable(RPKChatNameColorTable::class.java)[minecraftProfileId].join()
            if (chatNameColorRecord != null) {
                return chatNameColorRecord.chatNameColor
            }
        }
        return null
    }
}