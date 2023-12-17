package com.atombuilt.shulkers

import com.atombuilt.atomkt.spigot.listener.KotlinListener
import com.atombuilt.atomkt.spigot.listener.register
import com.atombuilt.atomkt.spigot.listener.unregister
import net.minecraft.world.item.BlockItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.craftbukkit.v1_20_R3.block.CraftShulkerBox
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import net.kyori.adventure.sound.Sound as AdventureSound
import net.minecraft.world.item.ItemStack as NMSItemStack

class ShulkerInventory private constructor(
    private val itemStack: ItemStack,
    private val player: Player
) : KotlinListener, KoinComponent {

    private val inventoryHolder = InventoryHolder { throw IllegalStateException() }
    private val meta = itemStack.itemMeta as BlockStateMeta
    private val blockState = meta.blockState as CraftShulkerBox
    private var inventory = createInventory()

    private fun createInventory(): Inventory {
        return Bukkit.createInventory(inventoryHolder, InventoryType.SHULKER_BOX)
    }

    private fun open() {
        playOpenSound()
        register(get())
        player.openInventory(inventory)
    }

    private fun playOpenSound() {
        val sound = AdventureSound.sound().type(Sound.BLOCK_SHULKER_BOX_OPEN).build()
        player.playSound(sound)
    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.holder !== inventoryHolder) return
        playCloseSound()
        updateItemStack()
        unregister()
    }

    private fun updateItemStack() {
        val tileEntity = blockState.tileEntity
        inventory.contents.forEachIndexed { index, itemStack ->
            tileEntity.setItem(index, itemStack.toNMS())
        }
        BlockItem.setBlockEntityData(itemStack.toNMS(), tileEntity.type, tileEntity.saveWithFullMetadata())
    }

    private fun ItemStack?.toNMS(): NMSItemStack {
        if (this == null) return CraftItemStack.asNMSCopy(ItemStack(Material.AIR))
        return (this as CraftItemStack).handle
    }

    private fun playCloseSound() {
        val sound = AdventureSound.sound().type(Sound.BLOCK_SHULKER_BOX_CLOSE).build()
        player.playSound(sound)
    }

    companion object {

        fun openFor(itemStack: ItemStack, player: Player) = ShulkerInventory(itemStack, player).also { it.open() }
    }
}
