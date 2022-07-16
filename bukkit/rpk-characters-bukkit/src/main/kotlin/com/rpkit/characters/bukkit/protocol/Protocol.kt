/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.characters.bukkit.protocol

import com.comphenix.protocol.PacketType.Play.Server.*
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction.ADD_PLAYER
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerInfoAction.REMOVE_PLAYER
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedGameProfile
import com.rpkit.characters.bukkit.character.RPKCharacter
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.chat.ComponentSerializer
import org.bukkit.entity.Player

fun reloadPlayer(player: Player, character: RPKCharacter, viewers: List<Player>) {
    reloadPlayerInfo(player, character, viewers)
    reloadPlayerEntity(player, viewers)
}

private fun reloadPlayerInfo(player: Player, character: RPKCharacter, recipients: List<Player>) {
    sendRemovePlayerPacket(player, recipients)
    sendAddPlayerPacket(player, character, recipients)
}

private fun reloadPlayerEntity(player: Player, recipients: List<Player>) {
    sendRemoveEntityPacket(player, recipients)
    sendAddEntityPacket(player, recipients)
}

private fun sendRemovePlayerPacket(player: Player, recipients: List<Player>) {
    val packet = createRemovePlayerPacket(player)
    recipients.forEach { onlinePlayer ->
        ProtocolLibrary.getProtocolManager().sendServerPacket(onlinePlayer, packet)
    }
}

private fun createRemovePlayerPacket(player: Player): PacketContainer {
    val packet = ProtocolLibrary.getProtocolManager().createPacket(PLAYER_INFO)
    packet.playerInfoAction.write(0, REMOVE_PLAYER)
    val profile = WrappedGameProfile.fromPlayer(player)
    val chatComponent = WrappedChatComponent.fromText(profile.name)
    val playerInfoData =
        PlayerInfoData(profile, 0, EnumWrappers.NativeGameMode.fromBukkit(player.gameMode), chatComponent)
    packet.playerInfoDataLists.write(0, listOf(playerInfoData))
    return packet
}

private fun sendAddPlayerPacket(player: Player, character: RPKCharacter, recipients: List<Player>) {
    val packet = createAddPlayerPacket(player, character)
    recipients.forEach { onlinePlayer ->
        ProtocolLibrary.getProtocolManager().sendServerPacket(onlinePlayer, packet)
    }
}

private fun createAddPlayerPacket(
    player: Player,
    character: RPKCharacter
): PacketContainer {
    val packet = ProtocolLibrary.getProtocolManager().createPacket(PLAYER_INFO)
    packet.playerInfoAction.write(0, ADD_PLAYER)
    val profile = WrappedGameProfile.fromPlayer(player).withName(character.name.take(16))
    val tabListTextComponent =
        WrappedChatComponent.fromJson(ComponentSerializer.toString(TextComponent(player.playerListName)))
    val playerInfoData = PlayerInfoData(
        profile,
        0,
        EnumWrappers.NativeGameMode.fromBukkit(player.gameMode),
        tabListTextComponent
    )
    packet.playerInfoDataLists.write(0, listOf(playerInfoData))
    return packet
}

private fun sendRemoveEntityPacket(player: Player, recipients: List<Player>) {
    val packet = createRemoveEntityPacket(player)
    recipients.forEach { onlinePlayer ->
        ProtocolLibrary.getProtocolManager().sendServerPacket(onlinePlayer, packet)
    }
}

private fun createRemoveEntityPacket(player: Player): PacketContainer {
    val packet = ProtocolLibrary.getProtocolManager().createPacket(ENTITY_DESTROY)
    packet.intLists.write(0, listOf(player.entityId))
    return packet
}

private fun sendAddEntityPacket(player: Player, recipients: List<Player>) {
    val packet = createAddEntityPacket(player)
    recipients.forEach { onlinePlayer ->
        ProtocolLibrary.getProtocolManager().sendServerPacket(onlinePlayer, packet)
    }
}

private fun createAddEntityPacket(player: Player): PacketContainer {
    val packet = ProtocolLibrary.getProtocolManager().createPacket(NAMED_ENTITY_SPAWN)
    packet.integers.write(0, player.entityId)
    packet.uuiDs.write(0, player.uniqueId)
    packet.doubles.write(0, player.location.x)
    packet.doubles.write(1, player.location.y)
    packet.doubles.write(2, player.location.z)
    val yawByte = (player.location.yaw / 360f * 256f).toInt().toByte()
    val pitchByte = (player.location.pitch / 360f * 256f).toInt().toByte()
    packet.bytes.write(0, yawByte)
    packet.bytes.write(1, pitchByte)
    return packet
}
