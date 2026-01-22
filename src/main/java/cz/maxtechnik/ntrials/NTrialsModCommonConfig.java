package cz.maxtechnik.ntrials;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid=NTrialsMod.MODID,bus=Mod.EventBusSubscriber.Bus.MOD)
public class NTrialsModCommonConfig{
    private static final ForgeConfigSpec.Builder BUILDER=new ForgeConfigSpec.Builder();

	private static final ForgeConfigSpec.IntValue VAULT_UNLOCKING_DURATION;
	private static final ForgeConfigSpec.IntValue VAULT_EJECT_INTERVAL;
	private static final ForgeConfigSpec.IntValue VAULT_CLOSE_DELAY;
	private static final ForgeConfigSpec.IntValue VAULT_DISPLAY_ITEM_SWITCH_INTERVAL;
	private static final ForgeConfigSpec.DoubleValue VAULT_ITEM_ROTATION_SPEED;

	private static final ForgeConfigSpec.IntValue SPAWNER_COOLDOWN_OMINOUS;
	private static final ForgeConfigSpec.IntValue SPAWNER_LOOT_DROP_INTERVAL;
	private static final ForgeConfigSpec.IntValue SPAWNER_DEFAULT_BASE_MOBS_PER_WAVE;
	private static final ForgeConfigSpec.IntValue SPAWNER_DEFAULT_BASE_TOTAL_MOBS;

	private static final ForgeConfigSpec.IntValue SPAWNER_BREEZE_BASE_MOBS_PER_WAVE;
	private static final ForgeConfigSpec.IntValue SPAWNER_BREEZE_BASE_TOTAL_MOBS;
	private static final ForgeConfigSpec.IntValue SPAWNER_BREEZE_MOBS_ADDED_PER_PLAYER;
	private static final ForgeConfigSpec.IntValue SPAWNER_BREEZE_TOTAL_ADDED_PER_PLAYER;


	private static final ForgeConfigSpec.IntValue BOSS_SPAWNER_SPAWN_DELAY;
	private static final ForgeConfigSpec.IntValue BOSS_SPAWNER_LOOT_DROP_INTERVAL;
	private static final ForgeConfigSpec.IntValue BOSS_SPAWNER_COMPLETE_TRIAL_DELAY;
	private static final ForgeConfigSpec.IntValue BOSS_SPAWNER_BASE_BOSS_HP;
	private static final ForgeConfigSpec.DoubleValue BOSS_SPAWNER_HP_MULTIPLIER_PER_PLAYER;
	private static final ForgeConfigSpec.IntValue BOSS_SPAWNER_MAX_COOLDOWN_TICKS;



