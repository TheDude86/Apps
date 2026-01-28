package com.mcmlr

import NMSFactory
import PaperFactory
import SpigotFactory

val packetFactory: NMSFactory = if (isPaper()) PaperFactory() else SpigotFactory()

private fun isPaper(): Boolean {
    try {
        Class.forName("com.destroystokyo.paper.ParticleBuilder")
        return true
    } catch (_: ClassNotFoundException) {
        return false
    }
}