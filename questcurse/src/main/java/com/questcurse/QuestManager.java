package com.questcurse;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class QuestManager {
    private static final Random RNG = new Random();
    private static final Map<UUID, PlayerState> STATES = new HashMap<>();

    private static final String K_NEXT = "qc_next";
    private static final String K_CURSES = "qc_curses";

    private static int intervalSec() { return Config.INTERVAL_MIN.get() * 60; }

    // =====================================================================
    // Состояние и сохранение (в persistent data игрока)
    // =====================================================================

    public static PlayerState state(ServerPlayer p) {
        return STATES.computeIfAbsent(p.getUUID(), id -> load(p));
    }

    private static PlayerState load(ServerPlayer p) {
        PlayerState s = new PlayerState();
        CompoundTag d = p.getPersistentData();
        s.untilNextSec = d.getIntOr(K_NEXT, intervalSec());
        String raw = d.getStringOr(K_CURSES, "");
        if (!raw.isEmpty()) {
            for (String part : raw.split(";")) {
                String[] kv = part.split(":");
                if (kv.length != 2) continue;
                try {
                    s.curses.add(new PlayerState.Curse(BodyPart.valueOf(kv[0]), Integer.parseInt(kv[1])));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return s;
    }

    private static void save(ServerPlayer p, PlayerState s) {
        CompoundTag d = p.getPersistentData();
        d.putInt(K_NEXT, s.untilNextSec);
        StringBuilder sb = new StringBuilder();
        for (PlayerState.Curse c : s.curses) {
            if (sb.length() > 0) sb.append(';');
            sb.append(c.part.name()).append(':').append(c.leftSec);
        }
        d.putString(K_CURSES, sb.toString());
    }

    public static void onLogin(ServerPlayer p) {
        STATES.remove(p.getUUID());
        PlayerState s = state(p);
        if (!s.curses.isEmpty()) {
            p.sendSystemMessage(Component.literal("На тебе всё ещё лежат проклятия. Смотри /quest")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public static void onLogout(ServerPlayer p) {
        PlayerState s = STATES.remove(p.getUUID());
        if (s == null) return;
        // активное задание при выходе отменяется без наказания
        if (s.task != null) s.untilNextSec = intervalSec();
        save(p, s);
    }

    /** Вызывать при остановке сервера/выходе из мира, чтобы состояния не «перетекали» между мирами. */
    public static void clearAll() { STATES.clear(); }

    // =====================================================================
    // Главный цикл — раз в секунду
    // =====================================================================

    public static void serverTick(MinecraftServer server) {
        int tc = server.getTickCount();
        if (tc % 20 != 0) return;
        boolean autosave = (tc / 20) % 30 == 0;
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!p.isAlive() || p.isSpectator()) continue;
            PlayerState st = state(p);
            tickPlayer(p, st);
            if (autosave) save(p, st);
        }
    }

    private static void tickPlayer(ServerPlayer p, PlayerState st) {
        // --- проклятия ---
        Iterator<PlayerState.Curse> it = st.curses.iterator();
        while (it.hasNext()) {
            PlayerState.Curse c = it.next();
            if (--c.leftSec <= 0) {
                it.remove();
                p.sendSystemMessage(Component.literal("Проклятие снято: " + c.part.title)
                        .withStyle(ChatFormatting.AQUA));
            } else {
                c.part.apply(p);
            }
        }

        // --- нет задания: ждём ---
        if (st.task == null) {
            if (--st.untilNextSec <= 0) assign(p, st);
            return;
        }

        // --- есть задание ---
        if (st.task.kind() == TaskDef.Kind.COLLECT) {
            st.progress = Math.max(0, countItems(p, st.task) - st.baseline);
        }

        if (st.progress >= st.task.amount()) {
            st.task = null;
            st.untilNextSec = intervalSec();
            if (RNG.nextInt(100) < Config.BLESSING_CHANCE.get()) {
                Blessings.bless(p);
            } else {
                p.sendSystemMessage(Component.literal("Задание выполнено, но боги промолчали...")
                        .withStyle(ChatFormatting.GRAY));
            }
        } else if (--st.taskLeftSec <= 0) {
            st.task = null;
            st.untilNextSec = intervalSec();
            Punishments.punish(p, st);
        } else {
            int left = st.taskLeftSec;
            p.displayClientMessage(Component.literal("📜 " + st.task.title() + " ["
                    + Math.min(st.progress, st.task.amount()) + "/" + st.task.amount() + "]  ⏱ "
                    + (left / 60) + ":" + String.format("%02d", left % 60))
                    .withStyle(ChatFormatting.GOLD), true);
        }
    }

    private static void assign(ServerPlayer p, PlayerState st) {
        List<TaskDef> all = Tasks.all();
        TaskDef t;
        do {
            t = all.get(RNG.nextInt(all.size()));
        } while (t == st.lastTask && all.size() > 1);
        st.task = t;
        st.lastTask = t;
        st.progress = 0;
        st.taskLeftSec = Config.TASK_TIME_MIN.get() * 60;
        st.baseline = t.kind() == TaskDef.Kind.COLLECT ? countItems(p, t) : 0;
        p.sendSystemMessage(Component.literal("📜 НОВОЕ ЗАДАНИЕ: " + t.title() + " — у тебя "
                + Config.TASK_TIME_MIN.get() + " мин.").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }

    /** Для debug-команды. */
    public static void forceNewTask(ServerPlayer p) {
        PlayerState st = state(p);
        if (st.task == null) assign(p, st);
        else p.sendSystemMessage(Component.literal("Задание уже есть.").withStyle(ChatFormatting.GRAY));
    }

    // =====================================================================
    // Прогресс
    // =====================================================================

    public static void onBlockBroken(ServerPlayer p, BlockState state) {
        if (p.isCreative()) return;
        PlayerState st = STATES.get(p.getUUID());
        if (st != null && st.task != null && st.task.kind() == TaskDef.Kind.MINE
                && st.task.blocks().contains(state.getBlock())) {
            st.progress++;
        }
    }

    public static void onKill(ServerPlayer p, EntityType<?> type) {
        PlayerState st = STATES.get(p.getUUID());
        if (st != null && st.task != null && st.task.kind() == TaskDef.Kind.KILL
                && st.task.entities().contains(type)) {
            st.progress++;
        }
    }

    public static void onPlayerDeath(ServerPlayer p) {
        PlayerState st = STATES.get(p.getUUID());
        if (st != null) st.curses.clear();   // смерть снимает проклятия
    }

    // =====================================================================
    // Команды
    // =====================================================================

    public static void sendStatus(ServerPlayer p) {
        PlayerState st = state(p);
        if (st.task != null) {
            p.sendSystemMessage(Component.literal("Задание: " + st.task.title() + " ["
                    + Math.min(st.progress, st.task.amount()) + "/" + st.task.amount()
                    + "], осталось " + fmt(st.taskLeftSec)).withStyle(ChatFormatting.GOLD));
        } else {
            p.sendSystemMessage(Component.literal("Следующее задание через " + fmt(st.untilNextSec))
                    .withStyle(ChatFormatting.YELLOW));
        }
        if (st.curses.isEmpty()) {
            p.sendSystemMessage(Component.literal("Проклятий нет.").withStyle(ChatFormatting.GREEN));
        } else {
            for (PlayerState.Curse c : st.curses) {
                p.sendSystemMessage(Component.literal(" ☠ " + c.part.title + " — ещё " + fmt(c.leftSec))
                        .withStyle(ChatFormatting.RED));
            }
            p.sendSystemMessage(Component.literal("Снять за алмазы: /quest cleanse ("
                    + Config.DIAMOND_COST.get() + " алм. за каждое)").withStyle(ChatFormatting.GRAY));
        }
    }

    /** Платное снятие проклятий. Возвращает число снятых. */
    public static int cleanse(ServerPlayer p) {
        PlayerState st = state(p);
        if (st.curses.isEmpty()) {
            p.sendSystemMessage(Component.literal("У тебя нет проклятий.").withStyle(ChatFormatting.GREEN));
            return 0;
        }
        int cost = Config.DIAMOND_COST.get();
        int removed = 0;
        while (!st.curses.isEmpty() && countItem(p, Items.DIAMOND) >= cost) {
            takeItems(p, Items.DIAMOND, cost);
            PlayerState.Curse c = st.curses.remove(0);
            p.sendSystemMessage(Component.literal("Ты откупился от проклятия: " + c.part.title)
                    .withStyle(ChatFormatting.AQUA));
            removed++;
        }
        if (removed == 0) {
            p.sendSystemMessage(Component.literal("Нужно минимум " + cost + " алмаз(ов).")
                    .withStyle(ChatFormatting.RED));
        }
        save(p, st);
        return removed;
    }

    // =====================================================================
    // Утилиты
    // =====================================================================

    private static String fmt(int sec) {
        sec = Math.max(0, sec);
        return (sec / 60) + ":" + String.format("%02d", sec % 60);
    }

    private static int countItems(ServerPlayer p, TaskDef t) {
        int sum = 0;
        for (Item i : t.items()) sum += countItem(p, i);
        return sum;
    }

    private static int countItem(ServerPlayer p, Item item) {
        int n = 0;
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(item)) n += s.getCount();
        }
        return n;
    }

    private static void takeItems(ServerPlayer p, Item item, int amount) {
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getContainerSize() && amount > 0; i++) {
            ItemStack s = inv.getItem(i);
            if (s.is(item)) {
                int take = Math.min(amount, s.getCount());
                s.shrink(take);
                amount -= take;
            }
        }
        inv.setChanged();
    }
}
