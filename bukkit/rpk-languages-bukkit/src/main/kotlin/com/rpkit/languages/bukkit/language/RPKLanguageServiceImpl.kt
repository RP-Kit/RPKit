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

package com.rpkit.languages.bukkit.language

import com.rpkit.languages.bukkit.RPKLanguagesBukkit

class RPKLanguageServiceImpl(override val plugin: RPKLanguagesBukkit) : RPKLanguageService {
    override val languages = plugin.config
            .getConfigurationSection("languages")
            ?.getKeys(false)
            ?.map { languageName ->
                RPKLanguageImpl(
                        RPKLanguageName(languageName),
                    plugin.config.getConfigurationSection("languages.$languageName.default-race-understanding")
                        ?.getKeys(false)
                        ?.associate { race ->
                            race to plugin.config.getDouble("languages.$languageName.default-race-understanding.$race")
                                .toFloat()
                        } ?: emptyMap(),
                    plugin.config.getConfigurationSection("languages.$languageName.understanding-increment")
                        ?.getKeys(false)
                        ?.associate { race ->
                            race to plugin.config.getDouble("languages.$languageName.understanding-increment.$race.minimum")
                                .toFloat()
                        } ?: emptyMap(),
                    plugin.config.getConfigurationSection("languages.$languageName.understanding-increment")
                        ?.getKeys(false)
                        ?.associate { race ->
                            race to plugin.config.getDouble("languages.$languageName.understanding-increment.$race.maximum")
                                .toFloat()
                        } ?: emptyMap(),
                    plugin.config.getConfigurationSection("languages.$languageName.cypher")
                        ?.getKeys(false)
                        ?.associate { key ->
                            key to (plugin.config.getString("languages.$languageName.cypher.$key") ?: key)
                        } ?: emptyMap()
                )
            }
            ?: emptyList()

    override fun getLanguage(name: RPKLanguageName): RPKLanguage? {
        return languages.firstOrNull { it.name.value == name.value }
    }

}