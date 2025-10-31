package cz.maxtechnik.ntrials.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class BreezeWindModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("ntrials", "breeze_wind_model"), "main");
	private final ModelPart Wind;

	public BreezeWindModel(ModelPart root) {
		this.Wind = root.getChild("Wind");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("Wind",CubeListBuilder.create()
						.texOffs(1,83).addBox(-2.5F,-7.0F,-2.5F,5.0F,7.0F,5.0F,new CubeDeformation(0.0F))
						.texOffs(74,28).addBox(-6.0F,-13.0F,-6.0F,12.0F,6.0F,12.0F,new CubeDeformation(0.0F))
						.texOffs(0,0).addBox(-9.0F,-21.0F,-9.0F,18.0F,8.0F,18.0F,new CubeDeformation(0.0F))
						.texOffs(6,6).addBox(-6.0F,-21.0F,-6.0F,12.0F,8.0F,12.0F,new CubeDeformation(0.0F)),
				PartPose.offset(0.0F,24.0F,0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Wind.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}

