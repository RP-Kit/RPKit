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

package com.rpkit.core.service

/**
 * Provides services
 */
interface Service {
    /*
    ServicesDelegate implementations may use this in a number of ways
    As Service is implementation-agnostic, it doesn't want to have awareness of the plugin implementations.
    We also don't want to force plugins to implement RPKPlugin, as this means optional RPKit functionality
    cannot be achieved (the plugin would fail to load as it couldn't find the interface when RPKit is not installed)
    Therefore, as nasty as it may be, using an Any typing here allows us to be implementation-agnostic and not force
    plugins to have RPKit awareness.
    */
    val plugin: Any
}
