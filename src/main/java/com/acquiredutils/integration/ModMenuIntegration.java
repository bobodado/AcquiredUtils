package com.acquiredutils.integration;

import com.acquiredutils.config.ConfigManager;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> YetAnotherConfigLib.createBuilder()
                .title(Component.literal("AcquiredUtils"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Notifications"))
                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("X Position"))
                                .description(OptionDescription.of(Component.literal("Horizontal position. -1 for default (right side).")))
                                .binding(-1, () -> ConfigManager.get().notificationX, v -> ConfigManager.get().notificationX = v)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(-1, 1920)
                                        .step(1))
                                .build())
                        .option(Option.<Integer>createBuilder()
                                .name(Component.literal("Y Position"))
                                .description(OptionDescription.of(Component.literal("Vertical position. -1 for default (centered).")))
                                .binding(-1, () -> ConfigManager.get().notificationY, v -> ConfigManager.get().notificationY = v)
                                .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                        .range(-1, 1080)
                                        .step(1))
                                .build())
                        .build())
                .category(ConfigCategory.createBuilder()
                        .name(Component.literal("Rarity Filter"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Show Common"))
                                .binding(true, () -> ConfigManager.get().showCommon, v -> ConfigManager.get().showCommon = v)
                                .controller(BooleanControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Show Uncommon"))
                                .binding(true, () -> ConfigManager.get().showUncommon, v -> ConfigManager.get().showUncommon = v)
                                .controller(BooleanControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Show Rare"))
                                .binding(true, () -> ConfigManager.get().showRare, v -> ConfigManager.get().showRare = v)
                                .controller(BooleanControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Show Epic"))
                                .binding(true, () -> ConfigManager.get().showEpic, v -> ConfigManager.get().showEpic = v)
                                .controller(BooleanControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Show Legendary"))
                                .binding(true, () -> ConfigManager.get().showLegendary, v -> ConfigManager.get().showLegendary = v)
                                .controller(BooleanControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.literal("Show Mythic"))
                                .binding(true, () -> ConfigManager.get().showMythic, v -> ConfigManager.get().showMythic = v)
                                .controller(BooleanControllerBuilder::create)
                                .build())
                        .build())
                .save(ConfigManager::save)
                .build()
                .generateScreen(parent);
    }
}
