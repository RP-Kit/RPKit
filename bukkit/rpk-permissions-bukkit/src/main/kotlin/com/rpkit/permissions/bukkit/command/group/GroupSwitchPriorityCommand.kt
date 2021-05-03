package com.rpkit.permissions.bukkit.command.group

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroup
import com.rpkit.permissions.bukkit.group.RPKGroupName
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.players.bukkit.command.result.InvalidTargetMinecraftProfileFailure
import com.rpkit.players.bukkit.command.result.NoMinecraftProfileOtherFailure
import com.rpkit.players.bukkit.command.result.NoProfileOtherFailure
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.util.concurrent.CompletableFuture

class GroupSwitchPriorityCommand(private val plugin: RPKPermissionsBukkit) : RPKCommandExecutor {

    class InvalidGroupFailure : CommandFailure()
    class GroupNotPresentFailure(val group: RPKGroup): CommandFailure()

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (!sender.hasPermission("rpkit.permissions.command.group.switchpriority")) {
            sender.sendMessage(plugin.messages.noPermissionGroupSwitchPriority)
            return CompletableFuture.completedFuture(NoPermissionFailure("rpkit.permissions.command.group.switchpriority"))
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return CompletableFuture.completedFuture(MissingServiceFailure(RPKMinecraftProfileService::class.java))
        }
        if (args.size < 3) {
            sender.sendMessage(plugin.messages.groupSwitchPriorityUsage)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
        }
        val targetBukkitPlayer = plugin.server.getPlayer(args[0])
        if (targetBukkitPlayer == null) {
            sender.sendMessage(plugin.messages.groupSwitchPriorityInvalidTarget)
            return CompletableFuture.completedFuture(InvalidTargetMinecraftProfileFailure())
        }
        val targetMinecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(targetBukkitPlayer)
        if (targetMinecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileOther)
            return CompletableFuture.completedFuture(NoMinecraftProfileOtherFailure())
        }
        val targetProfile = targetMinecraftProfile.profile
        if (targetProfile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfile)
            return CompletableFuture.completedFuture(NoProfileOtherFailure())
        }
        val groupService = Services[RPKGroupService::class.java]
        if (groupService == null) {
            sender.sendMessage(plugin.messages.noGroupService)
            return CompletableFuture.completedFuture(MissingServiceFailure(RPKGroupService::class.java))
        }
        val group1 = groupService.getGroup(RPKGroupName(args[1]))
        if (group1 == null) {
            sender.sendMessage(plugin.messages.groupSwitchPriorityInvalidGroup.withParameters(
                groupName = args[1]
            ))
            return CompletableFuture.completedFuture(InvalidGroupFailure())
        }
        val group2 = groupService.getGroup(RPKGroupName(args[2]))
        if (group2 == null) {
            sender.sendMessage(plugin.messages.groupSwitchPriorityInvalidGroup.withParameters(
                groupName = args[2]
            ))
            return CompletableFuture.completedFuture(InvalidGroupFailure())
        }
        val group1Priority = groupService.getGroupPriority(targetProfile, group1)
        if (group1Priority == null) {
            sender.sendMessage(plugin.messages.groupSwitchPriorityInvalidGroupNotPresent.withParameters(
                profile = targetProfile,
                group = group1
            ))
            return CompletableFuture.completedFuture(GroupNotPresentFailure(group1))
        }
        val group2Priority = groupService.getGroupPriority(targetProfile, group2)
        if (group2Priority == null) {
            sender.sendMessage(plugin.messages.groupSwitchPriorityInvalidGroupNotPresent.withParameters(
                profile = targetProfile,
                group = group2
            ))
            return CompletableFuture.completedFuture(GroupNotPresentFailure(group2))
        }
        groupService.setGroupPriority(targetProfile, group1, group2Priority)
        groupService.setGroupPriority(targetProfile, group2, group1Priority)
        sender.sendMessage(plugin.messages.groupSwitchPriorityValid.withParameters(
            profile = targetProfile,
            group1 = group1,
            group2 = group2
        ))
        return CompletableFuture.completedFuture(CommandSuccess)
    }

}