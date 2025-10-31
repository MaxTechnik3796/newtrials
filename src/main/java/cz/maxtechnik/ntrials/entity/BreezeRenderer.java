package cz.maxtechnik.ntrials.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import cz.maxtechnik.ntrials.client.model.BreezeWind;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BreezeRenderer extends MobRenderer<BreezeEntity, BreezeModel> {
    private static final ResourceLocation BREEZE_TEXTURE = new ResourceLocation("ntrials", "textures/entities/breeze.png");
    private static final ResourceLocation BREEZE_WIND_TEXTURE = new ResourceLocation("ntrials", "textures/entities/breeze_wind.png");

    private final BreezeWind<BreezeEntity> windModel;

    public BreezeRenderer(EntityRendererProvider.Context context) {
        super(context, new BreezeModel(context.bakeLayer(BreezeModel.LAYER_LOCATION)), 0.5F);
        this.windModel = new BreezeWind<>(context.bakeLayer(BreezeWind.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(BreezeEntity entity) {
        return BREEZE_TEXTURE;
    }

    @Override
    protected float getFlipDegrees(BreezeEntity entity) {
        return 0.0F; // Breeze doesn't flip when dying
    }

    @Override
    public void render(BreezeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

        // Render the spinning wind effect
        poseStack.pushPose();

        // Move the wind effect up more
        poseStack.translate(0.0D, 1.5D, 0.0D);

        // Rotate the model vertically (flip it upside down)
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(180.0F));

        // Render the wind model (texture will rotate, not the model geometry)
        var vertexConsumer = bufferSource.getBuffer(this.windModel.renderType(BREEZE_WIND_TEXTURE));
        this.windModel.renderToBuffer(poseStack, vertexConsumer, packedLight, getOverlayCoords(entity, 0.0F), 1.0F, 1.0F, 1.0F, 0.5F);

        poseStack.popPose();
    }
}