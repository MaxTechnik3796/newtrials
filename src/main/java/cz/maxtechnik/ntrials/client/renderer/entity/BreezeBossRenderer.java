package cz.maxtechnik.ntrials.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import cz.maxtechnik.ntrials.client.model.BreezeBossModel;
import cz.maxtechnik.ntrials.client.model.BreezeWindBossModel;
import cz.maxtechnik.ntrials.entity.BreezeBossEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class BreezeBossRenderer extends MobRenderer<BreezeBossEntity, BreezeBossModel<BreezeBossEntity>> {
	private static final ResourceLocation BREEZE_BOSS_TEXTURE = ResourceLocation.fromNamespaceAndPath("ntrials", "textures/entities/breeze_boss.png");
	private static final ResourceLocation BREEZE_WIND_BOSS_TEXTURE = ResourceLocation.fromNamespaceAndPath("ntrials", "textures/entities/breeze_wind_boss.png");

	private final BreezeWindBossModel<BreezeBossEntity> windModel;

	public BreezeBossRenderer(EntityRendererProvider.Context context) {
		super(context, new BreezeBossModel<>(context.bakeLayer(BreezeBossModel.LAYER_LOCATION)), 0.5F);
		this.windModel = new BreezeWindBossModel<>(context.bakeLayer(BreezeWindBossModel.LAYER_LOCATION));
	}

	@Override
	public @NotNull ResourceLocation getTextureLocation(@NotNull BreezeBossEntity entity) {
		return BREEZE_BOSS_TEXTURE;
	}

	@Override
	protected float getFlipDegrees(@NotNull BreezeBossEntity entity) {
		return 0.0F;
	}

	@Override
	public void render(@NotNull BreezeBossEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, net.minecraft.client.renderer.@NotNull MultiBufferSource bufferSource, int packedLight) {
		super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

		// Logika pro animaci textury
		float ageInTicks = (float)entity.tickCount + partialTicks;
		float scrollSpeed = 0.05F;
		float uOffset = Mth.frac(ageInTicks * scrollSpeed);
		VertexConsumer baseVertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(BREEZE_WIND_BOSS_TEXTURE));
		VertexConsumer scrollingVertexConsumer = new UvScrollingVertexConsumer(baseVertexConsumer, uOffset);

		// Renderování Větru
		poseStack.pushPose();
		poseStack.translate(0.0D, 1.5D, 0.0D);
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F)); // Otočení vzhůru nohama
		int overlayCoords = MobRenderer.getOverlayCoords(entity, partialTicks);
		this.windModel.renderToBuffer(poseStack, scrollingVertexConsumer, packedLight, overlayCoords, 1.0F, 1.0F, 1.0F, 0.5F);
		poseStack.popPose();
	}
    private record UvScrollingVertexConsumer(VertexConsumer parent, float uOffset) implements VertexConsumer {

        @NotNull
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            parent.vertex(x, y, z);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            parent.color(red, green, blue, alpha);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer uv(float u, float v) {
            parent.uv(u + uOffset, v);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            parent.overlayCoords(u, v);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer uv2(int u, int v) {
            parent.uv2(u, v);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer normal(float x, float y, float z) {
            parent.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            parent.endVertex();
        }

        @Override
        public void defaultColor(int defaultRed, int defaultGreen, int defaultBlue, int defaultAlpha) {
            parent.defaultColor(defaultRed, defaultGreen, defaultBlue, defaultAlpha);
        }

        @Override
        public void unsetDefaultColor() {
            parent.unsetDefaultColor();
        }
    }
}

