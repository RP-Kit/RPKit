/*
 * Copyright 2021 Ren Binden
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

package com.rpkit.players.bukkit.test.command.profile

import com.rpkit.chat.bukkit.discord.RPKDiscordService
import com.rpkit.chat.bukkit.irc.RPKIRCService
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.IncorrectUsageFailure
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.result.NoPermissionFailure
import com.rpkit.core.command.sender.RPKConsoleCommandSender
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.service.Services
import com.rpkit.core.service.ServicesDelegate
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.command.profile.ProfileCommand
import com.rpkit.players.bukkit.command.profile.ProfileConfirmLinkCommand
import com.rpkit.players.bukkit.command.profile.ProfileDenyLinkCommand
import com.rpkit.players.bukkit.command.profile.ProfileLinkDiscordCommand
import com.rpkit.players.bukkit.command.profile.ProfileLinkIRCCommand
import com.rpkit.players.bukkit.command.profile.ProfileSetNameCommand
import com.rpkit.players.bukkit.command.profile.ProfileViewCommand
import com.rpkit.players.bukkit.command.result.InvalidTargetMinecraftProfileFailure
import com.rpkit.players.bukkit.command.result.NoProfileOtherFailure
import com.rpkit.players.bukkit.command.result.NoProfileSelfFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.messages.PlayersMessages
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileDiscriminator
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.RPKThinProfile
import com.rpkit.players.bukkit.profile.discord.DiscordUserId
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfile
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfileService
import com.rpkit.players.bukkit.profile.irc.RPKIRCNick
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfile
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileLinkRequest
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftUsername
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beInstanceOf
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify

class ProfileCommandTests : WordSpec({

    "the profile command" should {
        "return incorrect usage when called with no arguments" {
            val profileUsageMessage = "profile usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileUsage } returns profileUsageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, emptyArray()) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(profileUsageMessage) }
        }
        "return incorrect usage when called with an invalid argument" {
            val profileUsageMessage = "profile usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileUsage } returns profileUsageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("invalid")) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(profileUsageMessage) }
        }
        "return missing service failure when view is used without a Minecraft profile service" {
            val noMinecraftProfileServiceMessage = "no minecraft profile service"
            val messages = mockk<PlayersMessages>()
            every { messages.noMinecraftProfileService } returns noMinecraftProfileServiceMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns null
            Services.delegate = testServicesDelegate
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("view")) should beInstanceOf<MissingServiceFailure>()
            verify(exactly = 1) { sender.sendMessage(noMinecraftProfileServiceMessage) }
        }
        "return no permission failure when view is used and sender does not have permission to view own profile" {
            val noPermissionMessage = "no permission"
            val messages = mockk<PlayersMessages>()
            every { messages.noPermissionProfileViewSelf } returns noPermissionMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.hasPermission("rpkit.players.command.profile.view.self") } returns false
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("view")) should beInstanceOf<NoPermissionFailure>()
            verify(exactly = 1) { sender.sendMessage(noPermissionMessage) }
        }
        "return invalid target failure when view is used, sender is console and no player is specified" {
            val invalidTargetMessage = "invalid target"
            val messages = mockk<PlayersMessages>()
            every { messages.profileViewInvalidTarget } returns invalidTargetMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val sender = mockk<RPKConsoleCommandSender>()
            every { sender.hasPermission("rpkit.players.command.profile.view.self") } returns true
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("view")) should beInstanceOf<ProfileViewCommand.InvalidTargetFailure>()
            verify(exactly = 1) { sender.sendMessage(invalidTargetMessage) }
        }
        "return invalid target failure when view is used, sender is console and player is specified but not found" {
            val invalidTargetMessage = "invalid target"
            val messages = mockk<PlayersMessages>()
            every { messages.profileViewInvalidTarget } returns invalidTargetMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            every { minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername("abc")) } returns null
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val sender = mockk<RPKConsoleCommandSender>()
            every { sender.hasPermission("rpkit.players.command.profile.view.self") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.view.other") } returns true
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("view", "abc")) should beInstanceOf<ProfileViewCommand.InvalidTargetFailure>()
            verify(exactly = 1) { sender.sendMessage(invalidTargetMessage) }
        }
        "return no profile (self) failure when view is used and sender does not have a profile" {
            val noProfileSelfMessage = "no profile"
            val messages = mockk<PlayersMessages>()
            every { messages.noProfileSelf } returns noProfileSelfMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            every { minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername("abc")) } returns null
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.hasPermission("rpkit.players.command.profile.view.self") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.view.other") } returns true
            val profile = mockk<RPKThinProfile>()
            every { sender.profile } returns profile
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("view")) should beInstanceOf<NoProfileSelfFailure>()
            verify(exactly = 1) { sender.sendMessage(noProfileSelfMessage) }
        }
        "return no profile (other) failure when view is used and target does not have a profile" {
            val noProfileOtherMessage = "no profile"
            val messages = mockk<PlayersMessages>()
            every { messages.noProfileOther } returns noProfileOtherMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            val target = mockk<RPKMinecraftProfile>()
            val profile = mockk<RPKThinProfile>()
            every { target.profile } returns profile
            every { minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername("abc")) } returns target
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.hasPermission("rpkit.players.command.profile.view.self") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.view.other") } returns true
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("view", "abc")) should beInstanceOf<NoProfileOtherFailure>()
            verify(exactly = 1) { sender.sendMessage(noProfileOtherMessage) }
        }
        "return success when view is used and target has a profile" {
            val profileMessage = PlayersMessages.ProfileViewValidMessage(listOf(
                ParameterizedMessage("name \${name}"),
                ParameterizedMessage("discriminator \${discriminator}")
            ))
            val messages = mockk<PlayersMessages>()
            every { messages.profileViewValid } returns profileMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            val target = mockk<RPKMinecraftProfile>()
            val profile = mockk<RPKProfile>()
            every { profile.name } returns RPKProfileName("abc")
            every { profile.discriminator } returns RPKProfileDiscriminator(1)
            every { target.profile } returns profile
            every { minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername("abc")) } returns target
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.hasPermission("rpkit.players.command.profile.view.self") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.view.other") } returns true
            every { sender.sendMessage(any<Array<String>>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("view", "abc")) shouldBe CommandSuccess
            verify(exactly = 1) { sender.sendMessage(arrayOf("name abc", "discriminator 1")) }

        }
        "return incorrect usage failure when set is used without a second argument" {
            val profileSetUsageMessage = "profile set usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileSetUsage } returns profileSetUsageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set")) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(profileSetUsageMessage) }
        }
        "return incorrect usage failure when set is used with an invalid second argument" {
            val profileSetUsageMessage = "profile set usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileSetUsage } returns profileSetUsageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set", "abcd")) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(profileSetUsageMessage) }
        }
        "return incorrect usage failure when set name is used without specifying a name" {
            val profileSetNameUsageMessage = "profile set name usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileSetNameUsage } returns profileSetNameUsageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set", "name")) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(profileSetNameUsageMessage) }
        }
        "return not a player failure when set name is used from console" {
            val notAPlayerMessage = "not a player"
            val messages = mockk<PlayersMessages>()
            every { messages.notFromConsole } returns notAPlayerMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKConsoleCommandSender>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set", "name", "abcd")) should beInstanceOf<NotAPlayerFailure>()
            verify(exactly = 1) { sender.sendMessage(notAPlayerMessage) }
        }
        "return no profile (self) failure when set name is used and sender does not have a profile" {
            val noProfileSelfMessage = "no profile (self)"
            val messages = mockk<PlayersMessages>()
            every { messages.noProfileSelf } returns noProfileSelfMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profile = mockk<RPKThinProfile>()
            every { sender.profile } returns profile
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set", "name", "abcd")) should beInstanceOf<NoProfileSelfFailure>()
            verify(exactly = 1) { sender.sendMessage(noProfileSelfMessage) }
        }
        "return invalid name failure when set name is used with an invalid name" {
            val invalidNameMessage = "invalid name"
            val messages = mockk<PlayersMessages>()
            every { messages.profileSetNameInvalidName } returns invalidNameMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profile = mockk<RPKProfile>()
            every { sender.profile } returns profile
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set", "name", "&e£kfg~+x:934^^#]")) should beInstanceOf<ProfileSetNameCommand.InvalidNameFailure>()
            verify(exactly = 1) { sender.sendMessage(invalidNameMessage) }
        }
        "return missing service failure when set name is used with no profile service present" {
            val noProfileServiceMessage = "no profile service"
            val messages = mockk<PlayersMessages>()
            every { messages.noProfileService } returns noProfileServiceMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profile = mockk<RPKProfile>()
            every { sender.profile } returns profile
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKProfileService::class.java] } returns null
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set", "name", "abcd")) should beInstanceOf<MissingServiceFailure>()
            verify(exactly = 1) { sender.sendMessage(noProfileServiceMessage) }
        }
        "return success when set name is used with a valid name" {
            val messages = mockk<PlayersMessages>()
            every { messages.profileSetNameValid } returns PlayersMessages.ProfileSetNameValidMessage(
                ParameterizedMessage("name set to \${name}")
            )
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profile = mockk<RPKProfile>()
            every { profile.name = any() } just runs
            every { profile.discriminator = any() } just runs
            every { sender.profile } returns profile
            val profileService = mockk<RPKProfileService>()
            every { profileService.generateDiscriminatorFor(any()) } returns RPKProfileDiscriminator(1)
            every { profileService.updateProfile(any()) } just runs
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKProfileService::class.java] } returns profileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set", "name", "abcd")) should beInstanceOf<CommandSuccess>()
            verify(exactly = 1) { profile.name = RPKProfileName("abcd") }
            verify(exactly = 1) { profileService.updateProfile(profile) }
            verify(exactly = 1) { sender.sendMessage("name set to abcd") }
        }
        "return incorrect usage failure when set password is used without specifying a password" {
            val profileSetPasswordUsageMessage = "profile set password usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileSetPasswordUsage } returns profileSetPasswordUsageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set", "password")) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(profileSetPasswordUsageMessage) }
        }
        "return not a player failure when set password is used from console" {
            val notAPlayerMessage = "not a player"
            val messages = mockk<PlayersMessages>()
            every { messages.notFromConsole } returns notAPlayerMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKConsoleCommandSender>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set", "password", "abc")) should beInstanceOf<NotAPlayerFailure>()
            verify(exactly = 1) { sender.sendMessage(notAPlayerMessage) }
        }
        "return no profile (self) failure when set password is used and sender does not have a profile" {
            val noProfileSelfMessage = "no profile (self)"
            val messages = mockk<PlayersMessages>()
            every { messages.noProfileSelf } returns noProfileSelfMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profile = mockk<RPKThinProfile>()
            every { sender.profile } returns profile
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set", "password", "abcd")) should beInstanceOf<NoProfileSelfFailure>()
            verify(exactly = 1) { sender.sendMessage(noProfileSelfMessage) }
        }
        "return missing service failure when set password is used with no profile service present" {
            val noProfileServiceMessage = "no profile service"
            val messages = mockk<PlayersMessages>()
            every { messages.noProfileService } returns noProfileServiceMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profile = mockk<RPKProfile>()
            every { sender.profile } returns profile
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKProfileService::class.java] } returns null
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set", "password", "abcd")) should beInstanceOf<MissingServiceFailure>()
            verify(exactly = 1) { sender.sendMessage(noProfileServiceMessage) }
        }
        "return success when set password is used with a valid password" {
            val profileSetPasswordValidMessage = "password set"
            val messages = mockk<PlayersMessages>()
            every { messages.profileSetPasswordValid } returns profileSetPasswordValidMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profile = mockk<RPKProfile>()
            every { profile.setPassword(any()) } just runs
            every { sender.profile } returns profile
            val profileService = mockk<RPKProfileService>()
            every { profileService.updateProfile(profile) } just runs
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKProfileService::class.java] } returns profileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("set", "password", "abcd")) should beInstanceOf<CommandSuccess>()
            verify(exactly = 1) { profile.setPassword("abcd".toCharArray()) }
            verify(exactly = 1) { profileService.updateProfile(profile) }
            verify(exactly = 1) { sender.sendMessage(profileSetPasswordValidMessage) }
        }
        "return no permission failure when link is used with no permission" {
            val noPermissionMessage = "no permission"
            val messages = mockk<PlayersMessages>()
            every { messages.noPermissionProfileLink } returns noPermissionMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns false
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link")) should beInstanceOf<NoPermissionFailure>()
            verify(exactly = 1) { sender.sendMessage(noPermissionMessage) }
        }
        "return incorrect usage failure when link is used without specifying account type" {
            val profileLinkUsageMessage = "profile link usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkUsage } returns profileLinkUsageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link")) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(profileLinkUsageMessage) }
        }
        "return incorrect usage failure when link is used with an invalid account type" {
            val profileLinkUsageMessage = "profile link usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkUsage } returns profileLinkUsageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "abcd")) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(profileLinkUsageMessage) }
        }
        "return not a player failure when link irc is used from console" {
            val notAPlayerMessage = "not a player"
            val messages = mockk<PlayersMessages>()
            every { messages.notFromConsole } returns notAPlayerMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKConsoleCommandSender>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "irc", "abcd")) should beInstanceOf<NotAPlayerFailure>()
            verify(exactly = 1) { sender.sendMessage(notAPlayerMessage) }
        }
        "return no permission failure when link irc is used without permission" {
            val noPermissionMessage = "no permission"
            val messages = mockk<PlayersMessages>()
            every { messages.noPermissionProfileLinkIrc } returns noPermissionMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.irc") } returns false
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "irc", "abcd")) should beInstanceOf<NoPermissionFailure>()
            verify(exactly = 1) { sender.sendMessage(noPermissionMessage) }
        }
        "return incorrect usage failure when link irc is used without specifying an IRC nick" {
            val profileLinkIrcUsageMessage = "profile link irc usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkIrcUsage } returns profileLinkIrcUsageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.irc") } returns true
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "irc")) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(profileLinkIrcUsageMessage) }
        }
        "return missing service failure when link irc is used with no IRC service present" {
            val noIrcServiceMessage = "no irc service"
            val messages = mockk<PlayersMessages>()
            every { messages.noIrcService } returns noIrcServiceMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.irc") } returns true
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKIRCService::class.java] } returns null
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "irc", "abcd")) should beInstanceOf<MissingServiceFailure>()
            verify(exactly = 1) { sender.sendMessage(noIrcServiceMessage) }
        }
        "return invalid IRC nick failure when link irc is used without someone with that nick online" {
            val invalidIrcNickMessage = "invalid irc nick"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkIrcInvalidNick } returns invalidIrcNickMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.irc") } returns true
            val ircService = mockk<RPKIRCService>()
            every { ircService.isOnline(any()) } returns false
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKIRCService::class.java] } returns ircService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "irc", "abcd")) should beInstanceOf<ProfileLinkIRCCommand.InvalidIRCNickFailure>()
            verify(exactly = 1) { sender.sendMessage(invalidIrcNickMessage) }
        }
        "return no profile (self) failure when link irc is used and sender does not have a profile" {
            val noProfileSelfMessage = "no profile (self)"
            val messages = mockk<PlayersMessages>()
            every { messages.noProfileSelf } returns noProfileSelfMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.irc") } returns true
            val profile = mockk<RPKThinProfile>()
            every { sender.profile } returns profile
            val ircService = mockk<RPKIRCService>()
            every { ircService.isOnline(any()) } returns true
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKIRCService::class.java] } returns ircService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "irc", "abcd")) should beInstanceOf<NoProfileSelfFailure>()
            verify(exactly = 1) { sender.sendMessage(noProfileSelfMessage) }
        }
        "return missing service failure when link irc is used with no profile service present" {
            val noProfileServiceMessage = "no profile service"
            val messages = mockk<PlayersMessages>()
            every { messages.noProfileService } returns noProfileServiceMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.irc") } returns true
            val profile = mockk<RPKProfile>()
            every { sender.profile } returns profile
            val ircService = mockk<RPKIRCService>()
            every { ircService.isOnline(any()) } returns true
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKIRCService::class.java] } returns ircService
            every { testServicesDelegate[RPKProfileService::class.java] } returns null
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "irc", "abcd")) should beInstanceOf<MissingServiceFailure>()
            verify(exactly = 1) { sender.sendMessage(noProfileServiceMessage) }
        }
        "return missing service failure when link irc is used with no IRC profile service present" {
            val noIrcProfileServiceMessage = "no irc profile service"
            val messages = mockk<PlayersMessages>()
            every { messages.noIrcProfileService } returns noIrcProfileServiceMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.irc") } returns true
            val profile = mockk<RPKProfile>()
            every { sender.profile } returns profile
            val ircService = mockk<RPKIRCService>()
            every { ircService.isOnline(any()) } returns true
            val profileService = mockk<RPKProfileService>()
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKIRCService::class.java] } returns ircService
            every { testServicesDelegate[RPKProfileService::class.java] } returns profileService
            every { testServicesDelegate[RPKIRCProfileService::class.java] } returns null
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "irc", "abcd")) should beInstanceOf<MissingServiceFailure>()
            verify(exactly = 1) { sender.sendMessage(noIrcProfileServiceMessage) }
        }
        "return IRC profile already linked failure when link irc is used with an already-linked IRC profile" {
            val ircProfileAlreadyLinkedMessage = "irc profile already linked"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkIrcInvalidAlreadyLinked } returns ircProfileAlreadyLinkedMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.irc") } returns true
            val profile = mockk<RPKProfile>()
            every { sender.profile } returns profile
            val ircService = mockk<RPKIRCService>()
            val profileService = mockk<RPKProfileService>()
            val ircProfileService = mockk<RPKIRCProfileService>()
            val ircProfile = mockk<RPKIRCProfile>()
            val ircProfileProfile = mockk<RPKProfile>()
            every { ircProfile.profile } returns ircProfileProfile
            every { ircProfileService.getIRCProfile(any<RPKIRCNick>()) } returns ircProfile
            every { ircService.isOnline(any()) } returns true
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKIRCService::class.java] } returns ircService
            every { testServicesDelegate[RPKProfileService::class.java] } returns profileService
            every { testServicesDelegate[RPKIRCProfileService::class.java] } returns ircProfileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "irc", "abcd")) should beInstanceOf<ProfileLinkIRCCommand.IRCProfileAlreadyLinkedFailure>()
            verify(exactly = 1) { sender.sendMessage(ircProfileAlreadyLinkedMessage) }
        }
        "return success when link irc is used with a valid IRC nick and an IRC profile does not exist yet" {
            val profileLinkIrcValidMessage = "irc profile linked"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkIrcValid } returns profileLinkIrcValidMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.irc") } returns true
            val profile = mockk<RPKProfile>()
            every { sender.profile } returns profile
            val ircService = mockk<RPKIRCService>()
            every { ircService.isOnline(any()) } returns true
            val profileService = mockk<RPKProfileService>()
            val thinProfile = mockk<RPKThinProfile>()
            every { profileService.createThinProfile(any()) } returns thinProfile
            val ircProfileService = mockk<RPKIRCProfileService>()
            every { ircProfileService.getIRCProfile(any<RPKIRCNick>()) } returns null
            val ircProfile = mockk<RPKIRCProfile>()
            every { ircProfileService.createIRCProfile(any(), any()) } returns ircProfile
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKIRCService::class.java] } returns ircService
            every { testServicesDelegate[RPKProfileService::class.java] } returns profileService
            every { testServicesDelegate[RPKIRCProfileService::class.java] } returns ircProfileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "irc", "abcd")) should beInstanceOf<CommandSuccess>()
            verify(exactly = 1) { ircProfileService.createIRCProfile(profile, RPKIRCNick("abcd")) }
            verify(exactly = 1) { sender.sendMessage(profileLinkIrcValidMessage) }
        }
        "return success when link irc is used with a valid IRC nick and an IRC profile already exists but has not been linked" {
            val profileLinkIrcValidMessage = "irc profile linked"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkIrcValid } returns profileLinkIrcValidMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.irc") } returns true
            val profile = mockk<RPKProfile>()
            every { sender.profile } returns profile
            val ircService = mockk<RPKIRCService>()
            every { ircService.isOnline(any()) } returns true
            val profileService = mockk<RPKProfileService>()
            val thinProfile = mockk<RPKThinProfile>()
            every { profileService.createThinProfile(any()) } returns thinProfile
            val ircProfileService = mockk<RPKIRCProfileService>()
            every { ircProfileService.updateIRCProfile(any()) } just runs
            val ircProfile = mockk<RPKIRCProfile>()
            every { ircProfile.profile } returns thinProfile
            every { ircProfile.profile = any() } just runs
            every { ircProfileService.getIRCProfile(any<RPKIRCNick>()) } returns ircProfile
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKIRCService::class.java] } returns ircService
            every { testServicesDelegate[RPKProfileService::class.java] } returns profileService
            every { testServicesDelegate[RPKIRCProfileService::class.java] } returns ircProfileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "irc", "abcd")) should beInstanceOf<CommandSuccess>()
            verify(exactly = 1) { ircProfile.profile = profile }
            verify(exactly = 1) { ircProfileService.updateIRCProfile(ircProfile) }
            verify(exactly = 1) { sender.sendMessage(profileLinkIrcValidMessage) }
        }
        "return no permission failure when link minecraft is used with no permission" {
            val noPermissionMessage = "no permission"
            val messages = mockk<PlayersMessages>()
            every { messages.noPermissionProfileLinkMinecraft } returns noPermissionMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.minecraft") } returns false
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "minecraft", "abcd")) should beInstanceOf<NoPermissionFailure>()
            verify(exactly = 1) { sender.sendMessage(noPermissionMessage) }
        }
        "return incorrect usage failure when link minecraft is used without specifying a Minecraft username" {
            val profileLinkMinecraftUsageMessage = "profile link minecraft usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkMinecraftUsage } returns profileLinkMinecraftUsageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.minecraft") } returns true
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "minecraft")) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(profileLinkMinecraftUsageMessage) }
        }
        "return missing service failure when link minecraft is used with no Minecraft profile service present" {
            val noMinecraftProfileServiceMessage = "no minecraft profile service"
            val messages = mockk<PlayersMessages>()
            every { messages.noMinecraftProfileService } returns noMinecraftProfileServiceMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.minecraft") } returns true
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns null
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "minecraft", "abcd")) should beInstanceOf<MissingServiceFailure>()
            verify(exactly = 1) { sender.sendMessage(noMinecraftProfileServiceMessage) }
        }
        "return invalid Minecraft profile failure when link minecraft is used with an already-linked Minecraft profile" {
            val invalidMinecraftProfileMessage = "invalid minecraft profile"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkMinecraftInvalidMinecraftProfile } returns invalidMinecraftProfileMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.minecraft") } returns true
            val target = mockk<RPKMinecraftProfile>()
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            every { minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername("abcd")) } returns target
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "minecraft", "abcd")) should beInstanceOf<InvalidTargetMinecraftProfileFailure>()
            verify(exactly = 1) { sender.sendMessage(invalidMinecraftProfileMessage) }
        }
        "return not a player failure when link minecraft is used from console" {
            val notAPlayerMessage = "not a player"
            val messages = mockk<PlayersMessages>()
            every { messages.notFromConsole } returns notAPlayerMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKConsoleCommandSender>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.minecraft") } returns true
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            every { minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername("abcd")) } returns null
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "minecraft", "abcd")) should beInstanceOf<NotAPlayerFailure>()
            verify(exactly = 1) { sender.sendMessage(notAPlayerMessage) }
        }
        "return no profile (self) failure when link minecraft is used and sender does not have a profile" {
            val noProfileSelfMessage = "no profile (self)"
            val messages = mockk<PlayersMessages>()
            every { messages.noProfileSelf } returns noProfileSelfMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val profile = mockk<RPKThinProfile>()
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.minecraft") } returns true
            every { sender.profile } returns profile
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            every { minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername("abcd")) } returns null
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "minecraft", "abcd")) should beInstanceOf<NoProfileSelfFailure>()
            verify(exactly = 1) { sender.sendMessage(noProfileSelfMessage) }
        }
        "return success and create Minecraft profile link request when link minecraft is used with a valid Minecraft profile" {
            val minecraftProfileLinkedMessage = "minecraft profile linked"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkMinecraftValid } returns minecraftProfileLinkedMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val profile = mockk<RPKProfile>()
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.minecraft") } returns true
            every { sender.profile } returns profile
            val target = mockk<RPKMinecraftProfile>()
            val linkRequest = mockk<RPKMinecraftProfileLinkRequest>()
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            every { minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername("abcd")) } returns null
            every { minecraftProfileService.createMinecraftProfile(RPKMinecraftUsername("abcd")) } returns target
            every { minecraftProfileService.createMinecraftProfileLinkRequest(profile, target) } returns linkRequest
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "minecraft", "abcd")) should beInstanceOf<CommandSuccess>()
            verify(exactly = 1) { sender.sendMessage(minecraftProfileLinkedMessage) }
            verify(exactly = 1) { minecraftProfileService.createMinecraftProfile(RPKMinecraftUsername("abcd")) }
            verify(exactly = 1) { minecraftProfileService.createMinecraftProfileLinkRequest(profile, target) }
        }
        "return not a player failure when link discord is used from console" {
            val notAPlayerMessage = "not a player"
            val messages = mockk<PlayersMessages>()
            every { messages.notFromConsole } returns notAPlayerMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKConsoleCommandSender>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "discord", "abcd#1234")) should beInstanceOf<NotAPlayerFailure>()
            verify(exactly = 1) { sender.sendMessage(notAPlayerMessage) }
        }
        "return no permission failure when link discord is used without permission" {
            val noPermissionMessage = "no permission"
            val messages = mockk<PlayersMessages>()
            every { messages.noPermissionProfileLinkDiscord } returns noPermissionMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.discord") } returns false
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "discord", "abcd#1234")) should beInstanceOf<NoPermissionFailure>()
            verify(exactly = 1) { sender.sendMessage(noPermissionMessage) }
        }
        "return incorrect usage failure when link discord is used without specifying a Discord tag" {
            val usageMessage = "profile link discord usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkDiscordUsage } returns usageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.discord") } returns true
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "discord")) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(usageMessage) }
        }
        "return missing service failure when link discord is used with no Discord service present" {
            val noDiscordServiceMessage = "no discord service"
            val messages = mockk<PlayersMessages>()
            every { messages.noDiscordService } returns noDiscordServiceMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.discord") } returns true
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKDiscordService::class.java] } returns null
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "discord", "abcd#1234")) should beInstanceOf<MissingServiceFailure>()
            verify(exactly = 1) { sender.sendMessage(noDiscordServiceMessage) }
        }
        "return invalid discord user tag failure when link discord is used with an invalid discord tag" {
            val invalidDiscordTagMessage = "invalid discord tag"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkDiscordInvalidUserTag } returns invalidDiscordTagMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.discord") } returns true
            val discordService = mockk<RPKDiscordService>()
            every { discordService.getUserId(any()) } throws IllegalArgumentException()
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKDiscordService::class.java] } returns discordService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "discord", "abcd#1234")) should beInstanceOf<ProfileLinkDiscordCommand.InvalidDiscordUserTagFailure>()
            verify(exactly = 1) { sender.sendMessage(invalidDiscordTagMessage) }
        }
        "return invalid discord user failure when link discord is used and user ID cannot be found" {
            val invalidDiscordUserMessage = "invalid discord user"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkDiscordInvalidUser } returns invalidDiscordUserMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.discord") } returns true
            val discordService = mockk<RPKDiscordService>()
            every { discordService.getUserId(any()) } returns null
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKDiscordService::class.java] } returns discordService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "discord", "abcd#1234")) should beInstanceOf<ProfileLinkDiscordCommand.InvalidDiscordUserFailure>()
            verify(exactly = 1) { sender.sendMessage(invalidDiscordUserMessage) }
        }
        "return no profile (self) failure when link discord is used and sender does not have a profile" {
            val noProfileSelfMessage = "no profile (self)"
            val messages = mockk<PlayersMessages>()
            every { messages.noProfileSelf } returns noProfileSelfMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.discord") } returns true
            val profile = mockk<RPKThinProfile>()
            every { sender.profile } returns profile
            val userId = DiscordUserId(1)
            val discordService = mockk<RPKDiscordService>()
            every { discordService.getUserId(any()) } returns userId
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKDiscordService::class.java] } returns discordService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "discord", "abcd#1234")) should beInstanceOf<NoProfileSelfFailure>()
            verify(exactly = 1) { sender.sendMessage(noProfileSelfMessage) }
        }
        "return missing service failure when link discord is used and with no Discord profile service present" {
            val noDiscordProfileServiceMessage = "no discord profile service"
            val messages = mockk<PlayersMessages>()
            every { messages.noDiscordProfileService } returns noDiscordProfileServiceMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.discord") } returns true
            val profile = mockk<RPKProfile>()
            every { sender.profile } returns profile
            val userId = DiscordUserId(1)
            val discordService = mockk<RPKDiscordService>()
            every { discordService.getUserId(any()) } returns userId
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKDiscordService::class.java] } returns discordService
            every { testServicesDelegate[RPKDiscordProfileService::class.java] } returns null
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "discord", "abcd#1234")) should beInstanceOf<MissingServiceFailure>()
            verify(exactly = 1) { sender.sendMessage(noDiscordProfileServiceMessage) }
        }
        "return success and send profile link message when link discord is used with valid Discord tag" {
            val discordProfileLinkedMessage = "discord profile linked"
            val messages = mockk<PlayersMessages>()
            every { messages.profileLinkDiscordValid } returns discordProfileLinkedMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.hasPermission("rpkit.players.command.profile.link") } returns true
            every { sender.hasPermission("rpkit.players.command.profile.link.discord") } returns true
            val profile = mockk<RPKProfile>()
            every { profile.name } returns RPKProfileName("abcd")
            every { sender.profile } returns profile
            val userId = DiscordUserId(1)
            val discordProfile = mockk<RPKDiscordProfile>()
            every { discordProfile.discordId } returns userId
            val discordService = mockk<RPKDiscordService>()
            every { discordService.getUserId(any()) } returns userId
            every { discordService.sendMessage(discordProfile, any(), any()) } just runs
            val discordProfileService = mockk<RPKDiscordProfileService>()
            every { discordProfileService.getDiscordProfile(userId) } returns discordProfile
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKDiscordService::class.java] } returns discordService
            every { testServicesDelegate[RPKDiscordProfileService::class.java] } returns discordProfileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("link", "discord", "abcd#1234")) should beInstanceOf<CommandSuccess>()
            verify(exactly = 1) { sender.sendMessage(discordProfileLinkedMessage) }
            verify(exactly = 1) { discordService.sendMessage(discordProfile, any(), any()) }
        }
        "return not a player failure when confirmlink is used from console" {
            val notAPlayerMessage = "not a player"
            val messages = mockk<PlayersMessages>()
            every { messages.notFromConsole } returns notAPlayerMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKConsoleCommandSender>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("confirmlink", "minecraft", "1")) should beInstanceOf<NotAPlayerFailure>()
            verify(exactly = 1) { sender.sendMessage(notAPlayerMessage) }
        }
        "return incorrect usage failure when confirmlink is used without account type or request ID" {
            val confirmLinkUsageMessage = "confirm link usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileConfirmLinkUsage } returns confirmLinkUsageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("confirmlink")) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(confirmLinkUsageMessage) }
        }
        "return invalid ID failure when confirmlink minecraft is used with a non-numerical ID" {
            val invalidIdMessage = "invalid id"
            val messages = mockk<PlayersMessages>()
            every { messages.profileConfirmLinkInvalidId } returns invalidIdMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("confirmlink", "minecraft", "abc")) should beInstanceOf<ProfileConfirmLinkCommand.InvalidIdFailure>()
            verify(exactly = 1) { sender.sendMessage(invalidIdMessage) }
        }
        "return missing service failure when confirmlink minecraft is used with no Minecraft profile service present" {
            val noMinecraftProfileServiceMessage = "no minecraft profile service"
            val messages = mockk<PlayersMessages>()
            every { messages.noMinecraftProfileService } returns noMinecraftProfileServiceMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns null
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("confirmlink", "minecraft", "1")) should beInstanceOf<MissingServiceFailure>()
            verify(exactly = 1) { sender.sendMessage(noMinecraftProfileServiceMessage) }
        }
        "return already linked failure when confirmlink minecraft is used with a Minecraft profile that already has a profile linked" {
            val alreadyLinkedMessage = "already linked"
            val messages = mockk<PlayersMessages>()
            every { messages.profileConfirmLinkInvalidAlreadyLinked } returns alreadyLinkedMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val profile = mockk<RPKProfile>()
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.profile } returns profile
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("confirmlink", "minecraft", "1")) should beInstanceOf<ProfileConfirmLinkCommand.AlreadyLinkedFailure>()
            verify(exactly = 1) { sender.sendMessage(alreadyLinkedMessage) }
        }
        "return invalid request failure when confirmlink minecraft is used with a profile ID that has made no link request" {
            val invalidRequestMessage = "invalid request"
            val messages = mockk<PlayersMessages>()
            every { messages.profileConfirmLinkInvalidRequest } returns invalidRequestMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val profile = mockk<RPKThinProfile>()
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.profile } returns profile
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            every { minecraftProfileService.getMinecraftProfileLinkRequests(sender) } returns emptyList()
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("confirmlink", "minecraft", "1")) should beInstanceOf<ProfileConfirmLinkCommand.InvalidRequestFailure>()
            verify(exactly = 1) { sender.sendMessage(invalidRequestMessage) }
        }
        "return success and link profile when confirmlink minecraft is used with a valid request" {
            val profileLinkedMessage = "profile linked"
            val messages = mockk<PlayersMessages>()
            every { messages.profileConfirmLinkValid } returns profileLinkedMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val thinProfile = mockk<RPKThinProfile>()
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.profile } returns thinProfile
            every { sender.profile = any() } just runs
            val profile = mockk<RPKProfile>()
            every { profile.id } returns RPKProfileId(1)
            val linkRequest = mockk<RPKMinecraftProfileLinkRequest>()
            every { linkRequest.profile } returns profile
            val linkRequests = listOf(linkRequest)
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            every { minecraftProfileService.getMinecraftProfileLinkRequests(sender) } returns linkRequests
            every { minecraftProfileService.updateMinecraftProfile(any()) } just runs
            every { minecraftProfileService.removeMinecraftProfileLinkRequest(any()) } just runs
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("confirmlink", "minecraft", "1")) should beInstanceOf<CommandSuccess>()
            verify(exactly = 1) { sender.sendMessage(profileLinkedMessage) }
            verify(exactly = 1) { sender.profile = profile }
            verify(exactly = 1) { minecraftProfileService.updateMinecraftProfile(sender) }
            verify(exactly = 1) { minecraftProfileService.removeMinecraftProfileLinkRequest(linkRequest) }
        }
        "return invalid profile type failure when confirmlink is used with an invalid profile type" {
            val invalidProfileTypeMessage = "invalid profile type"
            val messages = mockk<PlayersMessages>()
            every { messages.profileConfirmLinkInvalidType } returns invalidProfileTypeMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("confirmlink", "abcd", "1")) should beInstanceOf<ProfileConfirmLinkCommand.InvalidProfileTypeFailure>()
            verify(exactly = 1) { sender.sendMessage(invalidProfileTypeMessage) }
        }
        "return not a player failure when denylink is used from console" {
            val notAPlayerMessage = "not a player"
            val messages = mockk<PlayersMessages>()
            every { messages.notFromConsole } returns notAPlayerMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKConsoleCommandSender>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("denylink", "minecraft", "1")) should beInstanceOf<NotAPlayerFailure>()
            verify(exactly = 1) { sender.sendMessage(notAPlayerMessage) }
        }
        "return incorrect usage failure when denylink is used without account type or request ID" {
            val denyLinkUsageMessage = "deny link usage"
            val messages = mockk<PlayersMessages>()
            every { messages.profileDenyLinkUsage } returns denyLinkUsageMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("denylink")) should beInstanceOf<IncorrectUsageFailure>()
            verify(exactly = 1) { sender.sendMessage(denyLinkUsageMessage) }
        }
        "return invalid ID failure when denylink minecraft is used with a non-numerical ID" {
            val invalidIdMessage = "invalid id"
            val messages = mockk<PlayersMessages>()
            every { messages.profileDenyLinkInvalidId } returns invalidIdMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("denylink", "minecraft", "abc")) should beInstanceOf<ProfileDenyLinkCommand.InvalidIdFailure>()
            verify(exactly = 1) { sender.sendMessage(invalidIdMessage) }
        }
        "return missing service failure when account denylink minecraft is used with no Minecraft profile service present" {
            val noMinecraftProfileServiceMessage = "no minecraft profile service"
            val messages = mockk<PlayersMessages>()
            every { messages.noMinecraftProfileService } returns noMinecraftProfileServiceMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns null
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("denylink", "minecraft", "1")) should beInstanceOf<MissingServiceFailure>()
            verify(exactly = 1) { sender.sendMessage(noMinecraftProfileServiceMessage) }
        }
        "return invalid request failure if denylink minecraft is used with a profile ID that has made no link request" {
            val invalidRequestMessage = "invalid request"
            val messages = mockk<PlayersMessages>()
            every { messages.profileDenyLinkInvalidRequest } returns invalidRequestMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val profile = mockk<RPKThinProfile>()
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.profile } returns profile
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            every { minecraftProfileService.getMinecraftProfileLinkRequests(sender) } returns emptyList()
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("denylink", "minecraft", "1")) should beInstanceOf<ProfileDenyLinkCommand.InvalidRequestFailure>()
            verify(exactly = 1) { sender.sendMessage(invalidRequestMessage) }
        }
        "return success and remove link request if denylink minecraft is used with a valid request" {
            val profileLinkedMessage = "profile linked"
            val messages = mockk<PlayersMessages>()
            every { messages.profileDenyLinkValid } returns profileLinkedMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val thinProfile = mockk<RPKThinProfile>()
            val name = "abcd"
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.name } returns name
            every { sender.profile } returns thinProfile
            every { sender.profile = any() } just runs
            val profile1 = mockk<RPKProfile>()
            every { profile1.id } returns RPKProfileId(1)
            val linkRequest1 = mockk<RPKMinecraftProfileLinkRequest>()
            every { linkRequest1.profile } returns profile1
            val profile2 = mockk<RPKProfile>()
            every { profile2.id } returns RPKProfileId(2)
            val linkRequest2 = mockk<RPKMinecraftProfileLinkRequest>()
            every { linkRequest2.profile } returns profile2
            val linkRequests = listOf(linkRequest1, linkRequest2)
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            every { minecraftProfileService.getMinecraftProfileLinkRequests(sender) } returns linkRequests
            every { minecraftProfileService.updateMinecraftProfile(any()) } just runs
            every { minecraftProfileService.removeMinecraftProfileLinkRequest(any()) } just runs
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            val profileService = mockk<RPKProfileService>()
            every { profileService.generateDiscriminatorFor(any()) } returns RPKProfileDiscriminator(1)
            val newProfile = mockk<RPKProfile>()
            every { profileService.createProfile(any(), any(), any()) } returns newProfile
            every { testServicesDelegate[RPKProfileService::class.java] } returns profileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("denylink", "minecraft", "1")) should beInstanceOf<CommandSuccess>()
            verify(exactly = 1) { sender.sendMessage(profileLinkedMessage) }
            verify(exactly = 1) { minecraftProfileService.removeMinecraftProfileLinkRequest(linkRequest1) }
        }
        "return missing service failure if denylink minecraft is used and there are no requests pending but no profile service is present" {
            val noProfileServiceMessage = "no profile service"
            val messages = mockk<PlayersMessages>()
            every { messages.noProfileService } returns noProfileServiceMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val thinProfile = mockk<RPKThinProfile>()
            val name = "abcd"
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.name } returns name
            every { sender.profile } returns thinProfile
            every { sender.profile = any() } just runs
            val profile1 = mockk<RPKProfile>()
            every { profile1.id } returns RPKProfileId(1)
            val linkRequest1 = mockk<RPKMinecraftProfileLinkRequest>()
            every { linkRequest1.profile } returns profile1
            val linkRequests = listOf(linkRequest1)
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            every { minecraftProfileService.getMinecraftProfileLinkRequests(sender) } returns linkRequests
            every { minecraftProfileService.updateMinecraftProfile(any()) } just runs
            every { minecraftProfileService.removeMinecraftProfileLinkRequest(any()) } just runs
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            every { testServicesDelegate[RPKProfileService::class.java] } returns null
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("denylink", "minecraft", "1")) should beInstanceOf<MissingServiceFailure>()
            verify(exactly = 1) { sender.sendMessage(noProfileServiceMessage) }
            verify(exactly = 1) { minecraftProfileService.removeMinecraftProfileLinkRequest(linkRequest1) }
        }
        "return success and create new profile if no further pending requests exist" {
            val profileCreatedMessage = "profile created"
            val messages = mockk<PlayersMessages>()
            every { messages.profileDenyLinkProfileCreated } returns profileCreatedMessage
            val plugin = mockk<RPKPlayersBukkit>()
            every { plugin.messages } returns messages
            val thinProfile = mockk<RPKThinProfile>()
            val name = "abcd"
            val sender = mockk<RPKMinecraftProfile>()
            every { sender.sendMessage(any<String>()) } just runs
            every { sender.name } returns name
            every { sender.profile } returns thinProfile
            every { sender.profile = any() } just runs
            val profile1 = mockk<RPKProfile>()
            every { profile1.id } returns RPKProfileId(1)
            val linkRequest1 = mockk<RPKMinecraftProfileLinkRequest>()
            every { linkRequest1.profile } returns profile1
            val linkRequests = listOf(linkRequest1)
            val minecraftProfileService = mockk<RPKMinecraftProfileService>()
            every { minecraftProfileService.getMinecraftProfileLinkRequests(sender) } returns linkRequests
            every { minecraftProfileService.updateMinecraftProfile(any()) } just runs
            every { minecraftProfileService.removeMinecraftProfileLinkRequest(any()) } just runs
            val profileService = mockk<RPKProfileService>()
            val discriminator = RPKProfileDiscriminator(1)
            every { profileService.generateDiscriminatorFor(any()) } returns discriminator
            val newProfile = mockk<RPKProfile>()
            every { profileService.createProfile(any(), any(), any()) } returns newProfile
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKMinecraftProfileService::class.java] } returns minecraftProfileService
            every { testServicesDelegate[RPKProfileService::class.java] } returns profileService
            Services.delegate = testServicesDelegate
            val profileCommand = ProfileCommand(plugin)
            profileCommand.onCommand(sender, arrayOf("denylink", "minecraft", "1")) should beInstanceOf<CommandSuccess>()
            verify(exactly = 1) { sender.sendMessage(profileCreatedMessage) }
            verify(exactly = 1) { minecraftProfileService.removeMinecraftProfileLinkRequest(linkRequest1) }
            verify(exactly = 1) { profileService.createProfile(RPKProfileName("abcd"), discriminator, null) }
        }
    }
})