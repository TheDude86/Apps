package com.mcmlr.apps

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.*
import com.comphenix.protocol.wrappers.ChunkCoordIntPair
import com.mcmlr.blocks.api.CursorEvent
import com.mcmlr.blocks.api.CursorModel
import com.mcmlr.blocks.api.data.CursorRepository
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PacketManager @Inject constructor(
    private val cursorRepository: CursorRepository,
) {
    fun initListeners() {
        val manager = ProtocolLibrary.getProtocolManager()

//        manager.addPacketListener(object : PacketAdapter(Apps.instance, PacketType.Play.Server.ENTITY_METADATA, PacketType.Play.Server.ENTITY_HEAD_ROTATION) {
//            override fun onPacketSending(event: PacketEvent?) {
//                val packet = event?.packet ?: return
//                val id = packet.integers.read(0)
//                val uuid = Apps.ENTITY_SET[id] ?: return
////                if (event.player.uniqueId != uuid) event.isCancelled = true
//            }
//        })

//        manager.addPacketListener(object : PacketAdapter(Apps.instance, ListenerPriority.HIGHEST, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
//            override fun onPacketSending(e: PacketEvent?) {
//                log(Log.ASSERT, "Cursor Moved ${e == null}")
//                val event = e ?: return
//                cursorRepository.updateStream(CursorModel(event.player.uniqueId, event.player.eyeLocation, CursorEvent.MOVE))
//            }
//        })

//        manager.addPacketListener(object : PacketAdapter(Apps.instance, PacketType.Play.Server.REL_ENTITY_MOVE_LOOK) {
//            override fun onPacketSending(e: PacketEvent?) {
//                log(Log.ASSERT, "Rel entity look")
//                val event = e ?: return
//                cursorRepository.updateStream(CursorModel(event.player.uniqueId, event.player.eyeLocation, CursorEvent.MOVE))
//            }
//        })
    }

    fun unload(x: Int, z: Int): PacketContainer {
        val chunk = PacketContainer(PacketType.Play.Server.UNLOAD_CHUNK)
        chunk.chunkCoordIntPairs.write(0, ChunkCoordIntPair(0, 0))

        return chunk
    }
}
