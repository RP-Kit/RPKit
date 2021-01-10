package com.rpkit.characters.bukkit.web.race

import com.rpkit.characters.bukkit.race.RPKRace
import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class RaceResponse(
    val id: Int,
    val name: String
) {
    companion object {
        val lens = Body.auto<RaceResponse>().toLens()
        val listLens = Body.auto<List<RaceResponse>>().toLens()
    }
}

fun RPKRace.toRaceResponse() = RaceResponse(
    id ?: 0,
    name
)