package com.mcmlr.system.products.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import javax.inject.Inject

class MaterialsRepository @Inject constructor() {
    private var materials = Material.entries.filter { it.isItem }.map { ItemStack(it) }.toTypedArray()

    fun setInventory(inventory: Inventory) {
        val itemList = inventory.filterNotNull()
        val nonDuplicatedItemList = mutableListOf<ItemStack>()
        itemList.forEach {
            if (!nonDuplicatedItemList.contains(it)) nonDuplicatedItemList.add(it)
        }

        materials = nonDuplicatedItemList.toTypedArray()
    }

    fun resetInventory() {
        materials = Material.entries.filter { it.isItem }.map { ItemStack(it) }.toTypedArray()
    }

    fun materialsStream() = flow {
        emit(materials.filter { materialItem ->
            materialItem.type.isItem
        })
    }

    fun searchMaterialsStream(searchTerm: String): Flow<List<ItemStack>> {
        return if (searchTerm.isEmpty()) {
            materialsStream()
        } else {
            materialsStream().map { materials ->
                materials.filter { material ->
                    material.type.name
                        .replace("_", " ")
                        .lowercase()
                        .contains(searchTerm.lowercase())
                }
            }
        }
    }
}