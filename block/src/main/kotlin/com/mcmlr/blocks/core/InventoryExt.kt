package com.mcmlr.blocks.core

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

fun Inventory.add(location: Location, material: Material, quantity: Int) {
    var q = quantity
    while (q > 64) {
        val drops = addItem(ItemStack(material, 64))
        drops.values.forEach {
            location.world?.dropItem(location, it)
        }

        q -= 64
    }

    if (q > 0) {
        val drops = addItem(ItemStack(material, q))
        drops.values.forEach {
            location.world?.dropItem(location, it)
        }
    }
}

fun Inventory.add(location: Location, item: ItemStack) {
    var q = item.amount
    while (q > 64) {
        val temp = item.clone()
        temp.amount = 64
        val drops = addItem(temp)
        drops.values.forEach {
            location.world?.dropItem(location, it)
        }

        q -= 64
    }

    if (q > 0) {
        val temp = item.clone()
        temp.amount = q
        val drops = addItem(temp)
        drops.values.forEach {
            location.world?.dropItem(location, it)
        }
    }
}

fun Inventory.remove(material: Material, meta: String?, quantity: Int) {
    var remaining = quantity
    forEach {
        if (it != null && it.type == material && it.itemMeta?.asComponentString == meta) {
            if (remaining > it.amount) {
                remaining -= it.amount
                it.amount = 0
            } else {
                it.amount -= remaining
                return
            }
        }
    }
}

fun Inventory.remove(material: Material, quantity: Int) {
    var remaining = quantity
    forEach {
        if (it != null && it.type == material) {
            if (remaining > it.amount) {
                remaining -= it.amount
                it.amount = 0
            } else {
                it.amount -= remaining
                return
            }
        }
    }
}
