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

package com.rpkit.payments.bukkit.command.payment

import com.rpkit.payments.bukkit.RPKPaymentsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Payment command.
 * Parent for all payment group management commands.
 */
class PaymentCommand(private val plugin: RPKPaymentsBukkit): CommandExecutor {

    private val paymentCreateCommand = PaymentCreateCommand(plugin)
    private val paymentInviteCommand = PaymentInviteCommand(plugin)
    private val paymentKickCommand = PaymentKickCommand(plugin)
    private val paymentJoinCommand = PaymentJoinCommand(plugin)
    private val paymentLeaveCommand = PaymentLeaveCommand(plugin)
    private val paymentWithdrawCommand = PaymentWithdrawCommand(plugin)
    private val paymentDepositCommand = PaymentDepositCommand(plugin)
    private val paymentListCommand = PaymentListCommand(plugin)
    private val paymentInfoCommand = PaymentInfoCommand(plugin)
    private val paymentSetCommand = PaymentSetCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("create", ignoreCase = true)) {
                return paymentCreateCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("invite", ignoreCase = true)) {
                return paymentInviteCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("kick", ignoreCase = true)) {
                return paymentKickCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("join", ignoreCase = true)) {
                return paymentJoinCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("leave", ignoreCase = true)) {
                return paymentLeaveCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("withdraw", ignoreCase = true)) {
                return paymentWithdrawCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("deposit", ignoreCase = true)) {
                return paymentDepositCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("list", ignoreCase = true)) {
                return paymentListCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("info", ignoreCase = true)) {
                return paymentInfoCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("set", ignoreCase = true)) {
                return paymentSetCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(plugin.core.messages["payment-usage"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["payment-usage"])
        }
        return true
    }

}
