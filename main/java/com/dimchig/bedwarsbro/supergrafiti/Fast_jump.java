package com.dimchig.bedwarsbro.supergrafiti;

import com.dimchig.bedwarsbro.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;

public class Fast_jump {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean isActive = false;

    public void updateBooleans() {
        isActive = Main.getConfigBool(Main.CONFIG_MSG.FAST_JUMP);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !isActive) return; // Проверяем наличие игрока и мира
        EntityPlayerSP player = mc.thePlayer;

        // Используем рефлексию для изменения jumpTicks в EntityLivingBase
        try {
            Field jumpTicksField = EntityLivingBase.class.getDeclaredField("jumpTicks");
            jumpTicksField.setAccessible(true); // Делаем поле доступным
            jumpTicksField.setInt(player, 0); // Устанавливаем значение jumpTicks в 0
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace(); // Обработка исключений
        }
    }
}