package com.rpkit.locks.bukkit.lock

import com.rpkit.core.database.Entity
import org.bukkit.block.Block


class RPKLockedBlock(
        override var id: Int = 0,
        val block: Block
) : Entity