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

package com.rpkit.core.expression

import org.scijava.parsington.Variable
import kotlin.math.roundToInt

internal class RPKExpressionImpl(
    private val expression: String,
    private val functions: Map<String, RPKFunction>,
    private val constants: Map<String, Any?>
) : RPKExpression {

    override fun parse(variables: Map<String, Any?>): Any? {
        val parsingtonEvaluator = RPKParsingtonEvaluator(functions)
        constants.forEach { (key, value) -> parsingtonEvaluator.evaluate("$key=$value") }
        variables.forEach { (key, value) -> parsingtonEvaluator.evaluate("$key=$value") }
        return parsingtonEvaluator.evaluate(expression)
    }

    override fun parseDouble(variables: Map<String, Any?>): Double? {
        val parsingtonEvaluator = RPKParsingtonEvaluator(functions)
        constants.forEach { (key, value) -> parsingtonEvaluator.evaluate("$key=$value") }
        variables.forEach { (key, value) -> parsingtonEvaluator.evaluate("$key=$value") }
        var result = parsingtonEvaluator.evaluate(expression)
        if (result is Variable) {
            result = parsingtonEvaluator.get(result)
        }
        if (result is Number) {
            return result.toDouble()
        }
        return null
    }

    override fun parseInt(variables: Map<String, Any?>): Int? {
        return parseDouble(variables)?.roundToInt()
    }

}