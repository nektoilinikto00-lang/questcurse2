package com.questcurse;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = QuestCurse.MODID)
public class GameEvents {

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        QuestManager.serverTick(event.getServer());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) QuestManager.onLogin(sp);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer sp) QuestManager.onLogout(sp);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        QuestManager.clearAll();
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer sp) {
            QuestManager.onBlockBroken(sp, event.getState());
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer dead) {
            QuestManager.onPlayerDeath(dead);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            QuestManager.onKill(killer, event.getEntity().getType());
        }
    }

    @SubscribeEvent
    public static void onCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(Commands.literal("quest")
                .executes(ctx -> {
                    QuestManager.sendStatus(ctx.getSource().getPlayerOrException());
                    return 1;
                })
                .then(Commands.literal("status").executes(ctx -> {
                    QuestManager.sendStatus(ctx.getSource().getPlayerOrException());
                    return 1;
                }))
                .then(Commands.literal("cleanse").executes(ctx ->
                        QuestManager.cleanse(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("new").executes(ctx -> {
                    if (!Config.DEBUG_COMMAND.get()) {
                        ctx.getSource().sendFailure(Component.literal("Отключено в конфиге (debugCommand)."));
                        return 0;
                    }
                    QuestManager.forceNewTask(ctx.getSource().getPlayerOrException());
                    return 1;
                })));
    }
}
