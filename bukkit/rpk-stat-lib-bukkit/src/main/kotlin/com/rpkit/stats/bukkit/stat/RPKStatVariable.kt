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

package com.rpkit.stats.bukkit.stat

import com.rpkit.characters.bukkit.character.RPKCharacter
import java.util.concurrent.CompletableFuture

/**
 * Represents a stat variable.
 * Used when calculating stats.
 */
interface RPKStatVariable {

    /**
     * The name of the variable.
     */
    val name: RPKStatVariableName

    /**
     * Gets the value of the variable for the given character.
     */
    fun get(character: RPKCharacter): CompletableFuture<Double>

}