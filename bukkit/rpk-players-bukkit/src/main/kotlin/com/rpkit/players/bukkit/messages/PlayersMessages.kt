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

package com.rpkit.players.bukkit.messages

import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKProfile

class PlayersMessages(plugin: RPKPlayersBukkit) : BukkitMessages(plugin) {

    class ProfileSetNameValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(name: String) = message.withParameters("name" to name)
    }

    class ProfileLinkRequestMessage(private val message: ParameterizedMessage) {
        fun withParameters(profile: RPKProfile) = message.withParameters("profile" to profile.name)
    }

    class ProfileViewValidMessage(private val message: List<ParameterizedMessage>) {
        fun withParameters(name: String, discriminator: Int) =
            message.map {
                it.withParameters(
                    "name" to name,
                    "discriminator" to discriminator.toString()
                )
            }.toTypedArray()
    }

    val profileLinkUsage = get("profile-link-usage")
    val profileLinkDiscordUsage = get("profile-link-discord-usage")
    val profileLinkDiscordInvalidUserTag = get("profile-link-discord-invalid-user-tag")
    val profileLinkDiscordInvalidUser = get("profile-link-discord-invalid-user")
    val profileLinkDiscordValid = get("profile-link-discord-valid")
    val profileLinkIrcUsage = get("profile-link-irc-usage")
    val profileLinkIrcInvalidAlreadyLinked = get("profile-link-irc-invalid-already-linked")
    val profileLinkIrcInvalidNick = get("profile-link-irc-invalid-nick")
    val profileLinkIrcValid = get("profile-link-irc-valid")
    val profileLinkMinecraftUsage = get("profile-link-minecraft-usage")
    val profileLinkMinecraftInvalidMinecraftProfile = get("profile-link-minecraft-invalid-minecraft-profile")
    val profileLinkMinecraftValid = get("profile-link-minecraft-valid")
    val profileConfirmLinkUsage = get("profile-confirm-link-usage")
    val profileConfirmLinkInvalidId = get("profile-confirm-link-invalid-id")
    val profileConfirmLinkInvalidAlreadyLinked = get("profile-confirm-link-invalid-already-linked")
    val profileConfirmLinkInvalidRequest = get("profile-confirm-link-invalid-request")
    val profileConfirmLinkValid = get("profile-confirm-link-valid")
    val profileConfirmLinkInvalidType = get("profile-confirm-link-invalid-type")
    val profileDenyLinkUsage = get("profile-deny-link-usage")
    val profileDenyLinkInvalidId = get("profile-deny-link-invalid-id")
    val profileDenyLinkInvalidRequest = get("profile-deny-link-invalid-request")
    val profileDenyLinkValid = get("profile-deny-link-valid")
    val profileDenyLinkProfileCreated = get("profile-deny-link-profile-created")
    val profileDenyLinkInvalidType = get("profile-deny-link-invalid-type")
    val profileViewInvalidTarget = get("profile-view-invalid-target")
    val profileViewValid = getParameterizedList("profile-view-valid").let(::ProfileViewValidMessage)
    val profileSetNameUsage = get("profile-set-name-usage")
    val profileSetNameInvalidName = get("profile-set-name-invalid-name")
    val profileSetNameValid = getParameterized("profile-set-name-valid").let(::ProfileSetNameValidMessage)
    val profileSetPasswordUsage = get("profile-set-password-usage")
    val profileSetPasswordValid = get("profile-set-password-valid")
    val profileSetUsage = get("profile-set-usage")
    val profileUsage = get("profile-usage")
    val noProfileSelf = get("no-profile-self")
    val noProfileOther = get("no-profile-other")
    val noMinecraftProfileSelf = get("no-minecraft-profile-self")
    val profileLinkRequest = getParameterized("profile-link-request").let(::ProfileLinkRequestMessage)
    val yes = get("yes")
    val no = get("no")
    val noPermissionProfileLink = get("no-permission-profile-link")
    val noPermissionProfileLinkDiscord = get("no-permission-profile-link-discord")
    val noPermissionProfileLinkIrc = get("no-permission-profile-link-irc")
    val noPermissionProfileLinkMinecraft = get("no-permission-profile-link-minecraft")
    val noPermissionProfileViewSelf = get("no-permission-profile-view-self")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noIrcService = get("no-irc-service")
    val noIrcProfileService = get("no-irc-profile-service")
    val noProfileService = get("no-profile-service")
    val noDiscordService = get("no-discord-service")
    val noDiscordProfileService = get("no-discord-profile-service")
    val notFromConsole = get("not-from-console")
}