package com.mcmlr.system.products.data

import com.mcmlr.system.SystemConfigRepository
import org.bukkit.entity.Player
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionsRepository @Inject constructor(
    private val systemConfigRepository: SystemConfigRepository,
) {

    fun checkPermission(player: Player, node: PermissionNode): Boolean = if (systemConfigRepository.model.usePermissions) {
        player.isOp ||
                player.hasPermission(PermissionNode.ADMIN.node) ||
                (player.hasPermission(PermissionNode.PLAYER.node) && node != PermissionNode.ADMIN) ||
                player.hasPermission(node.node)
    } else if (node == PermissionNode.ADMIN) {
        player.isOp
    } else {
        true
    }

    fun checkPermission(player: Player, node: String): Boolean = if (systemConfigRepository.model.usePermissions) {
        player.isOp ||
                player.hasPermission(PermissionNode.ADMIN.node) ||
                (player.hasPermission(PermissionNode.PLAYER.node) && node != PermissionNode.ADMIN.node) ||
                player.hasPermission(node)
    } else if (node == PermissionNode.ADMIN.node) {
        player.isOp
    } else {
        true
    }
}

enum class PermissionNode(val node: String) {
    HOME("apps.home"),
    WARP("apps.warp"),
    TELEPORT("apps.teleport"),
    MARKET("apps.market"),
    PREFERENCES("apps.preferences"),
    SPAWN("apps.spawn"),
    BACK("apps.spawn.back"),
    WORKBENCH("apps.workbench"),
    RECIPE("apps.recipe"),
    KIT("apps.kit"),
    TUTORIAL("apps.tutorial"),
    CHEATS("apps.cheats"),
    PLAYER("apps.player"),
    ADMIN("apps.admin"),
}