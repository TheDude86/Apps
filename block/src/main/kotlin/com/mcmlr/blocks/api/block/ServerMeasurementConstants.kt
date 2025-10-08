package com.mcmlr.blocks.api.block

import com.mcmlr.blocks.api.Versions
import com.mcmlr.blocks.api.serverVersion

fun getMeasurements(): ServerMeasurementConstants = when (serverVersion()) {
    Versions.V1_21_3,
    Versions.V1_21_4,
    Versions.V1_21_5,
    Versions.V1_21_6,
    Versions.V1_21_7,
    Versions.V1_21_8,
    Versions.V1_21_9,
    Versions.V1_21_10, -> Server1213Measurements
    else -> Server1211Measurements
}


object Server1213Measurements: ServerMeasurementConstants {
    override val containerXOffset: Float
        get() = -0.0000109575f
    override val containerYOffset: Float
        get() = -0.000125f
    override val containerWidth: Float
        get() = 0.00091f
    override val containerHeight: Float
        get() = 0.001f
    override val textYOffset: Float
        get() = -0.000135f
    override val itemDimension: Float
        get() = 0.00025f
}

object Server1211Measurements: ServerMeasurementConstants {
    override val containerXOffset: Float
        get() = -0.0000109575f
    override val containerYOffset: Float
        get() = -0.000125f
    override val containerWidth: Float
        get() = 0.00091f
    override val containerHeight: Float
        get() = 0.000909f
    override val textYOffset: Float
        get() = -0.000135f
    override val itemDimension: Float
        get() = 0.00025f
}

interface ServerMeasurementConstants {
    val containerXOffset: Float
    val containerYOffset: Float
    val containerWidth: Float
    val containerHeight: Float
    val textYOffset: Float
    val itemDimension: Float
}