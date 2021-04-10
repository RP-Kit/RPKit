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
import com.rpkit.core.expression.RPKExpressionService
import com.rpkit.core.service.Services
import java.util.concurrent.CompletableFuture

/**
 * Stat implementation.
 */
class RPKStatImpl(
        override val name: RPKStatName,
        override val formula: String
) : RPKStat {
    override fun get(character: RPKCharacter, variables: List<RPKStatVariable>): CompletableFuture<Int> {
        val expressionService = Services[RPKExpressionService::class.java] ?: return CompletableFuture.completedFuture(0)
        val expression = expressionService.createExpression(formula)
        return CompletableFuture.supplyAsync {
            expression.parseInt(variables.map { variable ->
                variable.name.value to variable.get(character).join()
            }.toMap()) ?: 0
        }
    }
}