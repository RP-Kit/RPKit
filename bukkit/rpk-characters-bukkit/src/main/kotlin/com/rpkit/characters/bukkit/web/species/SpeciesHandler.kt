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

package com.rpkit.characters.bukkit.web.species

import com.rpkit.characters.bukkit.species.RPKSpeciesService
import com.rpkit.characters.bukkit.web.ErrorResponse
import com.rpkit.core.service.Services
import org.http4k.core.Body
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.Gson.auto

class SpeciesHandler {

    val responseLens = Body.auto<List<String>>().toLens()

    fun list(request: Request): Response {
        val speciesService = Services[RPKSpeciesService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Species service not found"))
        val speciesList = speciesService.species
        return Response(OK)
            .with(responseLens of speciesList.map { species -> species.name.value })
    }

}