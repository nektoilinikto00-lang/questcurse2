package com.questcurse;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue INTERVAL_MIN = B
            .comment("Через сколько минут после прошлого задания выдаётся новое (X)")
            .defineInRange("intervalMinutes", 10, 1, 1440);
    public static final ModConfigSpec.IntValue TASK_TIME_MIN = B
            .comment("Сколько минут даётся на выполнение задания")
            .defineInRange("taskTimeMinutes", 5, 1, 120);
    public static final ModConfigSpec.IntValue BLESSING_CHANCE = B
            .comment("Шанс благословения за выполнение задания, %")
            .defineInRange("blessingChancePercent", 80, 0, 100);
    public static final ModConfigSpec.IntValue BLESSING_MIN = B
            .comment("Длительность благословения, минут")
            .defineInRange("blessingMinutes", 5, 1, 120);
    public static final ModConfigSpec.IntValue BODY_PUNISH_CHANCE = B
            .comment("Шанс того, что наказанием будет потеря органа (иначе — спавн мобов), %")
            .defineInRange("bodyPunishmentChancePercent", 50, 0, 100);
    public static final ModConfigSpec.IntValue CURSE_MIN = B
            .comment("Через сколько минут проклятие (потеря органа) проходит само")
            .defineInRange("curseMinutes", 5, 1, 120);
    public static final ModConfigSpec.IntValue DIAMOND_COST = B
            .comment("Цена снятия одного проклятия алмазами (/quest cleanse)")
            .defineInRange("diamondCostPerCurse", 3, 1, 64);
    public static final ModConfigSpec.BooleanValue DEBUG_COMMAND = B
            .comment("Включить тестовую команду /quest new (мгновенно выдаёт задание)")
            .define("debugCommand", false);

    public static final ModConfigSpec SPEC = B.build();
}
