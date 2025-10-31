package cz.maxtechnik.ntrials.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BreezeRenderer extends MobRenderer<BreezeEntity, BreezeModel> {
    private static final ResourceLocation BREEZE_TEXTURE = new ResourceLocation("ntrials", "textures/entities/breeze.png");
    
    public BreezeRenderer(EntityRendererProvider.Context context) {
        super(context, new BreezeModel(context.bakeLayer(BreezeModel.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BreezeEntity entity) {
        return BREEZE_TEXTURE;
    }

    @Override
    protected float getFlipDegrees(BreezeEntity entity) {
        return 0.0F; // Breeze doesn't flip when dying
    }
}