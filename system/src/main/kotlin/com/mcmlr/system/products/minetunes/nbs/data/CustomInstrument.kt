package com.mcmlr.system.products.minetunes.nbs.data

import org.bukkit.Sound

data class CustomInstrument(val index: Byte, val name: String, val soundFileName: String) {

    val sound: Sound?

    init {
        if (soundFileName.lowercase() == "pling" || soundFileName.lowercase() == "block.note_block.pling") {
            sound = CustomSound.NOTE_PLING.getSound()
        } else {
            sound = null
        }
    }
}