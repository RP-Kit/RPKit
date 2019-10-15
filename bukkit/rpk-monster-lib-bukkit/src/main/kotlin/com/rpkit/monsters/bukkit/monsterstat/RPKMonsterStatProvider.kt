/*
 * Copyright 2019 Ren Binden
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

package com.rpkit.monsters.bukkit.monsterstat

import com.rpkit.core.service.ServiceProvider
import org.bukkit.entity.LivingEntity


interface RPKMonsterStatProvider: ServiceProvider {

    fun getMonsterHealth(monster: LivingEntity): Double
    fun setMonsterHealth(monster: LivingEntity, health: Double)
    fun getMonsterMaxHealth(monster: LivingEntity): Double
    fun getMonsterMinDamageMultiplier(monster: LivingEntity): Double
    fun setMonsterMinDamageMultiplier(monster: LivingEntity, minDamageMultiplier: Double)
    fun getMonsterMinDamage(monster: LivingEntity): Double
    fun setMonsterMinDamage(monster: LivingEntity, minDamage: Double)
    fun getMonsterMaxDamageMultiplier(monster: LivingEntity): Double
    fun setMonsterMaxDamageMultiplier(monster: LivingEntity, maxDamageMultiplier: Double)
    fun getMonsterMaxDamage(monster: LivingEntity): Double
    fun setMonsterMaxDamage(monster: LivingEntity, maxDamage: Double)

}