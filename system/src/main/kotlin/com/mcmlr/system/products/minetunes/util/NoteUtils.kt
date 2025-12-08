package com.mcmlr.system.products.minetunes.util

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.log
import com.mcmlr.system.products.minetunes.nbs.data.CustomSound
import org.bukkit.Sound
import kotlin.math.pow

object NoteUtils {

    val pitches = List<Float>(2401) {
        2.0.pow((it - 1200.0) / 1200.0).toFloat()
    }

    fun pitch(key: Byte, pitch: Short): Float {
        var p = pitch + (key * 100)
        while (p < 3300) p += 1200
        while (p > 5700) p -= 1200

        p -= 3300

        return pitches[p]
    }

    fun isCustomInstrument(instrument: Byte) = instrument > 15

    fun getInstrumentName(instrument: Byte): Sound {
        when (instrument.toInt()) {
            0 -> return Sound.BLOCK_NOTE_BLOCK_HARP
            1 -> return Sound.BLOCK_NOTE_BLOCK_BASS
            2 -> return Sound.BLOCK_NOTE_BLOCK_BASEDRUM
            3 -> return Sound.BLOCK_NOTE_BLOCK_SNARE
            4 -> return Sound.BLOCK_NOTE_BLOCK_HAT
            5 -> return Sound.BLOCK_NOTE_BLOCK_GUITAR
            6 -> return Sound.BLOCK_NOTE_BLOCK_FLUTE
            7 -> return Sound.BLOCK_NOTE_BLOCK_BELL
            8 -> return Sound.BLOCK_NOTE_BLOCK_CHIME
            9 -> return Sound.BLOCK_NOTE_BLOCK_XYLOPHONE
            10 -> return Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE
            11 -> return Sound.BLOCK_NOTE_BLOCK_COW_BELL
            12 -> return Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO
            13 -> return Sound.BLOCK_NOTE_BLOCK_BIT
            14 -> return Sound.BLOCK_NOTE_BLOCK_BANJO
            15 -> return Sound.BLOCK_NOTE_BLOCK_PLING
            else -> return Sound.BLOCK_NOTE_BLOCK_HARP
        }
    }

//    fun getInstrumentName(instrument: Byte): Sound {
//        log(Log.ASSERT, "Get sound: instrument=$instrument")
//        when (instrument) {
//            0.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_HARP")
//            1.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_BASS")
//            2.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_BASEDRUM")
//            3.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_SNARE")
//            4.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_HAT")
//            5.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_GUITAR")
//            6.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_FLUTE")
//            7.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_BELL")
//            8.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_CHIME")
//            9.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_XYLOPHONE")
//            10.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_IRON_XYLOPHONE")
//            11.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_COW_BELL")
//            12.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_DIDGERIDOO")
//            13.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_BIT")
//            14.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_BANJO")
//            15.toByte() -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_PLING")
//            else -> return CustomSound.fromBukkitName("BLOCK_NOTE_BLOCK_HARP")
//        }
//    }
}