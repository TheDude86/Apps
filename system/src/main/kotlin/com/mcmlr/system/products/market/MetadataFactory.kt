package com.mcmlr.system.products.market

import com.mcmlr.blocks.api.Log
import com.mcmlr.blocks.api.app.R
import com.mcmlr.blocks.api.log
import com.mcmlr.blocks.core.fromMCItem
import com.mcmlr.blocks.core.titlecase
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ArmorMeta
import org.bukkit.inventory.meta.AxolotlBucketMeta
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.BookMeta
import org.bukkit.inventory.meta.BundleMeta
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.MusicInstrumentMeta
import org.bukkit.inventory.meta.OminousBottleMeta
import org.bukkit.inventory.meta.PotionMeta

object MetadataFactory {
    fun getMetadataStrings(player: Player, meta: ItemMeta): List<String> {
        val metaList = mutableListOf<String>()
        if (meta.hasDisplayName()) metaList.add(meta.displayName)

        if (meta.hasLore()) {
            val loreBuilder = StringBuilder()
            meta.lore?.forEach { line ->
                loreBuilder.append("$line, ")
            }
            val lore = loreBuilder.toString()
            if (lore.isNotEmpty()) metaList.add(lore.substring(0..<lore.length - 2))
        }

        val enchantmentBuilder = StringBuilder()
        meta.enchants.forEach {
            val enchantment = it.key.name.fromMCItem()
            val level = when(it.value) {
                5 -> "V"
                4 -> "IV"
                3 -> "III"
                2 -> "II"
                else -> "I"
            }

            enchantmentBuilder.append("$enchantment $level, ")
        }
        val enchantments = enchantmentBuilder.toString()
        if (enchantments.isNotEmpty()) metaList.add(enchantments.substring(0..<enchantments.length - 2))

        when (meta) {
            is ArmorMeta -> addArmorMeta(player, meta, metaList)
            is AxolotlBucketMeta -> addAxolotlBucketMeta(player, meta, metaList)
            is BannerMeta -> {}
            is BookMeta -> addBookMeta(player, meta, metaList)
            is BundleMeta -> addBundleMeta(meta, metaList)
            is Damageable -> addDamageableMeta(player, meta, metaList)
            is MusicInstrumentMeta -> addMusicInstrumentMeta(meta, metaList)
            is OminousBottleMeta -> addOminousBottleMeta(player, meta, metaList)
            is PotionMeta -> addPotionMeta(meta, metaList)
        }

        return metaList
    }

    private fun addArmorMeta(player: Player, meta: ArmorMeta, metaList: MutableList<String>) {
        val trim = meta.trim ?: return
        metaList.add(R.getString(player, S.PURCHASE_TRIM_TEMPLATE.resource(), trim.pattern.toString().titlecase(), trim.material.toString().fromMCItem()))
    }

    private fun addAxolotlBucketMeta(player: Player, meta: AxolotlBucketMeta, metaList: MutableList<String>) {
        metaList.add(R.getString(player, S.PURCHASE_COLOR_TEMPLATE.resource(), meta.variant.toString().titlecase()))
    }

    private fun addBookMeta(player: Player, meta: BookMeta, metaList: MutableList<String>) {
        if (!meta.hasTitle() || !meta.hasAuthor()) return
        metaList.add(R.getString(player, S.PURCHASE_BOOK_TITLE_TEMPLATE.resource(), meta.title, meta.author))
    }

    private fun addBundleMeta(meta: BundleMeta, metaList: MutableList<String>) {
        val bundleBuilder = StringBuilder()
        meta.items.forEach {
            bundleBuilder.append("${it.type.toString().titlecase()} (${it.amount}), ")
        }

        metaList.add(bundleBuilder.removeSuffix(", ").toString())
    }

    private fun addDamageableMeta(player: Player, meta: Damageable, metaList: MutableList<String>) {
        if (meta.hasMaxDamage()) {
            metaList.add(R.getString(player, S.PURCHASE_DURABILITY_TEMPLATE.resource(), "${meta.maxDamage - meta.damage} / ${meta.maxDamage}"))
        } else if (meta.damage > 0) {
            metaList.add(R.getString(player, S.PURCHASE_DAMAGE_TEMPLATE.resource(), meta.damage))
        }
    }

    private fun addMusicInstrumentMeta(meta: MusicInstrumentMeta, metaList: MutableList<String>) {
        metaList.add(meta.instrument.toString().fromMCItem())
    }

    private fun addOminousBottleMeta(player: Player, meta: OminousBottleMeta, metaList: MutableList<String>) {
        if (meta.hasAmplifier()) metaList.add(R.getString(player, S.PURCHASE_AMPLIFIER_TEMPLATE.resource(), meta.amplifier))
    }

    private fun addPotionMeta(meta: PotionMeta, metaList: MutableList<String>) {
        metaList.add(meta.basePotionType.toString().fromMCItem())
    }
}