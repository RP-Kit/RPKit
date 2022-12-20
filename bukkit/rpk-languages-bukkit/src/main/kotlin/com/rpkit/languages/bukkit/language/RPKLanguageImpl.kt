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

package com.rpkit.languages.bukkit.language

import com.rpkit.characters.bukkit.species.RPKSpecies
import kotlin.random.Random

class RPKLanguageImpl(
        override val name: RPKLanguageName,
        private val baseUnderstanding: Map<String, Float>,
        private val minimumUnderstandingIncrement: Map<String, Float>,
        private val maximumUnderstandingIncrement: Map<String, Float>,
        private val cypher: Map<String, String>
) : RPKLanguage {

    override fun getBaseUnderstanding(species: RPKSpecies): Float {
        return baseUnderstanding[species.name.value] ?: 0f
    }

    override fun getMinimumUnderstandingIncrement(species: RPKSpecies): Float {
        return minimumUnderstandingIncrement[species.name.value] ?: 0f
    }

    override fun getMaximumUnderstandingIncrement(species: RPKSpecies): Float {
        return maximumUnderstandingIncrement[species.name.value] ?: 0f
    }

    override fun randomUnderstandingIncrement(species: RPKSpecies): Float {
        val min = getMinimumUnderstandingIncrement(species)
        val max = getMaximumUnderstandingIncrement(species)
        return min + (Random.nextFloat() * (max - min))
    }

    override fun apply(message: String, senderUnderstanding: Float, receiverUnderstanding: Float): String {
        return applyCypher(applyGarble(message, senderUnderstanding), receiverUnderstanding)
    }

    private fun applyGarble(message: String, understanding: Float): String {
        var result = ""
        for (char in message) {
            result = if (understanding >= Random.nextFloat() * 100) {
                result + char
            } else {
                char + result
            }
        }
        return result
    }

    private fun applyCypher(message: String, understanding: Float): String {
        var pos = 0
        var result = ""
        msg@ while (pos < message.length) {
            for (cypherKey in cypher.keys) {
                if (pos + cypherKey.length <= message.length
                        && message.substring(pos, pos + cypherKey.length) == cypherKey) {
                    result += if (understanding >= Random.nextFloat() * 100) {
                        cypherKey
                    } else {
                        cypher[cypherKey]
                    }
                    pos += cypherKey.length
                    continue@msg
                }
            }
            result += message[pos]
            pos++
        }
        return result
    }
}