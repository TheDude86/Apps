package com.mcmlr.blocks.api.app

import com.mcmlr.apps.app.block.data.Bundle
import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.api.block.Block
import com.mcmlr.blocks.api.block.Context
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.core.FlowDisposer
import io.netty.channel.Channel
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraft.network.Connection
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_21_R5.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import java.util.*

abstract class BaseApp(val player: Player): FlowDisposer(), Context {

    protected lateinit var head: Block
    protected lateinit var origin: Location
    protected lateinit var parentEnvironment: BaseEnvironment<BaseApp>

    protected var parentApp: BaseApp? = null
    protected var deeplink: String? = null

    lateinit var resources: Resources

    abstract fun root(): Block

    fun create(resources: Resources) {
        this.resources = resources
        onCreate()
        head = root()
        head.context = this
        head.onCreate()

        foo(player)
    }

    lateinit var channel: Channel

    fun foo(player: Player) {
        val handle = (player as CraftPlayer).handle
        val playerConnection = handle.connection
        val connection = playerConnection.javaClass.getField("connection").get(playerConnection) as Connection
        channel = connection.channel

        val id = "3d7a9576-6498-4671-b725-d811b28e3dce"
        player.spectatorTarget = Bukkit.getEntity(UUID.fromString(id))
        Bukkit.getEntity(UUID.fromString(id))?.addPassenger(player)

        channel.pipeline().addBefore("packet_handler", "Apps", object : ChannelDuplexHandler() {
            override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {

                val movePlayerPacket = msg as? ServerboundMovePlayerPacket
                if (movePlayerPacket != null) {
                    log(Log.ASSERT, "Move Player = ${movePlayerPacket.xRot}, ${movePlayerPacket.yRot}")
                } else {
                    log(Log.ASSERT, "Other Packet = $msg")
                }



//                ClientboundMoveEntityPacket

                super.channelRead(ctx, msg)
            }

            override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {
//                log(Log.DEBUG, "Write = $msg")

                super.write(ctx, msg, promise)
            }
        })
    }

    override fun deeplink(): String? = deeplink

    fun registerEvents(eventHandler: Listener) {
        resources.server().pluginManager.registerEvents(eventHandler, resources.plugin())
    }

    override fun close() {
        onClose()
        head.onClose()
        clear()

        bar()
    }

    fun bar() {
        channel.eventLoop().submit {
            channel.pipeline().remove("Apps")
        }
    }

    override fun minimize() {
        head.onPause()
    }

    override fun maximize() {
        head.onResume(player.eyeLocation.clone())
    }

    override fun onPause() {}

    override fun onResume(newOrigin: Location?) {}

    override fun onClose() {
        clear()
    }

    override fun setHeadBlock(head: Block) {
        this.head = head
        this.head.context = this
    }

    override fun hasParent(): Boolean = false

    override fun routeTo(block: Block, callback: ((Bundle) -> Unit)?) {
        block.parent = head
        head.onClose()
        head = block
        head.context = this

        block.setResultCallback(callback)
        block.onCreate()
    }

    override fun routeBack() {
        val parent = head.parent
        if (parent == null) {
            close()
        } else {
            parent.onCreate()
            head.onClose()
            head = parent
        }
    }

    override fun getBlock(): Block = head
}
