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

package com.rpkit.characters.bukkit.character.field

import com.rpkit.core.service.ServiceProvider

/**
 * Represents a character card field provider.
 * Character card fields may be added to the list of character card fields in order to allow display on character cards.
 * Each character card field available from the character card field provider is usable on character cards where the
 * character card implementation permits usage of such variables.
 */
interface RPKCharacterCardFieldProvider: ServiceProvider {

    /**
     * A list of all character card fields.
     * In order to make character card fields usable on character cards, they must be added to this.
     */
    val characterCardFields: MutableList<CharacterCardField>

}