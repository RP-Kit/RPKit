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

package com.rpkit.selection.bukkit.selection.test

import com.rpkit.core.location.RPKBlockLocation
import com.rpkit.selection.bukkit.selection.RPKSelectionImpl
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk

class RPKSelectionImplTests : WordSpec({
    val minPosition = RPKBlockLocation(
        world = "world",
        x = 1,
        y = 1,
        z = 1
    )
    val maxPosition = RPKBlockLocation(
        world = "world",
        x = 3,
        y = 3,
        z = 3
    )
    val selection = RPKSelectionImpl(
            mockk(),
            "world",
            minPosition,
            maxPosition
    )

    "RPKSelectionImpl.contains" should {
        "return true when it contains the given point" {
            val block = RPKBlockLocation(
                world = "world",
                x = 2,
                y = 2,
                z = 2
            )

            selection.contains(block) shouldBe true
        }
        "return false when it does not contain the given point" {
            val block = RPKBlockLocation(
                world = "world",
                x = 4,
                y = 4,
                z = 4
            )

            selection.contains(block) shouldBe false
        }
        "return false when the given point is in another world" {
            val block = RPKBlockLocation(
                world = "otherWorld",
                x = 2,
                y = 2,
                z = 2
            )

            selection.contains(block) shouldBe false
        }
    }
})