	static{
		BUILDER.comment("NTrials common config.");
		BUILDER.comment("This configuration is generated, do not overwrite anything except the values!");
		BUILDER.comment("+----------------------------------------+");
		BUILDER.comment("Restart required - restart the game/server.");
		BUILDER.comment("NTrials-reload required - use '/ntrials_config_reload' command.");
		BUILDER.comment("+----------------------------------------+");


		BUILDER.push("VaultSettings");
		VAULT_UNLOCKING_DURATION=BUILDER.comment("Vault unlocking duration.\nNTrials-reload required!\nDefault value: 10").defineInRange("vault_unlocking_duration",10,1,Integer.MAX_VALUE);
		VAULT_EJECT_INTERVAL=BUILDER.comment("Vault ejecting interval.\nNTrials-reload required!\nDefault value: 20").defineInRange("vault_ejecting_interval",20,1,Integer.MAX_VALUE);
		VAULT_CLOSE_DELAY=BUILDER.comment("Vault close delay.\nNTrials-reload required!\nDefault value: 20").defineInRange("vault_close_delay",20,1,Integer.MAX_VALUE);
		VAULT_DISPLAY_ITEM_SWITCH_INTERVAL=BUILDER.comment("Vault display item switch interval.\nNTrials-reload required!\nDefault value: 20").defineInRange("vault_display_item_switch_interval",20,1,Integer.MAX_VALUE);
		VAULT_ITEM_ROTATION_SPEED=BUILDER.comment("Vault item rotation speed.\nNTrials-reload required!\nDefault value: 2.0").defineInRange("vault_item_rotation_speed",2.0,1.0,Double.MAX_VALUE);
		BUILDER.pop();
		BUILDER.push("SpawnerSettings");
		SPAWNER_COOLDOWN_OMINOUS=BUILDER.comment("Spawner cooldown ominous.\nNTrials-reload required!\nDefault value: 10").defineInRange("spawner_cooldown_ominous",10,1,Integer.MAX_VALUE);
		SPAWNER_LOOT_DROP_INTERVAL=BUILDER.comment("Spawner loot drop interval.\nNTrials-reload required!\nDefault value: 10").defineInRange("spawner_loot_drop_interval",10,1,Integer.MAX_VALUE);
		SPAWNER_DEFAULT_BASE_MOBS_PER_WAVE=BUILDER.comment("Spawner default base mobs per wave.\nNTrials-reload required!\nDefault value: 3").defineInRange("spawner_default_base_mobs_per_wave",3,1,Integer.MAX_VALUE);
		SPAWNER_DEFAULT_BASE_TOTAL_MOBS=BUILDER.comment("Spawner default base total mobs.\nNTrials-reload required!\nDefault value: 6").defineInRange("spawner_default_base_total_mobs",6,1,Integer.MAX_VALUE);
		SPAWNER_BREEZE_BASE_MOBS_PER_WAVE=BUILDER.comment("Spawner breeze base mobs per wave.\nNTrials-reload required!\nDefault value: 1").defineInRange("spawner_breeze_base_mobs_per_wave",1,1,Integer.MAX_VALUE);
		SPAWNER_BREEZE_BASE_TOTAL_MOBS=BUILDER.comment("Spawner breeze base total mobs.\nNTrials-reload required!\nDefault value: 3").defineInRange("spawner_breeze_base_total_mobs",3,1,Integer.MAX_VALUE);
		SPAWNER_BREEZE_MOBS_ADDED_PER_PLAYER=BUILDER.comment("Spawner breeze mobs added per player.\nNTrials-reload required!\nDefault value: 1").defineInRange("spawner_breeze_mobs_added_per_player",1,1,Integer.MAX_VALUE);
		SPAWNER_BREEZE_TOTAL_ADDED_PER_PLAYER=BUILDER.comment("Spawner breeze total added per player.\nNTrials-reload required!\nDefault value: 2").defineInRange("spawner_breeze_total_added_per_player",2,1,Integer.MAX_VALUE);
		BUILDER.pop();
		BUILDER.push("BossSpawnerSettings");

		BOSS_SPAWNER_SPAWN_DELAY=BUILDER.comment("Boss Spawner spawn delay.\nNTrials-reload required!\nDefault value: 60").defineInRange("boss_spawner_spawn_delay",60,1,Integer.MAX_VALUE);
		BOSS_SPAWNER_LOOT_DROP_INTERVAL=BUILDER.comment("Boss Spawner loot drop interval.\nNTrials-reload required!\nDefault value: 10").defineInRange("boss_spawner_loot_drop_interval",10,1,Integer.MAX_VALUE);
		BOSS_SPAWNER_COMPLETE_TRIAL_DELAY=BUILDER.comment("Boss Spawner complete_trial_delay.\nNTrials-reload required!\nDefault value: 20").defineInRange("boss_spawner_complete_trial_delay",20,1,Integer.MAX_VALUE);
		BOSS_SPAWNER_BASE_BOSS_HP=BUILDER.comment("Boss Spawner base boss hp.\nNTrials-reload required!\nDefault value: 200").defineInRange("boss_spawner_base_boss_hp",200,1,Integer.MAX_VALUE);
		BOSS_SPAWNER_HP_MULTIPLIER_PER_PLAYER=BUILDER.comment("Boss Spawner hp multiplier per player.\nNTrials-reload required!\nDefault value: 1.5").defineInRange("boss_spawner_hs_multiplier_per_player",1.5,1.0,Double.MAX_VALUE);
		BOSS_SPAWNER_MAX_COOLDOWN_TICKS=BUILDER.comment("Boss Spawner max cooldown ticks.\nNTrials-reload required!\nDefault value: 36000").defineInRange("boss_spawner_max_cooldown_ticks",36000,1,Integer.MAX_VALUE);




		BUILDER.pop();
		SPEC=BUILDER.build();
	}
	static final ForgeConfigSpec SPEC;
	public static int vaultUnlockingDuration;
	public static int vaultEjectInterval;
	public static int vaultCloseDelay;
	public static int vaultDisplayItemSwitchInterval;
	public static float vaultItemRotationSpeed;

