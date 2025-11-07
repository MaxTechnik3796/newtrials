package cz.maxtechnik.ntrials.network;

import cz.maxtechnik.ntrials.NTrialsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class NetworkHandler {
	private static final String PROTOCOL_VERSION = "1";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
			ResourceLocation.fromNamespaceAndPath(NTrialsMod.MODID, "main"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	private static int packetId = 0;

	public static void registerPackets() {
		INSTANCE.messageBuilder(TrialSpawnerSyncPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
				.encoder(TrialSpawnerSyncPacket::encode)
				.decoder(TrialSpawnerSyncPacket::decode)
				.consumerMainThread(clientConsumer())  // ← teď už správně!
				.add();
	}

	// Oddělená metoda – vrací BiConsumer přímo (ne Supplier!)
	private static BiConsumer<TrialSpawnerSyncPacket, Supplier<NetworkEvent.Context>> clientConsumer() {
		return DistExecutor.unsafeRunForDist(
				() -> () -> ClientPacketHandlers::handleTrialSpawnerSync,  // CLIENT
				() -> () -> (packet, ctx) -> ctx.get().setPacketHandled(true)  // SERVER
		);
	}
}