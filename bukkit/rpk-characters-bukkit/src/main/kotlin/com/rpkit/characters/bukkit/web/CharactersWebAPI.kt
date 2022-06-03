/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.characters.bukkit.web

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.web.character.CharacterHandler
import com.rpkit.characters.bukkit.web.race.RaceHandler
import org.http4k.core.Method.*
import org.http4k.core.then
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer

class CharactersWebAPI(plugin: RPKCharactersBukkit) {

    private val characterHandler = CharacterHandler(plugin)
    private val raceHandler = RaceHandler()
    private val app = ServerFilters.CatchAll()
        .then(ServerFilters.Cors(CorsPolicy.UnsafeGlobalPermissive))
        .then(routes(
            "/characters" bind routes(
                "/api" bind routes(
                    "/v2" bind routes(
                        "/character" bind routes(
                            "/{id}" bind GET to characterHandler::get,
                            "/{id}" bind PUT to authenticated().then(characterHandler::put),
                            "/{id}" bind PATCH to authenticated().then(characterHandler::patch),
                            "/{id}" bind DELETE to authenticated().then(characterHandler::delete),
                            "/" bind POST to authenticated().then(characterHandler::post),
                            "/" bind GET to characterHandler::list
                        ),
                        "/race" bind routes(
                            "/" bind GET to raceHandler::list
                        )
                    )
                )
            )
        ))

    private val server = app.asServer(Jetty(plugin.getWebConfig().getInt("port"))).start()

    fun start() = server.start()


}