package com.mcmlr.system.products.market

import com.google.gson.GsonBuilder
import com.mcmlr.blocks.api.Resources
import com.mcmlr.system.AppScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.io.File
import java.io.FileWriter
import java.io.InputStream
import java.util.*
import javax.inject.Inject

@AppScope
class OrderRepository @Inject constructor(
    private val resources: Resources,
    private val marketRepository: MarketRepository,
) {

    var selectedMaterial: Material? = null
    var updatingOrder: Order? = null
    var purchaseOrder: Pair<Material, Order>? = null

    fun deleteOrder(material: Material, order: Order) = flow {
        val orderStorageModel = OrderStorage(order.quantity, order.price, order.meta)
        val ordersDirectory = File(resources.dataFolder(), "Market/Orders")
        val existingOrders = File(ordersDirectory, "${order.playerId}.json")
        val gson = GsonBuilder().setPrettyPrinting().create()

        if (!existingOrders.exists()) {
            emit(OrderStatus.ERROR)
            return@flow
        }

        val playerOrdersInputStream: InputStream = existingOrders.inputStream()
        val playerOrdersInputString = playerOrdersInputStream.bufferedReader().use { it.readText() }
        val playerOrders = gson.fromJson(playerOrdersInputString, PlayerOrdersStorage::class.java)
        val materialOrders = playerOrders.orders[material]

        if (materialOrders != null) {
            val remainingOrders = materialOrders.filter { it.price != orderStorageModel.price || it.meta != orderStorageModel.meta }
            playerOrders.orders[material] = remainingOrders.toMutableList()
        } else {
            emit(OrderStatus.ERROR)
            return@flow
        }

        val orderString = gson.toJson(playerOrders)
        val orderWriter = FileWriter(existingOrders)
        orderWriter.append(orderString)
        orderWriter.close()

        marketRepository.removeOrder(material, order)

        emit(OrderStatus.DELETED)
    }.flowOn(Dispatchers.IO)

    fun updateOrder(material: Material, existingOrder: Order, updatedOrder: Order) = flow {
        val orderStorageModel = OrderStorage(existingOrder.quantity, existingOrder.price, existingOrder.meta)
        val ordersDirectory = File(resources.dataFolder(), "Market/Orders")
        val existingOrders = File(ordersDirectory, "${existingOrder.playerId}.json")
        val gson = GsonBuilder().setPrettyPrinting().create()

        if (!existingOrders.exists()) {
            emit(OrderStatus.ERROR)
            return@flow
        }

        val playerOrdersInputStream: InputStream = existingOrders.inputStream()
        val playerOrdersInputString = playerOrdersInputStream.bufferedReader().use { it.readText() }
        val playerOrders = gson.fromJson(playerOrdersInputString, PlayerOrdersStorage::class.java)
        val materialOrders = playerOrders.orders[material]

        if (materialOrders != null) {
            val matchedOrder = materialOrders.find { it.price == orderStorageModel.price && it.meta == orderStorageModel.meta }
            if (matchedOrder != null) {
                if (matchedOrder.price != updatedOrder.price) {
                    materialOrders.remove(matchedOrder)
                    val existingOrderWithNewPrice = materialOrders.find { it.price == updatedOrder.price && it.meta == orderStorageModel.meta }

                    if (existingOrderWithNewPrice != null) {
                        existingOrderWithNewPrice.quantity += updatedOrder.quantity + existingOrder.quantity
                    } else {
                        orderStorageModel.quantity += updatedOrder.quantity
                        orderStorageModel.price = updatedOrder.price
                        materialOrders.add(orderStorageModel)
                    }
                } else {
                    matchedOrder.quantity += updatedOrder.quantity
                }
            } else {
                emit(OrderStatus.ERROR)
                return@flow
            }
        } else {
            emit(OrderStatus.ERROR)
            return@flow
        }

        val orderString = gson.toJson(playerOrders)
        val orderWriter = FileWriter(existingOrders)
        orderWriter.append(orderString)
        orderWriter.close()

        marketRepository.updateOrder(material, existingOrder, updatedOrder)

        emit(OrderStatus.UPDATED)
    }.flowOn(Dispatchers.IO)

    fun setOrder(item: ItemStack, order: Order) = flow {
        val orderStorageModel = OrderStorage(order.quantity, order.price, order.meta)
        val ordersDirectory = File(resources.dataFolder(), "Market/Orders")
        val existingOrders = File(ordersDirectory, "${order.playerId}.json")
        val gson = GsonBuilder().setPrettyPrinting().create()

        if (!existingOrders.exists()) {
            val playerOrderStorage = PlayerOrdersStorage(mutableMapOf(item.type to mutableListOf(orderStorageModel)))
            val orderString = gson.toJson(playerOrderStorage)
            val orderWriter = FileWriter(existingOrders)
            orderWriter.append(orderString)
            orderWriter.close()

            marketRepository.addOrder(item.type, order)

            emit(OrderStatus.CREATED)
            return@flow
        }

        val playerOrdersInputStream: InputStream = existingOrders.inputStream()
        val playerOrdersInputString = playerOrdersInputStream.bufferedReader().use { it.readText() }
        val playerOrders = gson.fromJson(playerOrdersInputString, PlayerOrdersStorage::class.java)
        val materialOrders = playerOrders.orders[item.type]

        if (materialOrders != null) {
            val matchedOrder = materialOrders.find { it.price == orderStorageModel.price && it.meta == orderStorageModel.meta }
            if (matchedOrder != null) {
                matchedOrder.quantity += orderStorageModel.quantity
            } else {
                materialOrders.add(orderStorageModel)
            }
        } else {
            playerOrders.orders[item.type] = mutableListOf(orderStorageModel)
        }

        val orderString = gson.toJson(playerOrders)
        val orderWriter = FileWriter(existingOrders)
        orderWriter.append(orderString)
        orderWriter.close()

        marketRepository.addOrder(item.type, order)

        emit(OrderStatus.UPDATED)
    }.flowOn(Dispatchers.IO)
}

enum class OrderStatus {
    CREATED,
    UPDATED,
    DELETED,
    ERROR,
}

data class PlayerOrdersStorage(val orders: MutableMap<Material, MutableList<OrderStorage>>)

data class OrderStorage(var quantity: Int, var price: Int, var meta: String)

data class OrdersList(val orders: MutableMap<Material, MutableList<Order>>)

data class Order(val playerId: UUID, var quantity: Int, val price: Int, val meta: String) {

    class Builder {
        var playerId: UUID? = null
        var quantity: Int? = null
        var price: Int? = null
        var meta: String? = null

        fun playerId(playerId: UUID): Builder {
            this.playerId = playerId
            return this
        }

        fun quantity(quantity: Int): Builder {
            this.quantity = quantity
            return this
        }

        fun price(price: Int): Builder {
            this.price = price
            return this
        }

        fun meta(meta: String?): Builder {
            this.meta = meta
            return this
        }

        fun build(): Order? {
            return playerId?.let { playerId ->
                quantity?.let { quantity ->
                    price?.let { price ->
                        meta?.let { meta ->
                            Order(playerId, quantity, price, meta)
                        }
                    }
                }
            }
        }
    }
}
