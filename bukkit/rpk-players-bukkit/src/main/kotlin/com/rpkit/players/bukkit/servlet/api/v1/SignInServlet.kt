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

package com.rpkit.players.bukkit.servlet.api.v1

import com.google.gson.Gson
import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_OK
import javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED


class SignInServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/api/v1/signin"

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val gson = Gson()

        val name = req.getParameter("name")
        val password = req.getParameter("password")
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = profileProvider.getProfile(name)
        if (profile != null) {
            if (profile.checkPassword(password.toCharArray())) {
                profileProvider.setActiveProfile(req, profile)
                resp.contentType = "application/json"
                resp.status = SC_OK
                resp.writer.write(
                        gson.toJson(
                                mapOf(
                                        Pair("message", "Success.")
                                )
                        )
                )
            } else {
                resp.contentType = "application/json"
                resp.status = SC_UNAUTHORIZED
                resp.writer.write(
                        gson.toJson(
                                mapOf(
                                        Pair("message", "Failure.")
                                )
                        )
                )
            }
        } else {
            resp.contentType = "application/json"
            resp.status = SC_UNAUTHORIZED
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Failure.")
                            )
                    )
            )
        }
    }

}