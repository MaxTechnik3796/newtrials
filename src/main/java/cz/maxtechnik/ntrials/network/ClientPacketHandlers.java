package cz.maxtechnik.ntrials.network;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;
@OnlyIn(Dist.CLIENT)
public class ClientPacketHandlers{
	public static void handleTrialSpawnerSync(Supplier<NetworkEvent.Context> contextSupplier){
		NetworkEvent.Context context=contextSupplier.get();
		context.setPacketHandled(true);
	}
}