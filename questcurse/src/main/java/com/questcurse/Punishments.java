package com.questcurse;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.List;
import java.util.Random;

public class Punishments {
    private static final Random RNG = new Random();

    private record SpawnPunish(String name, EntityType<?> type, int count) {}

    private static List<SpawnPunish> spawnList() {
        return List.of(
                new SpawnPunish("Орда зомби", EntityType.ZOMBIE, 6),
                new SpawnPunish("Скелеты-лучники", EntityType.SKELETON, 4),
                new SpawnPunish("Криперы", EntityType.CREEPER, 3),
                new SpawnPunish("Ведьмы", EntityType.WITCH, 2),
                new SpawnPunish("Фантомы", EntityType.PHANTOM, 3),
                new SpawnPunish("Рой силверфишей", EntityType.SILVERFISH, 10),
                new SpawnPunish("Пещерные пауки", EntityType.CAVE_SPIDER, 8),
                new SpawnPunish("Налётчики-разбойники", EntityType.VINDICATOR, 2),
                new SpawnPunish("Бомбардировка TNT", EntityType.TNT, 4),
                new SpawnPunish("Грозовые разряды", EntityType.LIGHTNING_BOLT, 3)
        );
    }

    /** Случайное наказание за провал. */
    public static void punish(ServerPlayer p, PlayerState st) {
        if (RNG.nextInt(100) < Config.BODY_PUNISH_CHANCE.get()) {
            BodyPart part = BodyPart.values()[RNG.nextInt(BodyPart.values().length)];
            int sec = Config.CURSE_MIN.get() * 60;
            PlayerState.Curse existing = null;
            for (PlayerState.Curse c : st.curses) if (c.part == part) existing = c;
            if (existing != null) existing.leftSec = sec;   // повторное выпадение просто продлевает
            else st.curses.add(new PlayerState.Curse(part, sec));
            p.sendSystemMessage(Component.literal("☠ Задание провалено! Ты потерял: " + part.title
                    + " (" + Config.CURSE_MIN.get() + " мин или /quest cleanse за алмазы)")
                    .withStyle(ChatFormatting.DARK_RED));
        } else {
            List<SpawnPunish> list = spawnList();
            SpawnPunish sp = list.get(RNG.nextInt(list.size()));
            p.sendSystemMessage(Component.literal("☠ Задание провалено! Наказание: " + sp.name())
                    .withStyle(ChatFormatting.DARK_RED));
            spawn(p, sp.type(), sp.count());
        }
    }

    private static void spawn(ServerPlayer p, EntityType<?> type, int count) {
        ServerLevel level = (ServerLevel) p.level();
        for (int i = 0; i < count; i++) {
            for (int attempt = 0; attempt < 15; attempt++) {
                Entity e = type.create(level, EntitySpawnReason.EVENT);
                if (e == null) return;
                double a = RNG.nextDouble() * Math.PI * 2;
                double r = 3 + RNG.nextDouble() * 4;
                e.setPos(p.getX() + Math.cos(a) * r, p.getY(), p.getZ() + Math.sin(a) * r);
                if (level.noCollision(e)) {
                    if (e instanceof Mob mob) {
                        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(e.blockPosition()),
                                EntitySpawnReason.EVENT, null);
                        mob.setTarget(p);               // сразу идут на игрока
                        mob.setPersistenceRequired();   // и не деспавнятся
                    }
                    level.addFreshEntity(e);
                    break;
                }
            }
        }
    }
}
