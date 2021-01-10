package com.rpkit.characters.bukkit.web.race

import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.characters.bukkit.race.RPKRaceService
import com.rpkit.characters.bukkit.web.ErrorResponse
import com.rpkit.core.service.Services
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.int

class RaceHandler {

    val idLens = Path.int().of("id")

    fun get(request: Request): Response {
        val id = idLens(request)
        val raceService = Services[RPKRaceService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Race service not found"))
        val race = raceService.getRace(id)
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Race not found"))
        return Response(OK)
            .with(RaceResponse.lens of race.toRaceResponse())
    }

    fun list(request: Request): Response {
        val raceService = Services[RPKRaceService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Race service not found"))
        val races = raceService.races
        return Response(OK)
            .with(RaceResponse.listLens of races.map(RPKRace::toRaceResponse))
    }

}