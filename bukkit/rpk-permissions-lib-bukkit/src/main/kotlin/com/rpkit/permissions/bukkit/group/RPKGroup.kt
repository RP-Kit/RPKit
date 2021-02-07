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

package com.rpkit.permissions.bukkit.group

/**
 * Represents a group.
 */
interface RPKGroup {

    /**
     * The name of the group.
     */
    val name: RPKGroupName

    /**
     * A list of permissions allowed by the group.
     */
    val allow: List<String>

    /**
     * A list of permissions denied by the group.
     */
    val deny: List<String>

    /**
     * A list of groups permissions are inherited from.
     */
    val inheritance: List<RPKGroup>

}