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

import com.rpkit.core.expression.function.log.ExponentialFunction
import com.rpkit.core.expression.function.log.LogarithmBase10Function
import com.rpkit.core.expression.function.log.LogarithmBase2Function
import com.rpkit.core.expression.function.log.NaturalLogarithmFunction
import com.rpkit.core.expression.function.misc.AbsoluteFunction
import com.rpkit.core.expression.function.misc.IfFunction
import com.rpkit.core.expression.function.misc.RandomFunction
import com.rpkit.core.expression.function.misc.SignumFunction
import com.rpkit.core.expression.function.misc.SquareRootFunction
import com.rpkit.core.expression.function.misc.StringFunction
import com.rpkit.core.expression.function.misc.SumFunction
import com.rpkit.core.expression.function.rounding.CeilFunction
import com.rpkit.core.expression.function.rounding.FloorFunction
import com.rpkit.core.expression.function.rounding.RoundFunction
import com.rpkit.core.expression.function.rounding.RoundToIntFunction
import com.rpkit.core.expression.function.stats.AverageFunction
import com.rpkit.core.expression.function.stats.MaximumFunction
import com.rpkit.core.expression.function.stats.MinimumFunction
import com.rpkit.core.expression.function.string.LeftFunction
import com.rpkit.core.expression.function.string.LengthFunction
import com.rpkit.core.expression.function.string.LowerCaseFunction
import com.rpkit.core.expression.function.string.MiddleFunction
import com.rpkit.core.expression.function.string.RightFunction
import com.rpkit.core.expression.function.string.SubstringFunction
import com.rpkit.core.expression.function.string.TrimFunction
import com.rpkit.core.expression.function.string.UpperCaseFunction
import com.rpkit.core.expression.function.trig.ArcCosineFunction
import com.rpkit.core.expression.function.trig.ArcSineFunction
import com.rpkit.core.expression.function.trig.ArcTangent2Function
import com.rpkit.core.expression.function.trig.ArcTangentFunction
import com.rpkit.core.expression.function.trig.CosecantFunction
import com.rpkit.core.expression.function.trig.CosineFunction
import com.rpkit.core.expression.function.trig.CotangentFunction
import com.rpkit.core.expression.function.trig.HyperbolicCosineFunction
import com.rpkit.core.expression.function.trig.HyperbolicSineFunction
import com.rpkit.core.expression.function.trig.HyperbolicTangentFunction
import com.rpkit.core.expression.function.trig.InverseHyperbolicCosineFunction
import com.rpkit.core.expression.function.trig.InverseHyperbolicSineFunction
import com.rpkit.core.expression.function.trig.InverseHyperbolicTangentFunction
import com.rpkit.core.expression.function.trig.SecantFunction
import com.rpkit.core.expression.function.trig.SineFunction
import com.rpkit.core.expression.function.trig.TangentFunction
import com.rpkit.core.plugin.RPKPlugin
import kotlin.math.E
import kotlin.math.PI

class RPKExpressionServiceImpl(override val plugin: RPKPlugin) : RPKExpressionService {

    private val functions = mutableMapOf(
        // Trig
        "sin" to SineFunction(),
        "cos" to CosineFunction(),
        "tan" to TangentFunction(),
        "asin" to ArcSineFunction(),
        "acos" to ArcCosineFunction(),
        "atan" to ArcTangentFunction(),
        "atan2" to ArcTangent2Function(),
        "sec" to SecantFunction(),
        "cosec" to CosecantFunction(),
        "cot" to CotangentFunction(),
        "sinh" to HyperbolicSineFunction(),
        "cosh" to HyperbolicCosineFunction(),
        "tanh" to HyperbolicTangentFunction(),
        "asinh" to InverseHyperbolicSineFunction(),
        "acosh" to InverseHyperbolicCosineFunction(),
        "atanh" to InverseHyperbolicTangentFunction(),
        // Log
        "ln" to NaturalLogarithmFunction(),
        "log" to LogarithmBase10Function(),
        "lb" to LogarithmBase2Function(),
        "exp" to ExponentialFunction(),
        // Stats
        "avg" to AverageFunction(),
        "min" to MinimumFunction(),
        "max" to MaximumFunction(),
        // Rounding
        "round" to RoundFunction(),
        "rint" to RoundToIntFunction(),
        "floor" to FloorFunction(),
        "ceil" to CeilFunction(),
        // Misc
        "if" to IfFunction(),
        "str" to StringFunction(),
        "abs" to AbsoluteFunction(),
        "rand" to RandomFunction(),
        "sqrt" to SquareRootFunction(),
        "sum" to SumFunction(),
        "signum" to SignumFunction(),
        // String
        "left" to LeftFunction(),
        "right" to RightFunction(),
        "mid" to MiddleFunction(),
        "substr" to SubstringFunction(),
        "lower" to LowerCaseFunction(),
        "upper" to UpperCaseFunction(),
        "len" to LengthFunction(),
        "trim" to TrimFunction()
    )

    private val constants = mutableMapOf<String, Any?>(
        "e" to E,
        "pi" to PI
    )
    override fun addFunction(name: String, function: RPKFunction) {
        functions[name] = function
    }

    override fun createExpression(expression: String): RPKExpression {
        return RPKExpressionImpl(expression, functions, constants)
    }

}