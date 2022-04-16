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

package com.rpkit.rolling.bukkit.roll

import org.bukkit.ChatColor.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.sign
import kotlin.random.Random


class Roll(vararg parts: RollPart) {
    private val parts: List<RollPart>

    interface RollPart {
        fun determine(): RollPartResult
    }

    class Die @JvmOverloads constructor(
        private val rolls: Int,
        private val sides: Int,
        private val multiplier: Int = 1
    ) :
        RollPart {
        private val random: Random = Random
        override fun determine(): RollPartResult {
            val results: MutableList<Int> = ArrayList(rolls)
            for (i in 0 until rolls) {
                results.add(multiplier * (random.nextInt(sides) + 1))
            }
            return RollPartResult(
                this,
                results
            )
        }

        override fun toString(): String {
            return (if (sign(multiplier.toFloat()) == -1f) "-" else "") + rolls + "d" + sides
        }
    }

    class Modifier(private val value: Int) : RollPart {
        override fun determine(): RollPartResult {
            val results: MutableList<Int> = ArrayList(1)
            results.add(value)
            return RollPartResult(
                this,
                results
            )
        }

        override fun toString(): String {
            return value.toString()
        }
    }

    init {
        this.parts = parts.toList()
    }

    fun roll(): List<RollPartResult> {
        return parts.map(RollPart::determine)
    }

    override fun toString(): String {
        val roll = parts.map(RollPart::toString)
            .reduce { a: String, b: String -> a + (if (b.startsWith("-")) "" else "+") + b }
        return if (roll.startsWith("+")) roll.substring(1) else roll
    }

    fun toDisplayString(): String {
        val roll = parts
            .map { rollPart: RollPart ->
                when (rollPart) {
                    is Die -> "$AQUA$rollPart$WHITE"
                    is Modifier -> "$YELLOW$rollPart$WHITE"
                    else -> rollPart.toString()
                }
            }
            .reduce { a: String, b: String ->
                a + (if (stripColor(b)!!.startsWith("-")) "" else "+") + b
            }
        return if (stripColor(roll)!!.startsWith("+")) {
            roll.replaceFirst("+", "")
        } else {
            roll
        }
    }

    companion object {
        @Throws(NumberFormatException::class)
        fun parse(rollString: String): Roll {
            val parts: MutableList<RollPart> = ArrayList()
            val diePattern = Pattern.compile("[+\\-]\\d*d\\d+")
            val fullRollString = if (rollString.startsWith("+")) rollString else "+$rollString"
            val dieMatcher = diePattern.matcher(fullRollString)
            while (dieMatcher.find()) {
                val dieRollString: String = dieMatcher.group()
                val multiplier = if (dieRollString.startsWith("-")) -1 else 1
                val rollSections = dieRollString.split("d".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                val diceAmountString = rollSections[0].substring(1)
                val dieFaces = rollSections[1].toInt()
                val rollCount = if (diceAmountString.isEmpty()) 1 else diceAmountString.toInt()
                parts.add(Die(rollCount, dieFaces, multiplier))
            }
            val rollStringWithoutDice = fullRollString.replace("[+\\-]\\d*d\\d+".toRegex(), "")
            val literalPattern: Pattern = Pattern.compile("([+\\-]\\d+)(?!d)")
            val literalMatcher: Matcher = literalPattern.matcher(rollStringWithoutDice)
            while (literalMatcher.find()) {
                val amount: Int = literalMatcher.group(1).toInt()
                parts.add(Modifier(amount))
            }
            return Roll(*parts.toTypedArray())
        }
    }
}