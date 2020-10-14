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

package com.rpkit.stats.bukkit.stat.test

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.stats.bukkit.stat.RPKStatImpl
import com.rpkit.stats.bukkit.stat.RPKStatVariable
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import io.mockk.mockk


class RPKStatImplTests : WordSpec() {

    init {
        val character = mockk<RPKCharacter>()
        val statVariables = listOf(object : RPKStatVariable {
            override val name: String = "level"

            override fun get(character: RPKCharacter): Double {
                return 5.toDouble()
            }

        })
        val stat = RPKStatImpl(
                "test",
                "level*3"
        )
        "RPKStatImpl.get" should {
            "successfully parse its script and return the correct value" {
                stat.get(character, statVariables) shouldBe 15
            }
        }
    }

}