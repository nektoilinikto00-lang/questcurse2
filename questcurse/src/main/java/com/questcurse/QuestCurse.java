package com.questcurse;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.common.Mod;

@Mod(QuestCurse.MODID)
public class QuestCurse {
    public static final String MODID = "questcurse";

    public QuestCurse(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
