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
import io.kotlintest.mock.`when`
import io.kotlintest.mock.mock
import io.kotlintest.specs.WordSpec
import org.bukkit.World
import org.bukkit.block.Block

class RPKSelectionImplTests: WordSpec() {

    init {
        val world = mock<World>()
        val minPosition = mock<Block>()
        `when`(minPosition.world).thenReturn(world)
        `when`(minPosition.x).thenReturn(1)
        `when`(minPosition.y).thenReturn(1)
        `when`(minPosition.z).thenReturn(1)
        `when`(world.getBlockAt(1, 1, 1)).thenReturn(minPosition)
        val maxPosition = mock<Block>()
        `when`(maxPosition.world).thenReturn(world)
        `when`(maxPosition.x).thenReturn(3)
        `when`(maxPosition.y).thenReturn(3)
        `when`(maxPosition.z).thenReturn(3)
        `when`(world.getBlockAt(3, 3, 3)).thenReturn(maxPosition)
        val selection = RPKSelectionImpl(
                0,
                mock(),
                world,
                minPosition,
                maxPosition
        )

        "RPKSelectionImpl.contains" should {
            "return true when it contains the given point" {
                val block = mock<Block>()
                `when`(block.world).thenReturn(world)
                `when`(block.x).thenReturn(2)
                `when`(block.y).thenReturn(2)
                `when`(block.z).thenReturn(2)

                selection.contains(block) shouldBe true
            }
        }

        "RPKSelectionImpl.contains" should {
            "return false when it does not contain the given point" {
                val block = mock<Block>()
                `when`(block.world).thenReturn(world)
                `when`(block.x).thenReturn(4)
                `when`(block.y).thenReturn(4)
                `when`(block.z).thenReturn(4)

                selection.contains(block) shouldBe false
            }
        }

        "RPKSelectionImpl.contains" should {
            "return false when the given point is in another world" {
                val otherWorld = mock<World>()
                val block = mock<Block>()
                `when`(block.world).thenReturn(otherWorld)
                `when`(block.x).thenReturn(2)
                `when`(block.y).thenReturn(2)
                `when`(block.z).thenReturn(2)

                selection.contains(block) shouldBe false
            }
        }
    }

}