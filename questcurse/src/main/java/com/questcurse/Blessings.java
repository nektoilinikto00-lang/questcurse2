package com.questcurse;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

import java.util.List;
import java.util.Random;

public class Blessings {
    private static final Random RNG = new Random();

    private record Blessing(String name, Holder<MobEffect> effect, int amp) {}

    private static List<Blessing> list() {
        return List.of(
                new Blessing("Благословение скорости", MobEffects.SPEED, 1),
                new Blessing("Благословение силы", MobEffects.STRENGTH, 1),
                new Blessing("Благословение спешки", MobEffects.HASTE, 2),
                new Blessing("Благословение регенерации", MobEffects.REGENERATION, 1),
                new Blessing("Благословение стойкости", MobEffects.RESISTANCE, 1),
                new Blessing("Благословение защиты (поглощение)", MobEffects.ABSORPTION, 3),
                new Blessing("Благословение удачи", MobEffects.LUCK, 2),
                new Blessing("Благословение огня", MobEffects.FIRE_RESISTANCE, 0),
                new Blessing("Благословение ночного зрения", MobEffects.NIGHT_VISION, 0),
                new Blessing("Благословение прыжка", MobEffects.JUMP_BOOST, 2)
        );
    }

    public static void bless(ServerPlayer p) {
        Blessing b = list().get(RNG.nextInt(list().size()));
        int ticks = Config.BLESSING_MIN.get() * 60 * 20;
        p.addEffect(new MobEffectInstance(b.effect(), ticks, b.amp()));
        p.sendSystemMessage(Component.literal("✦ Задание выполнено! " + b.name() + " на "
                + Config.BLESSING_MIN.get() + " мин.").withStyle(ChatFormatting.GREEN));
    }
}
