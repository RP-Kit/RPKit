package com.seventh_root.elysium.api.character

import com.seventh_root.elysium.core.service.ServiceProvider


interface CharacterCardFieldProvider: ServiceProvider {

    val characterCardFields: MutableList<CharacterCardField>

}