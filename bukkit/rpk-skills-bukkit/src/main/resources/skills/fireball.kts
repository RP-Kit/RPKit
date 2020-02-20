/*
 * Copyright 2020 Ren Binden
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

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.skills.bukkit.skills.RPKSkill
import com.rpkit.skills.bukkit.skills.RPKSkillPointProvider
import com.rpkit.skills.bukkit.skills.RPKSkillTypeProvider
import org.bukkit.Bukkit

class Fireball : RPKSkill {
    override val name = "fireball"
    override val manaCost = 2
    override val cooldown = 10
    override fun use(character: RPKCharacter) {
        val minecraftProfile = character.minecraftProfile ?: return
        val bukkitPlayer = Bukkit.getPlayer(minecraftProfile.minecraftUUID) ?: return
        if (!bukkitPlayer.isOnline) return
        val bukkitOnlinePlayer = bukkitPlayer.player
        bukkitOnlinePlayer?.launchProjectile(org.bukkit.entity.Fireball::class.java)
    }

    override fun canUse(character: RPKCharacter): Boolean {
        val skillTypeProvider = core.getServiceManager().getServiceProvider(RPKSkillTypeProvider::class)
        val skillType = skillTypeProvider.getSkillType("magic_offence")
        val skillPointProvider = core.getServiceManager().getServiceProvider(RPKSkillPointProvider::class)
        val magicOffenceSkillPoints = skillPointProvider.getSkillPoints(character, skillType)
        return magicOffenceSkillPoints >= 5
    }
}