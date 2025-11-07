package cz.maxtechnik.ntrials.network;

import net.minecraft.client.multiplayer.ClientLevel; // OK, protože je client-only
import net.minecraft.client.Minecraft; // Pokud potřebuješ
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandlers {
    public static void handleTrialSpawnerSync(TrialSpawnerSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // Zde přesuň tvůj původní handle kód z TrialSpawnerSyncPacket.handle()
            // Např.:
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                // Aktualizuj spawner nebo co děláš s packetem...
            }
        });
        context.setPacketHandled(true);
    }
}