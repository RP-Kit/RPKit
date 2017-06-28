package com.rpkit.players.bukkit.profile

import com.rpkit.core.database.Entity


interface RPKGitHubProfile: Entity {

    val profile: RPKProfile
    val name: String
    val oauthToken: String

}