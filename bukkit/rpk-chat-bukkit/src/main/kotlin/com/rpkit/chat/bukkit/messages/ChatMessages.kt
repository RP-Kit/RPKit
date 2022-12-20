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

package com.rpkit.chat.bukkit.messages

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.chat.bukkit.chatgroup.RPKChatGroup
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile

class ChatMessages(plugin: RPKChatBukkit) : BukkitMessages(plugin) {

    class ChatChannelValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(chatChannel: RPKChatChannel) = message.withParameters(
            "channel" to chatChannel.name.value
        )
    }

    class MuteValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(chatChannel: RPKChatChannel) = message.withParameters(
            "channel" to chatChannel.name.value
        )
    }

    class IrcListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(minecraftProfile: RPKMinecraftProfile) = message.withParameters(
            "player" to minecraftProfile.name
        )
    }

    class ChatGroupCreateValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(chatGroup: RPKChatGroup) = message.withParameters(
            "group" to chatGroup.name.value
        )
    }

    class ChatGroupDisbandValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(chatGroup: RPKChatGroup) = message.withParameters(
            "group" to chatGroup.name.value
        )
    }

    class ChatGroupInviteReceivedMessage(private val message: ParameterizedMessage) {
        fun withParameters(chatGroup: RPKChatGroup) = message.withParameters(
            "group" to chatGroup.name.value
        )
    }

    class ChatGroupInviteValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(minecraftProfile: RPKMinecraftProfile, chatGroup: RPKChatGroup) = message.withParameters(
            "player" to minecraftProfile.name,
            "group" to chatGroup.name.value
        )
    }

    class ChatGroupJoinReceivedMessage(private val message: ParameterizedMessage) {
        fun withParameters(minecraftProfile: RPKMinecraftProfile, chatGroup: RPKChatGroup) = message.withParameters(
            "player" to minecraftProfile.name,
            "group" to chatGroup.name.value
        )
    }

    class ChatGroupJoinValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(chatGroup: RPKChatGroup) = message.withParameters(
            "group" to chatGroup.name.value
        )
    }

    class ChatGroupLeaveValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(chatGroup: RPKChatGroup) = message.withParameters(
            "group" to chatGroup.name.value
        )
    }

    class ChatGroupMembersListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(minecraftProfile: RPKMinecraftProfile) = message.withParameters(
            "player" to minecraftProfile.name
        )
    }

    class ChatGroupInvitationsListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(minecraftProfile: RPKMinecraftProfile) = message.withParameters(
            "player" to minecraftProfile.name
        )
    }

    class NoPermissionChatChannelMessage(private val message: ParameterizedMessage) {
        fun withParameters(chatChannel: RPKChatChannel) = message.withParameters(
            "channel" to chatChannel.name.value
        )
    }

    class NoPermissionMuteMessage(private val message: ParameterizedMessage) {
        fun withParameters(chatChannel: RPKChatChannel) = message.withParameters(
            "channel" to chatChannel.name.value
        )
    }

    class NoPermissionUnmuteMessage(private val message: ParameterizedMessage) {
        fun withParameters(chatChannel: RPKChatChannel) = message.withParameters(
            "channel" to chatChannel.name.value
        )
    }

    class CommandSnoopMessage(private val message: ParameterizedMessage) {
        fun withParameters(sender: RPKMinecraftProfile, command: String) = message.withParameters(
            "sender_player" to sender.name,
            "command" to command
        )
    }

    class AccountLinkDiscordSuccessfulMessage(private val message: ParameterizedMessage) {
        fun withParameters(discordTag: String) = message.withParameters(
            "discord_tag" to discordTag
        )
    }

    class PluginVersionMessage(private val message: ParameterizedMessage) {
        fun withParameters(name: String, version: String) = message.withParameters(
            "name" to name,
            "version" to version
        )
    }

    class PluginDescriptionMessage(private val message: ParameterizedMessage) {
        fun withParameters(description: String) = message.withParameters(
            "description" to description
        )
    }

    class PluginWebsiteMessage(private val message: ParameterizedMessage) {
        fun withParameters(website: String) = message.withParameters(
            "website" to website
        )
    }

    class PluginAuthorMessage(private val message: ParameterizedMessage) {
        fun withParameters(author: String) = message.withParameters(
            "author" to author
        )
    }

    class PluginAuthorsMessage(private val message: ParameterizedMessage) {
        fun withParameters(authors: List<String>) = message.withParameters(
            "authors" to authors.joinToString()
        )
    }

    class PluginContributorsMessage(private val message: ParameterizedMessage) {
        fun withParameters(contributors: List<String>) = message.withParameters(
            "contributors" to contributors.joinToString()
        )
    }

    class ServerVersionMessage(private val message: ParameterizedMessage) {
        fun withParameters(name: String, version: String, apiVersion: String) = message.withParameters(
            "name" to name,
            "version" to version,
            "api_version" to apiVersion
        )
    }

    val chatChannelValid = getParameterized("chatchannel-valid").let(::ChatChannelValidMessage)
    val chatChannelInvalidChatChannel = get("chatchannel-invalid-chatchannel")
    val chatChannelUsage = get("chatchannel-usage")
    val muteValid = getParameterized("mute-valid").let(::MuteValidMessage)
    val muteInvalidChatChannel = get("mute-invalid-chatchannel")
    val muteUsage = get("mute-usage")
    val unmuteValid = get("unmute-valid")
    val unmuteInvalidChatChannel = get("unmute-invalid-chatchannel")
    val unmuteUsage = get("unmute-usage")
    val listChatChannelsTitle = get("listchatchannels-title")
    val listChatChannelsItem = get("listchatchannels-item")
    val ircRegisterValid = get("irc-register-valid")
    val ircRegisterInvalidEmailInvalid = get("irc-register-invalid-email-invalid")
    val ircRegisterInvalidEmailNotSpecified = get("irc-register-invalid-email-not-specified")
    val ircVerifyValid = get("irc-verify-valid")
    val ircVerifyInvalidVerificationCodeNotSpecified = get("irc-verify-invalid-verification-code-not-specified")
    val ircQuit = get("irc-quit")
    val ircListTitle = get("irc-list-title")
    val ircListItem = getParameterized("irc-list-item").let(::IrcListItemMessage)
    val snoopUsage = get("snoop-usage")
    val snoopEnabled = get("snoop-enabled")
    val snoopAlreadyEnabled = get("snoop-already-enabled")
    val snoopDisabled = get("snoop-disabled")
    val snoopAlreadyDisabled = get("snoop-already-disabled")
    val snoopCheckOn = get("snoop-check-on")
    val snoopCheckOff = get("snoop-check-off")
    val chatGroupCreateValid = getParameterized("chat-group-create-valid").let(::ChatGroupCreateValidMessage)
    val chatGroupCreateInvalidReserved = get("chat-group-create-invalid-reserved")
    val chatGroupCreateInvalidTaken = get("chat-group-create-invalid-taken")
    val chatGroupCreateUsage = get("chat-group-create-usage")
    val chatGroupDisbandValid = getParameterized("chat-group-disband-valid").let(::ChatGroupDisbandValidMessage)
    val chatGroupDisbandInvalidNonexistent = get("chat-group-disband-invalid-nonexistent")
    val chatGroupDisbandInvalidNotAMember = get("chat-group-disband-invalid-not-a-member")
    val chatGroupDisbandUsage = get("chat-group-disband-usage")
    val chatGroupInviteReceived = getParameterized("chat-group-invite-received").let(::ChatGroupInviteReceivedMessage)
    val chatGroupInviteValid = getParameterized("chat-group-invite-valid").let(::ChatGroupInviteValidMessage)
    val chatGroupInviteInvalidPlayer = get("chat-group-invite-invalid-player")
    val chatGroupInviteInvalidNotAMember = get("chat-group-invite-invalid-not-a-member")
    val chatGroupInviteInvalidChatGroup = get("chat-group-invite-invalid-chat-group")
    val chatGroupInviteUsage = get("chat-group-invite-usage")
    val chatGroupJoinReceived = getParameterized("chat-group-join-received").let(::ChatGroupJoinReceivedMessage)
    val chatGroupJoinValid = getParameterized("chat-group-join-valid").let(::ChatGroupJoinValidMessage)
    val chatGroupJoinInvalidNoInvite = get("chat-group-join-invalid-no-invite")
    val chatGroupJoinInvalidChatGroup = get("chat-group-join-invalid-chat-group")
    val chatGroupJoinUsage = get("chat-group-join-usage")
    val chatGroupLeaveValid = getParameterized("chat-group-leave-valid").let(::ChatGroupLeaveValidMessage)
    val chatGroupLeaveInvalidNotAMember = get("chat-group-leave-invalid-not-a-member")
    val chatGroupLeaveInvalidChatGroup = get("chat-group-leave-invalid-chat-group")
    val chatGroupLeaveUsage = get("chat-group-leave-usage")
    val chatGroupMessageInvalidNotAMember = get("chat-group-message-invalid-not-a-member")
    val chatGroupMessageInvalidChatGroup = get("chat-group-message-invalid-chat-group")
    val chatGroupMessageUsage = get("chat-group-message-usage")
    val chatGroupMembersListTitle = get("chat-group-members-list-title")
    val chatGroupMembersListItem = getParameterized("chat-group-members-list-item").let(::ChatGroupMembersListItemMessage)
    val chatGroupInvitationsListTitle = get("chat-group-invitations-list-title")
    val chatGroupInvitationsListItem = getParameterized("chat-group-invitations-list-item").let(::ChatGroupInvitationsListItemMessage)
    val chatGroupMembersInvalidChatGroup = get("chat-group-members-invalid-chat-group")
    val chatGroupMembersUsage = get("chat-group-members-usage")
    val chatGroupUsage = get("chat-group-usage")
    val replyUsage = get("reply-usage")
    val replyInvalidChatGroup = get("reply-invalid-chat-group")
    val messageInvalidTarget = get("message-invalid-target")
    val messageInvalidSelf = get("message-invalid-self")
    val messageUsage = get("message-usage")
    val notFromConsole = get("not-from-console")
    val noCharacter = get("no-character")
    val noMinecraftProfile = get("no-minecraft-profile")
    val noChatChannel = get("no-chat-channel")
    val noPermissionChatChannel = getParameterized("no-permission-chatchannel").let(::NoPermissionChatChannelMessage)
    val noPermissionListchatChannels = get("no-permission-listchatchannels")
    val noPermissionMute = getParameterized("no-permission-mute").let(::NoPermissionMuteMessage)
    val noPermissionUnmute = getParameterized("no-permission-unmute").let(::NoPermissionUnmuteMessage)
    val noPermissionSnoopOn = get("no-permission-snoop-on")
    val noPermissionSnoopOff = get("no-permission-snoop-off")
    val noPermissionSnoopCheck = get("no-permission-snoop-check")
    val noPermissionChatGroup = get("no-permission-chat-group")
    val noPermissionChatGroupCreate = get("no-permission-chat-group-create")
    val noPermissionChatGroupDisband = get("no-permission-chat-group-disband")
    val noPermissionChatGroupInvite = get("no-permission-chat-group-invite")
    val noPermissionChatGroupJoin = get("no-permission-chat-group-join")
    val noPermissionChatGroupLeave = get("no-permission-chat-group-leave")
    val noPermissionChatGroupMembers = get("no-permission-chat-group-members")
    val noPermissionChatGroupMessage = get("no-permission-chat-group-message")
    val noPermissionMessage = get("no-permission-message")
    val noPermissionReply = get("no-permission-reply")
    val commandSnoop = getParameterized("command-snoop").let(::CommandSnoopMessage)
    val accountLinkDiscordSuccessful = getParameterized("account-link-discord-successful").let(::AccountLinkDiscordSuccessfulMessage)
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noChatChannelService = get("no-chat-channel-service")
    val noChatGroupService = get("no-chat-group-service")
    val noSnooperService = get("no-snooper-service")
    val ircNoIrcService = get("irc-no-irc-service")
    val operationCancelled = get("operation-cancelled")
    val continueWriting = get("continue-writing")
    val versionInvalidPlugin = get("version-invalid-plugin")
    val pluginVersion = getParameterized("plugin-version").let(::PluginVersionMessage)
    val pluginDescription = getParameterized("plugin-description").let(::PluginDescriptionMessage)
    val pluginWebsite = getParameterized("plugin-website").let(::PluginWebsiteMessage)
    val pluginAuthor = getParameterized("plugin-author").let(::PluginAuthorMessage)
    val pluginAuthors = getParameterized("plugin-authors").let(::PluginAuthorsMessage)
    val pluginContributors = getParameterized("plugin-contributors").let(::PluginContributorsMessage)
    val serverVersion = getParameterized("server-version").let(::ServerVersionMessage)
}