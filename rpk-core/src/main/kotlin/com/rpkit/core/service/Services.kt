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

package com.rpkit.core.service

object Services {

    lateinit var delegate: ServicesDelegate

    operator fun <T: Service> get(type: Class<T>) = delegate[type]

    operator fun <T: Service> set(type: Class<T>, service: T) {
        delegate[type] = service
        val iterator = serviceReadyFunctions.iterator()
        while (iterator.hasNext()) {
            val requirement = iterator.next()
            if (requirement.type == type) {
                val serviceRequirement = requirement as ServiceReadyFunction<T>
                serviceRequirement.action(service)
                iterator.remove()
            }
        }
    }

    private val serviceReadyFunctions = mutableListOf<ServiceReadyFunction<out Service>>()

    fun <T: Service> require(type: Class<T>) = ServiceRequirement(type)
    internal fun addServiceReadyFunction(function: ServiceReadyFunction<out Service>) {
        serviceReadyFunctions.add(function)
    }

}