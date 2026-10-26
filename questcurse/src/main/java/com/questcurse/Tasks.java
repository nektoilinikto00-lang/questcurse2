package com.questcurse;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/** Список заданий. Добавляй свои сюда. */
public class Tasks {
    private static List<TaskDef> all;

    public static List<TaskDef> all() {
        if (all == null) {
            all = List.of(
                    // --- собрать (считается прирост в инвентаре) ---
                    TaskDef.collect("Добыть алмаз", 1, Items.DIAMOND),
                    TaskDef.collect("Добыть 8 железных слитков", 8, Items.IRON_INGOT),
                    TaskDef.collect("Добыть 3 золотых слитка", 3, Items.GOLD_INGOT),
                    TaskDef.collect("Добыть 5 хлеба", 5, Items.BREAD),
                    TaskDef.collect("Добыть 4 изумруда", 4, Items.EMERALD),
                    TaskDef.collect("Добыть 16 редстоуна", 16, Items.REDSTONE),
                    TaskDef.collect("Добыть 12 лазурита", 12, Items.LAPIS_LAZULI),
                    TaskDef.collect("Добыть 16 угля", 16, Items.COAL),
                    // --- выкопать (считаются сломанные блоки) ---
                    TaskDef.mine("Выкопать 12 угольной руды", 12, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE),
                    TaskDef.mine("Выкопать 6 железной руды", 6, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE),
                    TaskDef.mine("Выкопать алмазную руду", 1, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
                    TaskDef.mine("Добыть 40 булыжника/камня", 40, Blocks.STONE, Blocks.COBBLESTONE),
                    TaskDef.mine("Нарубить 10 брёвен", 10, Blocks.OAK_LOG, Blocks.BIRCH_LOG, Blocks.SPRUCE_LOG,
                            Blocks.JUNGLE_LOG, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.MANGROVE_LOG,
                            Blocks.CHERRY_LOG),
                    TaskDef.mine("Накопать 25 песка", 25, Blocks.SAND, Blocks.RED_SAND),
                    // --- убить ---
                    TaskDef.kill("Убить 5 зомби", 5, EntityType.ZOMBIE),
                    TaskDef.kill("Убить 3 скелетов", 3, EntityType.SKELETON),
                    TaskDef.kill("Убить 2 криперов", 2, EntityType.CREEPER),
                    TaskDef.kill("Убить 4 пауков", 4, EntityType.SPIDER),
                    TaskDef.kill("Убить эндермена", 1, EntityType.ENDERMAN)
            );
        }
        return all;
    }
}
