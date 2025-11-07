package cz.maxtechnik.ntrials.client.renderer.entity;

import cz.maxtechnik.ntrials.entity.WindChargeProjectile;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WindChargeProjectileRenderer extends ThrownItemRenderer<WindChargeProjectile> {
    public WindChargeProjectileRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0F, true);
    }
}
