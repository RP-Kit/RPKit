package com.seventh_root.elysium.api.player

import com.seventh_root.elysium.core.database.TableRow

interface ElysiumPlayer : TableRow {

    val name: String

}
