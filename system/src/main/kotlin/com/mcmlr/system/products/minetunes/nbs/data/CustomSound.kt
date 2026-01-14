package com.mcmlr.system.products.minetunes.nbs.data

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.log
import org.bukkit.Sound

private object CustomSoundHelper {
    var cachedSound: Sound? = null
    val cachedSoundMap = mutableMapOf<String, Sound>()

    init {
        CustomSound.entries.forEach { customSound ->
            customSound.versionDependentNames.forEach versionLoop@ { versionSoundName ->
                val sound = customSound.getSound() ?: return@versionLoop
                log(Log.ERROR, "Sound=$sound")
                cachedSoundMap.put(versionSoundName.uppercase(), sound)
            }
        }
    }
}

enum class CustomSound(vararg val versionDependentNames: String) {
    NOTE_PIANO("NOTE_PIANO", "BLOCK_NOTE_HARP", "BLOCK_NOTE_BLOCK_HARP"),
    NOTE_BASS("NOTE_BASS", "BLOCK_NOTE_BASS", "BLOCK_NOTE_BLOCK_BASS"),
    NOTE_BASS_DRUM("NOTE_BASS_DRUM", "BLOCK_NOTE_BASEDRUM", "BLOCK_NOTE_BLOCK_BASEDRUM"),
    NOTE_SNARE_DRUM("NOTE_SNARE_DRUM", "BLOCK_NOTE_SNARE", "BLOCK_NOTE_BLOCK_SNARE"),
    NOTE_STICKS("NOTE_STICKS", "BLOCK_NOTE_HAT", "BLOCK_NOTE_BLOCK_HAT"),
    NOTE_BASS_GUITAR("NOTE_BASS_GUITAR", "BLOCK_NOTE_GUITAR", "BLOCK_NOTE_BLOCK_GUITAR"),
    NOTE_FLUTE("NOTE_FLUTE", "BLOCK_NOTE_FLUTE", "BLOCK_NOTE_BLOCK_FLUTE"),
    NOTE_BELL("NOTE_BELL", "BLOCK_NOTE_BELL", "BLOCK_NOTE_BLOCK_BELL"),
    NOTE_CHIME("NOTE_CHIME", "BLOCK_NOTE_CHIME", "BLOCK_NOTE_BLOCK_CHIME"),
    NOTE_XYLOPHONE("NOTE_XYLOPHONE", "BLOCK_NOTE_XYLOPHONE", "BLOCK_NOTE_BLOCK_XYLOPHONE"),
    NOTE_PLING("NOTE_PLING", "BLOCK_NOTE_PLING", "BLOCK_NOTE_BLOCK_PLING"),
    NOTE_IRON_XYLOPHONE("BLOCK_NOTE_BLOCK_IRON_XYLOPHONE"),
    NOTE_COW_BELL("BLOCK_NOTE_BLOCK_COW_BELL"),
    NOTE_DIDGERIDOO("BLOCK_NOTE_BLOCK_DIDGERIDOO"),
    NOTE_BIT("BLOCK_NOTE_BLOCK_BIT"),
    NOTE_BANJO("BLOCK_NOTE_BLOCK_BANJO");

    companion object {
        fun fromBukkitName(bukkitSound: String): Sound = CustomSoundHelper.cachedSoundMap[bukkitSound.uppercase()] ?: Sound.valueOf(bukkitSound) //TODO: Update deprecated method
    }

    fun getSound(): Sound? {
        CustomSoundHelper.cachedSound?.let { return it }
        versionDependentNames.forEach {
            try {
                CustomSoundHelper.cachedSound = Sound.valueOf(it)
                return CustomSoundHelper.cachedSound
            } catch (_: IllegalArgumentException) {
                //Do nothing
            }
        }

        return null
    }
}