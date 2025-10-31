package cz.maxtechnik.ntrials.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis; // Důležitý import
import cz.maxtechnik.ntrials.client.model.BreezeModel;
import cz.maxtechnik.ntrials.client.model.BreezeWindModel;
import cz.maxtechnik.ntrials.entity.BreezeEntity;
import net.minecraft.client.renderer.RenderType; // Důležitý import pro RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class BreezeRenderer extends MobRenderer<BreezeEntity,BreezeModel> {
	private static final ResourceLocation BREEZE_TEXTURE = ResourceLocation.fromNamespaceAndPath("ntrials", "textures/entities/breeze.png");
	private static final ResourceLocation BREEZE_WIND_TEXTURE = ResourceLocation.fromNamespaceAndPath("ntrials", "textures/entities/breeze_wind.png");

	private final BreezeWindModel<BreezeEntity> windModel;

	public BreezeRenderer(EntityRendererProvider.Context context) {
		// Používáme super konstruktor z MobRenderer.
		super(context, new BreezeModel(context.bakeLayer(BreezeModel.LAYER_LOCATION)), 0.5F);
		this.windModel = new BreezeWindModel<>(context.bakeLayer(BreezeWindModel.LAYER_LOCATION));
	}

	@Override
	public @NotNull ResourceLocation getTextureLocation(@NotNull BreezeEntity entity) {
		return BREEZE_TEXTURE;
	}

	@Override
	protected float getFlipDegrees(@NotNull BreezeEntity entity) {
		return 0.0F;
	}

	// --- Původní nefunkční metoda getOverlayCoords je odstraněna ---

	@Override
	public void render(@NotNull BreezeEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, net.minecraft.client.renderer.@NotNull MultiBufferSource bufferSource, int packedLight) {
		// Vykreslíme základní model Breeze
		super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);

		// --- Logika pro animaci textury (UV SCROLLING) ---

		// 1. Výpočet aktuálního času pro plynulou animaci
		float ageInTicks = (float)entity.tickCount + partialTicks;

		// Rychlost posunu: 0.05F (posun o 1/20 šířky za tick)
		float scrollSpeed = 0.05F;

		// 2. Vypočítáme posun textury po ose U (horizontální/X)
		// Mth.frac() zajistí, že hodnota je vždy mezi 0.0 a 1.0 (znovu se opakuje)
		float uOffset = Mth.frac(ageInTicks * scrollSpeed);

		// 3. Získáme základní VertexConsumer (použijeme RenderType.entityTranslucent pro průhlednost)
		VertexConsumer baseVertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(BREEZE_WIND_TEXTURE));

		// 4. Vytvoříme náš upravený VertexConsumer, který provede posun U
		VertexConsumer scrollingVertexConsumer = new UvScrollingVertexConsumer(baseVertexConsumer, uOffset);


		// --- Renderování Větru ---

		poseStack.pushPose();

		// Posun nahoru a nastavení správné orientace
		poseStack.translate(0.0D, 1.5D, 0.0D);
		poseStack.mulPose(Axis.XP.rotationDegrees(180.0F)); // Otočení vzhůru nohama

		// POZNÁMKA: Používáme zde chráněnou metodu getOverlayCoords z MobRenderer
		int overlayCoords = MobRenderer.getOverlayCoords(entity, partialTicks);

		// 5. Vykreslíme model s novým, posouvajícím se VertexConsumerem
		this.windModel.renderToBuffer(poseStack, scrollingVertexConsumer, packedLight, overlayCoords, 1.0F, 1.0F, 1.0F, 0.5F);

		poseStack.popPose();
	}

	// --- Vnitřní třída (Wrapper) pro posun UV (Zůstává nezměněna) ---
	// Tato třída zachytí volání UV a přičte k němu náš posun

	private static class UvScrollingVertexConsumer implements VertexConsumer {
		private final VertexConsumer parent;
		private final float uOffset;

		public UvScrollingVertexConsumer(VertexConsumer parent, float uOffset) {
			this.parent = parent;
			this.uOffset = uOffset;
		}

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
			// TOTO JE TO KOUZLO:
			// K původní U souřadnici (horizontální) přičteme náš posun (uOffset)
			// U souřadnice automaticky "wrapne" (přeteče) díky geometrii modelu.
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