	public static int spawnerCooldownOminous;
	public static int spawnerLootDropInterval;
	public static int spawnerDefaultBaseMobsPerWave;
	public static int spawnerDefaultBaseTotalMobs;
	public static int spawnerBreezeBaseMobsPerWave;
	public static int spawnerBreezeBaseTotalMobs;
	public static int spawnerBreezeMobsAddedPerPlayer;
	public static int spawnerBreezeTotalAddedPerPlayer;

	public static int bossSpawnerSpawnDelay;
	public static int bossSpawnerLootDropInterval;
	public static int bossSpawnerCompleteTrialDelay;
	public static int bossSpawnerBaseBossHp;
	public static double bossSpawnerHpMultiplierPerPlayer;
	public static int bossSpawnerMaxCooldownTicks;

	@SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
		load();
	}
	@SubscribeEvent
	public static void onReload(ModConfigEvent.Reloading event){
		load();
	}
	public static void load(){
		NTrialsMod.LOGGER.debug("Configuration loaded!");
		vaultUnlockingDuration=VAULT_UNLOCKING_DURATION.get();
		vaultEjectInterval=VAULT_EJECT_INTERVAL.get();
		vaultCloseDelay=VAULT_CLOSE_DELAY.get();
		vaultDisplayItemSwitchInterval=VAULT_DISPLAY_ITEM_SWITCH_INTERVAL.get();
		vaultItemRotationSpeed=VAULT_ITEM_ROTATION_SPEED.get().floatValue();

		spawnerCooldownOminous=SPAWNER_COOLDOWN_OMINOUS.get();
		spawnerLootDropInterval=SPAWNER_LOOT_DROP_INTERVAL.get();
		spawnerDefaultBaseMobsPerWave=SPAWNER_DEFAULT_BASE_MOBS_PER_WAVE.get();
		spawnerDefaultBaseTotalMobs=SPAWNER_DEFAULT_BASE_TOTAL_MOBS.get();
		spawnerBreezeBaseMobsPerWave=SPAWNER_BREEZE_BASE_MOBS_PER_WAVE.get();
		spawnerBreezeBaseTotalMobs=SPAWNER_BREEZE_BASE_TOTAL_MOBS.get();
		spawnerBreezeMobsAddedPerPlayer=SPAWNER_BREEZE_MOBS_ADDED_PER_PLAYER.get();
		spawnerBreezeTotalAddedPerPlayer=SPAWNER_BREEZE_TOTAL_ADDED_PER_PLAYER.get();

		bossSpawnerSpawnDelay=BOSS_SPAWNER_SPAWN_DELAY.get();
		bossSpawnerLootDropInterval=BOSS_SPAWNER_LOOT_DROP_INTERVAL.get();
		bossSpawnerCompleteTrialDelay=BOSS_SPAWNER_COMPLETE_TRIAL_DELAY.get();
		bossSpawnerBaseBossHp=BOSS_SPAWNER_BASE_BOSS_HP.get();
		bossSpawnerHpMultiplierPerPlayer=BOSS_SPAWNER_HP_MULTIPLIER_PER_PLAYER.get();
		bossSpawnerMaxCooldownTicks=BOSS_SPAWNER_MAX_COOLDOWN_TICKS.get();
	}

}
