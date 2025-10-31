package cz.maxtechnik.ntrials.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class BreezeWind<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("ntrials", "breeze_wind"), "main");
	private final ModelPart Wind;

	public BreezeWind(ModelPart root) {
		this.Wind = root.getChild("Wind");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Wind = partdefinition.addOrReplaceChild("Wind", CubeListBuilder.create()
						// První vrstva: nejmenší a nejblíže
						.texOffs(1, 83).addBox(-2.5F, -7.0F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
						// Druhá vrstva
						.texOffs(74, 28).addBox(-6.0F, -13.0F, -6.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
						// Třetí vrstva: největší a nejvzdálenější
						.texOffs(0, 0).addBox(-9.0F, -21.0F, -9.0F, 18.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
						// Čtvrtá vrstva
						.texOffs(6, 6).addBox(-6.0F, -21.0F, -6.0F, 12.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Zde nechte prázdné, model větru nepotřebuje animaci kloubů.
	}

	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		// Pouze renderujeme statický model.
		Wind.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}

