package com.mcmlr.system.products.market

import com.google.gson.GsonBuilder
import com.mcmlr.blocks.api.Resources
import com.mcmlr.blocks.core.*
import com.mcmlr.system.dagger.EnvironmentScope
import com.mcmlr.system.products.data.VaultRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.util.*
import javax.inject.Inject

data class OrderQueueModel(val player: Player, val material: Material, val order: Order, val onFinish: (Boolean) -> Unit)

@EnvironmentScope
class MarketRepository @Inject constructor(
    private val resources: Resources,
    private val vaultRepository: VaultRepository,
) {
    private val market = OrdersList(mutableMapOf())
    private val ordersQueue = MutableSharedFlow<OrderQueueModel>()

    init {
        ordersQueue
            .collectOn(Dispatchers.IO)
            .collectLatest { request ->
                val materialOrders = market.orders[request.material]
                if (materialOrders == null) {
                    CoroutineScope(DudeDispatcher()).launch { //TODO: Folia Test code
                        request.onFinish.invoke(false)
                    }
                    return@collectLatest
                }

                val order = materialOrders.find {
                    (it.playerId == request.order.playerId) &&
                            (it.price == request.order.price) &&
                            (it.meta == request.order.meta)
                }
                if (order == null || order.quantity < request.order.quantity) {
                    CoroutineScope(DudeDispatcher()).launch { //TODO: Folia Test code
                        request.onFinish.invoke(false)
                    }
                    return@collectLatest
                }

                order.quantity -= request.order.quantity
                if (order.quantity == 0) {
                    materialOrders.remove(order)
                }

                updateOrderFile(request.material, request.order)

                CoroutineScope(DudeDispatcher()).launch { //TODO: Folia Test code
                    val cost = (request.order.quantity * request.order.price) / 100.0
                    vaultRepository.economy?.withdrawPlayer(request.player, cost)
                    vaultRepository.economy?.depositPlayer(Bukkit.getOfflinePlayer(request.order.playerId), cost)

//                    @Suppress("DEPRECATION") val key = if (checkVersion("1.21.5-R0.1-SNAPSHOT")) {
//                        request.material.keyOrNull
//                    } else {
//                        request.material.key
//                    }

                    @Suppress("DEPRECATION") val key = request.material.key

                    val item = Bukkit.getItemFactory().createItemStack("$key${request.order.meta}")
                    item.amount = request.order.quantity
                    request.player.inventory.add(request.player.location, item)

                    val message = "${ChatColor.DARK_GREEN}${ChatColor.BOLD}[Market] ${ChatColor.GREEN}${request.player.displayName} just bought ${request.order.quantity} ${request.material.name.fromMCItem()} from you!  $${"%.2f".format(cost)} has been added to you balance."

                    Bukkit.getOnlinePlayers()
                        .find { it.uniqueId == order.playerId }
                        ?.sendMessage(message)

                    request.onFinish.invoke(true)
                }
            }
    }

    fun queuePurchase(player: Player, material: Material, order: Order, onFinish: (Boolean) -> Unit) {
        ordersQueue.emitBackground(OrderQueueModel(player, material, order, onFinish))
    }

    private fun updateOrderFile(material: Material, order: Order) {
        val ordersDirectory = File(resources.dataFolder(), "Market/Orders")
        val existingOrders = File(ordersDirectory, "${order.playerId}.json")
        val gson = GsonBuilder().setPrettyPrinting().create()

        val playerOrdersInputStream: InputStream = existingOrders.inputStream()
        val playerOrdersInputString = playerOrdersInputStream.bufferedReader().use { it.readText() }
        val playerOrders = gson.fromJson(playerOrdersInputString, PlayerOrdersStorage::class.java)
        val materialOrders = playerOrders.orders[material] ?: return
        val matchedOrder = materialOrders.find { it.price == order.price && it.meta == order.meta } ?: return
        if (matchedOrder.quantity == order.quantity) {
            materialOrders.remove(matchedOrder)
        } else {
            matchedOrder.quantity -= order.quantity
        }

        val orderString = gson.toJson(playerOrders)
        val orderWriter = FileWriter(existingOrders)
        orderWriter.append(orderString)
        orderWriter.close()
    }

    fun loadOrders() {
        val ordersDirectory = File(resources.dataFolder(), "Market/Orders")
        ordersDirectory.listFiles()?.forEach { userOrdersFile ->
            val gson = GsonBuilder().setPrettyPrinting().create()
            val playerOrdersInputStream: InputStream = userOrdersFile.inputStream()
            val playerOrdersInputString = playerOrdersInputStream.bufferedReader().use { it.readText() }
            val playerOrders = gson.fromJson(playerOrdersInputString, PlayerOrdersStorage::class.java)

            playerOrders.orders.forEach { (key, value) ->
                val orders = value.map {
                    Order(
                        UUID.fromString(userOrdersFile.nameWithoutExtension),
                        it.quantity,
                        it.price,
                        it.meta
                    )
                }
                val existingOrders = market.orders[key]
                if (existingOrders != null) {
                    existingOrders.addAll(orders)
                } else {
                    market.orders[key] = orders.toMutableList()
                }
            }
        }
    }

    fun updateOrder(material: Material, existingOrder: Order, updatedOrder: Order) {
        val materialOrders = market.orders[material] ?: return
        if (existingOrder.price != updatedOrder.price) {
            market.orders[material] = materialOrders.filter { it.playerId != existingOrder.playerId || it.price != existingOrder.price }.toMutableList()
            val existingOrderWithNewPrice = materialOrders.find { it.playerId == existingOrder.playerId && it.price == updatedOrder.price }
            if (existingOrderWithNewPrice != null) {
                existingOrderWithNewPrice.quantity += existingOrder.quantity + updatedOrder.quantity
            } else {
                val newOrder = Order(
                    updatedOrder.playerId,
                    existingOrder.quantity + updatedOrder.quantity,
                    updatedOrder.price,
                    updatedOrder.meta
                )
                market.orders[material]?.add(newOrder)
            }
        } else {
            val order = materialOrders.find { it.price == existingOrder.price && it.meta == existingOrder.meta } ?: return
            order.quantity += updatedOrder.quantity
        }
    }

    fun removeOrder(material: Material, order: Order) {
        val materialOrders = market.orders[material]
        if (materialOrders != null) {
            market.orders[material] = materialOrders.filter {
                it.playerId != order.playerId ||
                        it.price != order.price ||
                        it.meta != order.meta
            }.toMutableList()
        }
    }

    fun addOrder(material: Material, order: Order) {
        val materialOrders = market.orders[material]
        if (materialOrders != null) {
            val playerMaterialOrder = materialOrders.find {
                it.playerId == order.playerId &&
                        it.price == order.price &&
                        it.meta == order.meta
            }
            if (playerMaterialOrder != null) {
                playerMaterialOrder.quantity += order.quantity
            } else {
                materialOrders.add(order)
            }
        } else {
            market.orders[material] = mutableListOf(order)
        }
    }

    fun getMyOrders(player: Player): List<Pair<Material, Order>> = market.orders.entries
        .map { map ->
            map.value
                .filter { it.playerId == player.uniqueId }
                .map { Pair(map.key, it) }
        }.flatten()

    fun getOrders(filter: String? = null): List<Pair<Material, Order>> {
        if (filter == null) return market.orders.entries.map { map -> map.value.map { Pair(map.key, it) } }.flatten()

        return market.orders.entries
            .mapNotNull { map ->
                if (map.key.name.lowercase().contains(filter.lowercase())) {
                    map.value.sortedBy { it.price }.map { Pair(map.key, it) }
                }
                else {
                    null
                }
            }.flatten()
    }
}