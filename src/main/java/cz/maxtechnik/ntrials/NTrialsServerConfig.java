package cz.maxtechnik.ntrials;

import net.minecraftforge.common.ForgeConfigSpec;

public class NTrialsServerConfig {

    public static final ForgeConfigSpec SPEC;
    public static final Settings CONFIG;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CONFIG = new Settings(builder);
        SPEC = builder.build();
    }

    // Hlavní obal pro všechny kategorie
    public static class Settings {
        public final VaultSettings vault;
        public final SpawnerSettings spawner;
        public final BossSpawnerSettings bossSpawner;

        Settings(ForgeConfigSpec.Builder builder) {
            vault = new VaultSettings(builder);
            spawner = new SpawnerSettings(builder);
            bossSpawner = new BossSpawnerSettings(builder);
        }
    }

    public static class VaultSettings {
        public final ForgeConfigSpec.IntValue unlockingDuration;
        public final ForgeConfigSpec.IntValue ejectInterval;
        public final ForgeConfigSpec.IntValue closeDelay;
        public final ForgeConfigSpec.IntValue displayItemSwitchInterval;
        public final ForgeConfigSpec.DoubleValue itemRotationSpeed;

        VaultSettings(ForgeConfigSpec.Builder builder) {
            builder.push("VaultSettings");

            unlockingDuration = builder.comment("Vault unlocking duration.")
                    .defineInRange("vault_unlocking_duration", 10, 1, Integer.MAX_VALUE);
            ejectInterval = builder.comment("Vault ejecting interval.")
                    .defineInRange("vault_ejecting_interval", 20, 1, Integer.MAX_VALUE);
            closeDelay = builder.comment("Vault close delay.")
                    .defineInRange("vault_close_delay", 20, 1, Integer.MAX_VALUE);
            displayItemSwitchInterval = builder.comment("Vault display item switch interval.")
                    .defineInRange("vault_display_item_switch_interval", 20, 1, Integer.MAX_VALUE);
            itemRotationSpeed = builder.comment("Vault item rotation speed.")
                    .defineInRange("vault_item_rotation_speed", 2.0, 1.0, Integer.MAX_VALUE);

            builder.pop();
        }
    }

    public static class SpawnerSettings {
        public final ForgeConfigSpec.IntValue cooldownOminous;
        public final ForgeConfigSpec.IntValue lootDropInterval;
        public final ForgeConfigSpec.IntValue defaultBaseMobsPerWave;
        public final ForgeConfigSpec.IntValue defaultBaseTotalMobs;
        public final ForgeConfigSpec.IntValue MobsAddedPerPlayer;
        public final ForgeConfigSpec.IntValue TotalAddedPerPlayer;
        public final ForgeConfigSpec.IntValue breezeBaseMobsPerWave;
        public final ForgeConfigSpec.IntValue breezeBaseTotalMobs;
        public final ForgeConfigSpec.IntValue breezeMobsAddedPerPlayer;
        public final ForgeConfigSpec.IntValue breezeTotalAddedPerPlayer;

        SpawnerSettings(ForgeConfigSpec.Builder builder) {
            builder.push("SpawnerSettings");

            cooldownOminous = builder.defineInRange("spawner_cooldown_ominous", 10, 1, Integer.MAX_VALUE);
            lootDropInterval = builder.defineInRange("spawner_loot_drop_interval", 10, 1, Integer.MAX_VALUE);
            defaultBaseMobsPerWave = builder.defineInRange("spawner_default_base_mobs_per_wave", 3, 1, Integer.MAX_VALUE);
            defaultBaseTotalMobs = builder.defineInRange("spawner_default_base_total_mobs", 6, 1, Integer.MAX_VALUE);
            MobsAddedPerPlayer = builder.defineInRange("spawner_mobs_added_per_player", 1, 1, Integer.MAX_VALUE);
            TotalAddedPerPlayer = builder.defineInRange("spawner_total_added_per_player", 2, 1, Integer.MAX_VALUE);
            breezeBaseMobsPerWave = builder.defineInRange("spawner_breeze_base_mobs_per_wave", 1, 1, Integer.MAX_VALUE);
            breezeBaseTotalMobs = builder.defineInRange("spawner_breeze_base_total_mobs", 3, 1, Integer.MAX_VALUE);
            breezeMobsAddedPerPlayer = builder.defineInRange("breeze_spawner_mobs_added_per_player", 1, 1, Integer.MAX_VALUE);
            breezeTotalAddedPerPlayer = builder.defineInRange("breeze_spawner_total_added_per_player", 2, 1, Integer.MAX_VALUE);

            builder.pop();
        }
    }

    public static class BossSpawnerSettings {
        public final ForgeConfigSpec.IntValue spawnDelay;
        public final ForgeConfigSpec.IntValue lootDropInterval;
        public final ForgeConfigSpec.IntValue baseBossHp;
        public final ForgeConfigSpec.DoubleValue hpMultiplierPerPlayer;
        public final ForgeConfigSpec.IntValue maxCooldownTicks;

        BossSpawnerSettings(ForgeConfigSpec.Builder builder) {
            builder.push("BossSpawnerSettings");

            spawnDelay = builder.defineInRange("boss_spawner_spawn_delay", 60, 1, Integer.MAX_VALUE);
            lootDropInterval = builder.defineInRange("boss_spawner_loot_drop_interval", 10, 1, Integer.MAX_VALUE);
            baseBossHp = builder.defineInRange("boss_spawner_base_boss_hp", 200, 1, Integer.MAX_VALUE);
            hpMultiplierPerPlayer = builder.defineInRange("boss_spawner_hp_multiplier_per_player", 1.5, 1.0, Double.MAX_VALUE);
            maxCooldownTicks = builder.defineInRange("boss_spawner_max_cooldown_ticks", 36000, 1, Integer.MAX_VALUE);

            builder.pop();
        }
    }
}