/*
 * Copyright 2018 Ross Binden
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

import com.rpkit.selection.bukkit.selection.RPKSelectionImpl
import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import io.mockk.every
import io.mockk.mockk
import org.bukkit.World
import org.bukkit.block.Block

class RPKSelectionImplTests: WordSpec() {

    init {
        val world = mockk<World>()
        val minPosition = mockk<Block>()
        every { minPosition.world } returns world
        every { minPosition.x } returns 1
        every { minPosition.y } returns 1
        every { minPosition.z } returns 1
        every { world.getBlockAt(1, 1, 1) } returns minPosition
        val maxPosition = mockk<Block>()
        every { maxPosition.world } returns world
        every { maxPosition.x } returns 3
        every { maxPosition.y } returns 3
        every { maxPosition.z } returns 3
        every { world.getBlockAt(3, 3, 3) } returns maxPosition
        val selection = RPKSelectionImpl(
                0,
                mockk(),
                world,
                minPosition,
                maxPosition
        )

        "RPKSelectionImpl.contains" should {
            "return true when it contains the given point" {
                val block = mockk<Block>()
                every { block.world } returns world
                every { block.x } returns 2
                every { block.y } returns 2
                every { block.z } returns 2

                selection.contains(block) shouldBe true
            }
        }

        "RPKSelectionImpl.contains" should {
            "return false when it does not contain the given point" {
                val block = mockk<Block>()
                every { block.world } returns world
                every { block.x } returns 4
                every { block.y } returns 4
                every { block.z } returns 4

                selection.contains(block) shouldBe false
            }
        }

        "RPKSelectionImpl.contains" should {
            "return false when the given point is in another world" {
                val otherWorld = mockk<World>()
                val block = mockk<Block>()
                every { block.world } returns otherWorld
                every { block.x } returns 2
                every { block.y } returns 2
                every { block.z } returns 2

                selection.contains(block) shouldBe false
            }
        }
    }

}