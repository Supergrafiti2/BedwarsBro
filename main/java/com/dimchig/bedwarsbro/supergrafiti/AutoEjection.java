package com.dimchig.bedwarsbro.supergrafiti;

import com.dimchig.bedwarsbro.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.world.World;

public class AutoEjection {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean isActive = false;
    private boolean hasDroppedAllItems = false;
    private int tickDelay = 0;
    private boolean isTransferring = false;
    private boolean isInventoryOpen = false;

    public void updateBooleans() {
        isActive = Main.getConfigBool(Main.CONFIG_MSG.AUTO_EJECTION);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.thePlayer == null || !isActive) return;
        EntityPlayer player = mc.thePlayer;

        if (player.posY > 20) {
            isInventoryOpen = false;
        }

        if (isFallingIntoVoid(player)) {
            if (!isInventoryOpen) {
                mc.displayGuiScreen(new GuiInventory(player));
                isInventoryOpen = true;
            }
            ejectItemsFromHotbar(player);
        }
    }

    private boolean isFallingIntoVoid(EntityPlayer player) {
        BlockPos pos = player.getPosition();

        if (pos.getY() >= 20) {
            return false;
        }

        World world = player.worldObj;

        if (player.motionY >= -0.7) {
            return false;
        }

        for (int y = pos.getY() - 1; y >= 0; y--) {
            BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
            if (!world.isAirBlock(checkPos)) {
                return false;
            }
        }
        return true;
    }

    private void ejectItemsFromHotbar(EntityPlayer player) {
        if (hasDroppedAllItems) return;

        if (tickDelay > 0) {
            tickDelay--;
            return;
        }

        boolean hasDropped = false;

        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = player.inventory.getStackInSlot(i);
            if (itemStack != null && itemStack.stackSize > 0 && isTargetItem(itemStack)) {
                player.inventory.currentItem = i;
                player.dropOneItem(true);
                hasDropped = true;
            }
        }

        if (!hasDropped) {
            transferItemsToHotbar(player);
        }

        if (hasDropped && isInventoryEmpty(player)) {
            hasDroppedAllItems = true;
        }
    }

    private void transferItemsToHotbar(EntityPlayer player) {
        if (isTransferring) return;

        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack stackInSlot = player.inventory.getStackInSlot(i);
            if (stackInSlot != null && isTargetItem(stackInSlot) && i >= 9) {
                isTransferring = true;
                Container playerContainer = player.openContainer;
                int windowId = playerContainer.windowId;
                short actionNumber = playerContainer.getNextTransactionID(player.inventory);

                mc.thePlayer.sendQueue.addToSendQueue(new C0EPacketClickWindow(
                        windowId,
                        i,
                        0,
                        2,
                        stackInSlot,
                        actionNumber
                ));
                isTransferring = false;
                break;
            }
        }
    }

    private boolean isInventoryEmpty(EntityPlayer player) {
        for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
            ItemStack itemStack = player.inventory.getStackInSlot(i);
            if (itemStack != null && isTargetItem(itemStack)) {
                return false;
            }
        }
        return true;
    }

    private boolean isTargetItem(ItemStack itemStack) {
        Item item = itemStack.getItem();
        return item == Item.getItemById(265) ||
                item == Item.getItemById(266) ||
                item == Item.getItemById(264) ||
                item == Item.getItemById(388);
    }
}
