package com.leclowndu93150.spawnercontrol.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class SpawnerControlConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue DISABLER_DURATION;
    public static final ForgeConfigSpec.BooleanValue SPAWNER_DROP_ENCHANTED;
    public static final ForgeConfigSpec.BooleanValue ALLOW_SPAWNER_COLLECTION;

    static {
        BUILDER.comment("Spawner Control Configuration");

        BUILDER.push("general");
        DISABLER_DURATION = BUILDER
                .comment("Duration in ticks (20 ticks = 1 second) that a spawner will be disabled with the disabler item")
                .defineInRange("disablerDuration", 18000, 1200, 72000);

        SPAWNER_DROP_ENCHANTED = BUILDER
                .comment("Whether items will appear enchanted")
                .define("itemsEnchanted", true);

        ALLOW_SPAWNER_COLLECTION = BUILDER
                .comment("Whether spawners can be collected with the breaker item")
                .define("allowSpawnerCollection", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
}
