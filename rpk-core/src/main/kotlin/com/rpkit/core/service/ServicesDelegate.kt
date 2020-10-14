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

import kotlin.reflect.KClass

/**
 * Implemented by platforms to provide services.
 */
interface ServicesDelegate {
    /**
     * Gets the service from the platform.
     *
     * @param type The type of the service
     * @return The service
     */
    operator fun <T: Service> get(type: KClass<T>): T?
    operator fun <T: Service> set(type: KClass<T>, service: T)

}