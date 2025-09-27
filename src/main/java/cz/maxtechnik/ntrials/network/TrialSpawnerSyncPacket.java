package cz.maxtechnik.ntrials.network;

import cz.maxtechnik.ntrials.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class TrialSpawnerSyncPacket {
    private final BlockPos pos;
    private final String entityTypeId;

    public TrialSpawnerSyncPacket(BlockPos pos, EntityType<?> entityType) {
        this.pos = pos;
        if (entityType != null) {
            ResourceLocation location = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
            this.entityTypeId = location != null ? location.toString() : "";
        } else {
            this.entityTypeId = "";
        }
    }

    public TrialSpawnerSyncPacket(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.entityTypeId = buf.readUtf();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeUtf(this.entityTypeId);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                BlockEntity blockEntity = level.getBlockEntity(this.pos);
                if (blockEntity instanceof TrialSpawnerBlockEntity trialSpawner) {
                    if (this.entityTypeId.isEmpty()) {
                        trialSpawner.setSpawnEntity(null);
                    } else {
                        ResourceLocation location = new ResourceLocation(this.entityTypeId);
                        EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(location);
                        trialSpawner.setSpawnEntity(entityType);
                    }
                }
            }
        });
        context.get().setPacketHandled(true);
    }
}
