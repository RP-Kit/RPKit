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

class ServiceRequirement<T: Service>(val type: Class<T>) {

    fun whenAvailable(action: ServiceRequirementAction<T>): ServiceRequirement<T> {
        val service = Services[type]
        if (service != null) {
            action(service)
        } else {
            Services.addServiceReadyFunction(ServiceReadyFunction(type, action))
        }
        return this
    }

    fun ifAvailable(action: ServiceRequirementAction<T>): ServiceRequirement<T> {
        val service = Services[type]
        if (service != null) {
            action(service)
        }
        return this
    }

    fun ifNotAvailable(function: () -> Unit): ServiceRequirement<T> {
        val service = Services[type]
        if (service == null) {
            function()
        }
        return this
    }

}