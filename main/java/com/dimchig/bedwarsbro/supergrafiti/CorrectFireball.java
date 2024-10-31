package com.dimchig.bedwarsbro.supergrafiti;

import com.dimchig.bedwarsbro.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

import java.util.HashSet;
import java.util.Set;

public class CorrectFireball {

    static Minecraft mc;

    // Переменная для включения/выключения функции
    public static boolean isActive = false;

    // Время последнего нажатия правой кнопки мыши
    private long lastRightClickTime = 0;

    // Список сущностей фаерболов, по которым уже был нанесён удар
    private Set<Integer> hitFireballs = new HashSet<>();

    public CorrectFireball() {
        mc = Minecraft.getMinecraft();
    }

    // Метод для обновления состояния функции (вкл/выкл)
    public void updateBooleans() {
        isActive = Main.getConfigBool(Main.CONFIG_MSG.CORRECT_FIREBALL);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Проверяем, что игра и игрок инициализированы правильно, и функция включена
        if (mc == null || mc.thePlayer == null || !isActive) return;

        EntityPlayer player = mc.thePlayer;

        // Проверяем, нажата ли правая кнопка мыши в данный момент
        if (Mouse.isButtonDown(1)) {
            // Запоминаем время нажатия правой кнопки мыши
            lastRightClickTime = System.currentTimeMillis();
        }

        // Проверяем, не прошло ли больше 1 секунды с момента нажатия правой кнопки
        if (System.currentTimeMillis() - lastRightClickTime <= 1000) {
            // Проверяем, есть ли перед игроком фаербол
            EntityFireball fireball = getFireballInFrontOfPlayer();
            if (fireball != null) {
                // Ударяем фаербол, если он еще не был ударен
                if (!hasBeenHit(fireball)) {
                    hitFireball(fireball);
                }
            }
        }
    }

    // Метод для проверки наличия фаербола перед игроком и возвращения сущности фаербола
    private EntityFireball getFireballInFrontOfPlayer() {
        // Проверка, является ли объект перед игроком (под прицелом) фаерболом
        if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null) {
            if (mc.objectMouseOver.entityHit instanceof EntityFireball) {
                return (EntityFireball) mc.objectMouseOver.entityHit;
            }
        }
        return null;
    }

    // Метод для проверки, был ли фаербол уже ударен
    private boolean hasBeenHit(EntityFireball fireball) {
        return hitFireballs.contains(fireball.getEntityId());
    }

    // Метод для регистрации удара по фаерболу (добавляем его в список ударенных фаерболов)
    private void registerHit(EntityFireball fireball) {
        hitFireballs.add(fireball.getEntityId());
    }

    // Метод для имитации удара по фаерболу (симуляция нажатия левой кнопки мыши)
    private void hitFireball(EntityFireball fireball) {
        // Имитируем нажатие и отпускание клавиши удара (левая кнопка мыши)
        int attackKey = mc.gameSettings.keyBindAttack.getKeyCode();
        KeyBinding.setKeyBindState(attackKey, true);
        KeyBinding.onTick(attackKey);
        KeyBinding.setKeyBindState(attackKey, false);

        // Регистрируем, что по этому фаерболу уже был нанесён удар
        registerHit(fireball);
    }
}