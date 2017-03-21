package com.rpkit.players.bukkit.profile

import org.kohsuke.github.GitHub
import java.io.IOException

class RPKGitHubProfileImpl(
        override var id: Int = 0,
        override val profile: RPKProfile,
        override val name: String,
        override val oauthToken: String
) : RPKGitHubProfile {

    @Throws(IOException::class)
    constructor(profile: RPKProfile, oauthToken: String): this(profile = profile, name = GitHub.connectUsingOAuth(oauthToken).myself.login, oauthToken = oauthToken)

}
