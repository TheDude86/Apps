package com.mcmlr.blocks.api.block

import org.bukkit.Location

interface Presenter {
    fun updateOrigin(origin: Location)

    fun render()

    fun createView()
}