package com.rpkit.players.bukkit.web

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.web.discord.DiscordProfileHandler
import com.rpkit.players.bukkit.web.github.GitHubProfileHandler
import com.rpkit.players.bukkit.web.irc.IRCProfileHandler
import com.rpkit.players.bukkit.web.minecraft.MinecraftProfileHandler
import com.rpkit.players.bukkit.web.profile.ProfileHandler
import org.http4k.core.Method.DELETE
import org.http4k.core.Method.GET
import org.http4k.core.Method.PATCH
import org.http4k.core.Method.POST
import org.http4k.core.Method.PUT
import org.http4k.core.then
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

class PlayersWebAPI(plugin: RPKPlayersBukkit) {

    private val profileHandler = ProfileHandler()
    private val minecraftProfileHandler = MinecraftProfileHandler()
    private val discordProfileHandler = DiscordProfileHandler()
    private val githubProfileHandler = GitHubProfileHandler()
    private val ircProfileHandler = IRCProfileHandler()
    private val app = ServerFilters.CatchAll()
        .then(ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive))
        .then(routes(
            "/players" bind routes(
                "/api" bind routes (
                    "/v2" bind routes(
                        "/profile" bind routes(
                            "/{name}/{discriminator}" bind GET to profileHandler::get,
                            "/{name}/{discriminator}" bind PUT to authenticated().then(profileHandler::put),
                            "/{name}/{discriminator}" bind PATCH to authenticated().then(profileHandler::patch),
                            "/{name}/{discriminator}" bind DELETE to authenticated().then(profileHandler::delete),
                            "/" bind POST to profileHandler::post,
                        ),
                        "/minecraft" bind routes(
                            "/{name}" bind GET to minecraftProfileHandler::get,
                            "/" bind POST to minecraftProfileHandler::post,
                            "/" bind GET to minecraftProfileHandler::list
                        ),
                        "/discord" bind routes(
                            "/{id}" bind GET to discordProfileHandler::get,
                            "/" bind GET to discordProfileHandler::list
                        ),
                        "/github" bind routes(
                            "/{name}" bind GET to githubProfileHandler::get,
                            "/" bind GET to githubProfileHandler::list
                        ),
                        "/irc" bind routes(
                            "/{nick}" bind GET to ircProfileHandler::get,
                            "/" bind GET to ircProfileHandler::list
                        )
                    )
                )
            )
        ))

    private val server = app.asServer(Jetty(plugin.getWebConfig().getInt("port"))).start()

    fun start() = server.start()

}