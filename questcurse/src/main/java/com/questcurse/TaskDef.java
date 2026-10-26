package com.questcurse;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Set;

public record TaskDef(Kind kind, String title, int amount,
                      Set<Item> items, Set<Block> blocks, Set<EntityType<?>> entities) {

    public enum Kind { COLLECT, MINE, KILL }

    public static TaskDef collect(String title, int amount, Item... items) {
        return new TaskDef(Kind.COLLECT, title, amount, Set.of(items), Set.of(), Set.of());
    }

    public static TaskDef mine(String title, int amount, Block... blocks) {
        return new TaskDef(Kind.MINE, title, amount, Set.of(), Set.of(blocks), Set.of());
    }

    public static TaskDef kill(String title, int amount, EntityType<?>... types) {
        return new TaskDef(Kind.KILL, title, amount, Set.of(), Set.of(), Set.of(types));
    }
}
