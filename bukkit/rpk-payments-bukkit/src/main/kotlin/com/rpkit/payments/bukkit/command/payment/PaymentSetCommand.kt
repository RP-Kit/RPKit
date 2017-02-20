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
import com.rpkit.payments.bukkit.command.payment.set.PaymentSetAmountCommand
import com.rpkit.payments.bukkit.command.payment.set.PaymentSetCurrencyCommand
import com.rpkit.payments.bukkit.command.payment.set.PaymentSetIntervalCommand
import com.rpkit.payments.bukkit.command.payment.set.PaymentSetNameCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Payment set command.
 * Parent for all payment group modification commands.
 */
class PaymentSetCommand(private val plugin: RPKPaymentsBukkit): CommandExecutor {

    private val paymentSetNameCommand = PaymentSetNameCommand(plugin)
    private val paymentSetAmountCommand = PaymentSetAmountCommand(plugin)
    private val paymentSetCurrencyCommand = PaymentSetCurrencyCommand(plugin)
    private val paymentSetIntervalCommand = PaymentSetIntervalCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("name", ignoreCase = true)) {
                return paymentSetNameCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("amount", ignoreCase = true)) {
                return paymentSetAmountCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("currency", ignoreCase = true)) {
                return paymentSetCurrencyCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("interval", ignoreCase = true)) {
                return paymentSetIntervalCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(plugin.core.messages["payment-set-usage"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["payment-set-usage"])
        }
        return true
    }

}