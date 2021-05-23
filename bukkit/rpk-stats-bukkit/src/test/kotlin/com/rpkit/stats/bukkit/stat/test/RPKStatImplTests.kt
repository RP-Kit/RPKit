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

package com.rpkit.stats.bukkit.stat.test

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.expression.RPKExpressionService
import com.rpkit.core.expression.RPKExpressionServiceImpl
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import com.rpkit.core.service.ServicesDelegate
import com.rpkit.stats.bukkit.stat.RPKStatImpl
import com.rpkit.stats.bukkit.stat.RPKStatName
import com.rpkit.stats.bukkit.stat.RPKStatVariable
import com.rpkit.stats.bukkit.stat.RPKStatVariableName
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk


class RPKStatImplTests : WordSpec({
    val character = mockk<RPKCharacter>()
    val statVariables = listOf(object : RPKStatVariable {
        override val name = RPKStatVariableName("level")

        override fun get(character: RPKCharacter): Double {
            return 5.toDouble()
        }

    })
    val stat = RPKStatImpl(
        RPKStatName("test"),
        "level*3"
    )
    "RPKStatImpl.get" should {
        "successfully parse its script and return the correct value" {
            val plugin = mockk<RPKPlugin>()
            val testServicesDelegate = mockk<ServicesDelegate>()
            every { testServicesDelegate[RPKExpressionService::class.java] } returns RPKExpressionServiceImpl(plugin)
            Services.delegate = testServicesDelegate
            stat.get(character, statVariables) shouldBe 15
        }
    }
})