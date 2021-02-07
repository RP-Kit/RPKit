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

package com.rpkit.statbuilds.bukkit.statattribute

import com.rpkit.statbuilds.bukkit.RPKStatBuildsBukkit

class RPKStatAttributeServiceImpl(override val plugin: RPKStatBuildsBukkit) : RPKStatAttributeService {

    private val statAttributeMap: Map<String, RPKStatAttribute> = plugin.config.getConfigurationSection("stat-attributes")
        ?.getKeys(false)
        ?.map(::RPKStatAttributeName)
        ?.map(::RPKStatAttributeImpl)
        ?.associateBy { attribute -> attribute.name.value }
        ?: emptyMap()

    override val statAttributes = statAttributeMap.values.toList()

    override fun getStatAttribute(name: RPKStatAttributeName) = statAttributeMap[name.value]
}