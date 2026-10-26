package com.questcurse;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Наказания первого типа: "потеря" частей тела / органов. */
public enum BodyPart {
    LEFT_ARM("Левая рука"),
    RIGHT_ARM("Правая рука"),
    LEGS("Ноги"),
    EYES("Глаза"),
    STOMACH("Желудок"),
    LUNGS("Лёгкие"),
    HEART("Сердце"),
    EARS("Вестибулярный аппарат");

    public final String title;

    BodyPart(String title) { this.title = title; }

    /** Вызывается раз в секунду, пока проклятие активно. */
    public void apply(ServerPlayer p) {
        switch (this) {
            case LEFT_ARM -> { eff(p, MobEffects.MINING_FATIGUE, 1); eff(p, MobEffects.WEAKNESS, 0); }
            case RIGHT_ARM -> { eff(p, MobEffects.MINING_FATIGUE, 2); }
            case LEGS -> { eff(p, MobEffects.SLOWNESS, 2); }
            case EYES -> { eff(p, MobEffects.BLINDNESS, 0); }
            case STOMACH -> { eff(p, MobEffects.HUNGER, 3); }
            case LUNGS -> {
                // под водой воздух кончается в разы быстрее
                if (p.isUnderWater()) p.setAirSupply(p.getAirSupply() - 40);
            }
            case HEART -> {
                // максимум 3 сердца
                if (p.getHealth() > 6.0f) p.setHealth(6.0f);
                eff(p, MobEffects.WEAKNESS, 1);
            }
            case EARS -> { eff(p, MobEffects.NAUSEA, 0); }
        }
    }

    private static void eff(ServerPlayer p, Holder<MobEffect> e, int amp) {
        p.addEffect(new MobEffectInstance(e, 60, amp, false, false, true));
    }
}
