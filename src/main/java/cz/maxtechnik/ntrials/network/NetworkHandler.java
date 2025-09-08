package cz.maxtechnik.ntrials.network;

import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(NTrialsMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void registerPackets() {
        INSTANCE.registerMessage(packetId++, TrialSpawnerSyncPacket.class,
                TrialSpawnerSyncPacket::encode,
                TrialSpawnerSyncPacket::new,
                TrialSpawnerSyncPacket::handle);
    }
}
