package com.mcmlr.system.products.minetunes.nbs

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.log
import com.mcmlr.system.products.minetunes.nbs.data.CustomInstrument
import com.mcmlr.system.products.minetunes.nbs.data.Layer
import com.mcmlr.system.products.minetunes.nbs.data.Note
import com.mcmlr.system.products.minetunes.nbs.data.Song
import org.bukkit.Bukkit
import java.io.DataInputStream
import java.io.EOFException
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

object NBSDecoder {

    fun parse(inputStream: InputStream): Song? {
        val layerMap = mutableMapOf<Int, Layer>()
        var isStereo = false
        try {
            val input = DataInputStream(inputStream)
            var length = decodeShort(input)
            val nbsVersion = if (length == 0.toShort()) input.readByte() else 0
            var firstCustomInstrument = if (length == 0.toShort()) input.readByte() else 10

            length = if (nbsVersion > 2) decodeShort(input) else length

            var firstCustomInstrumentDiff = 16 - firstCustomInstrument
            val height = decodeShort(input)
            val title = decodeString(input)
            val nbsArtist = decodeString(input)
            val artist = decodeString(input)
            val description = decodeString(input)
            val speed = decodeShort(input) / 100f
            input.readBoolean() //Auto save
            input.readByte() //Auto save duration
            input.readByte() //Time signature
            decodeInt(input) //Time spent on project (minutes)
            decodeInt(input) //Left clicks
            decodeInt(input) //Right clicks
            decodeInt(input) //Blocks added
            decodeInt(input) //Blocks removed
            decodeString(input) //File name

            if (nbsVersion > 3) {
                input.readByte() //Loop song
                input.readByte() //Max loops
                decodeShort(input) //Loop start tick
            }

            if (length == 0.toShort()) {
                input.readByte()
            }

            var tick = (-1).toShort()
            while (true) {
                val jumpTicks = decodeShort(input)
                if (jumpTicks == 0.toShort()) break

                tick = (tick + jumpTicks).toShort()

                var layerIndex = -1
                while (true) {
                    val jumpLayers = decodeShort(input)
                    if (jumpLayers == 0.toShort()) break
                    layerIndex += jumpLayers

                    var instrument = input.readByte()
                    if (firstCustomInstrumentDiff > 0 && instrument >= firstCustomInstrument) {
                        instrument = (instrument + firstCustomInstrumentDiff).toByte()
                    }

                    val key = input.readByte()
                    var velocity: Byte = 100
                    var panning = 100
                    var pitch: Short = 0
                    if (nbsVersion > 3) {
                        velocity = input.readByte()
                        panning = 200 - input.readUnsignedByte()
                        pitch = decodeShort(input)
                    }

                    isStereo = panning != 100

                    val layer = layerMap[layerIndex]
                    if (layer == null) {
                        layerMap[layerIndex] = Layer()
                    }

                    layer?.notesMap[tick.toInt()] = Note(instrument, key, velocity, panning, pitch)
                }
            }

            if (nbsVersion in 1..2) {
                length = tick
            }

            repeat(height.toInt()) {
                val layer = layerMap[it]
                val name = decodeString(input)
                if (nbsVersion > 3) input.readByte() //Layer lock
                val volume = input.readByte()
                val panning = if (nbsVersion > 1) 200 - input.readUnsignedByte() else 100
                isStereo = panning != 100

                if (layer != null) {
                    layer.name = name
                    layer.volume = volume
                    layer.panning = panning
                }
            }

            val customInstrumentCount = input.readByte().toInt()
            val customInstrumentsList = mutableListOf<CustomInstrument>()
            repeat(customInstrumentCount) {
                customInstrumentsList.add(CustomInstrument(it.toByte(), decodeString(input), decodeString(input)))
                input.readByte() //Pitch
                input.readByte() //Key
            }

            if (firstCustomInstrumentDiff > -1) firstCustomInstrument = (firstCustomInstrument + firstCustomInstrumentDiff).toByte()

            return Song(
                title,
                nbsArtist,
                artist,
                description,
                layerMap,
                height,
                length,
                speed,
                20 / speed,
                customInstrumentsList,
                firstCustomInstrument.toInt(),
                isStereo,
                File(""), //TODO: Update file
            )
        } catch (_: FileNotFoundException) {
            return null
        } catch (_: EOFException) {
            Bukkit.getServer().consoleSender.sendMessage("Song is corrupted")
            return null
        } catch (_: IOException) {
            return null
        }
    }

    private fun decodeShort(input: DataInputStream): Short {
        val first = input.readUnsignedByte()
        val second = input.readUnsignedByte()

        return (first + (second shl 8)).toShort()
    }

    private fun decodeString(input: DataInputStream): String {
        val builder = StringBuilder(decodeInt(input))
        repeat(builder.capacity()) {
            var c = input.read().toChar()
            if (c == 0x0D.toChar()) {
                c = ' '
            }

            builder.append(c)
        }

        return builder.toString()
    }

    private fun decodeInt(input: DataInputStream): Int {
        val first: Int = input.readUnsignedByte()
        val second: Int = input.readUnsignedByte()
        val third: Int = input.readUnsignedByte()
        val fourth: Int = input.readUnsignedByte()
        return (first + (second shl 8) + (third shl 16) + (fourth shl 24))
    }
}