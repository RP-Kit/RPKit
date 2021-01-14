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

import org.scijava.parsington.Literals
import org.scijava.parsington.Tokens
import org.scijava.parsington.Variable
import org.scijava.parsington.eval.DefaultEvaluator

internal class RPKParsingtonEvaluator(private val functions: Map<String, RPKFunction>) : DefaultEvaluator() {

    override fun function(a: Any, b: Any): Any? {
        val element = listElement(a, b)
        if (element != null) return element
        if (Tokens.isVariable(a)) {
            val name = (a as Variable).token
            val result = callFunction(name, b)
            if (result != null) return result
        }

        // NB: Unknown function type.
        return null
    }

    private fun listElement(a: Any, b: Any): Any? {
        val value = try {
            value(a)
        } catch (exc: IllegalArgumentException) {
            return null
        }
        if (value !is List<*>) return null
        if (b !is List<*>) return null
        val index = (b[0] as? Number ?: Literals.parseNumber(b[0].toString()))?.toInt() ?: return null
        return if (b.size != 1) null else value[index] // not a 1-D access
    }

    private fun callFunction(name: String, b: Any): Any? {
        return when  {
            name == "postfix" && b is String -> parser.parsePostfix(b)
            name == "tree" && b is String -> parser.parseTree(b)
            functions.containsKey(name) && b is List<*> -> functions[name]?.invoke(*b.toTypedArray())
            functions.containsKey(name) && b !is List<*> -> functions[name]?.invoke(b)
            else -> null
        }
    }

}