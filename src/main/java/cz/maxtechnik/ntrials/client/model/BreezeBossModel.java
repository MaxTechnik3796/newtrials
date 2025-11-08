package cz.maxtechnik.ntrials.client.model;// Made with Blockbench 5.0.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class BreezeBossModel<T extends Entity> extends EntityModel<T>{
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("ntrials", "breeze_boss_model"), "main");
	private final ModelPart head;
	private final ModelPart rods1;
	private final ModelPart rotation_1;
	private final ModelPart rod_1;
	private final ModelPart rotation_2;
	private final ModelPart rod_2;
	private final ModelPart rotation_3;
	private final ModelPart rod_3;
	private final ModelPart rods2;
	private final ModelPart rotation_4;
	private final ModelPart heavycore;

	public BreezeBossModel(ModelPart root) {
		this.head = root.getChild("head");
		this.rods1 = root.getChild("rods1");
		this.rotation_1 = this.rods1.getChild("rotation_1");
		this.rod_1 = this.rotation_1.getChild("rod_1");
		this.rotation_2 = this.rods1.getChild("rotation_2");
		this.rod_2 = this.rotation_2.getChild("rod_2");
		this.rotation_3 = this.rods1.getChild("rotation_3");
		this.rod_3 = this.rotation_3.getChild("rod_3");
		this.rods2 = root.getChild("rods2");
		this.rotation_4 = this.rods2.getChild("rotation_4");
		this.heavycore = root.getChild("heavycore");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -36.0F, -8.0F, 16.0F, 16.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(16, 32).addBox(-9.5F, -36.25F, -8.5F, 18.0F, 16.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition rods1 = partdefinition.addOrReplaceChild("rods1", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition rotation_1 = rods1.addOrReplaceChild("rotation_1", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition rod_1 = rotation_1.addOrReplaceChild("rod_1", CubeListBuilder.create().texOffs(0, 34).addBox(-2.0F, -31.0F, -2.6424F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -3.0F, 0.3927F, 0.0F, 0.0F));

		PartDefinition rotation_2 = rods1.addOrReplaceChild("rotation_2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -3.1416F, 1.0472F, -3.1416F));

		PartDefinition rod_2 = rotation_2.addOrReplaceChild("rod_2", CubeListBuilder.create().texOffs(0, 34).addBox(-3.0F, -31.0F, -2.6424F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -3.0F, 0.3927F, 0.0F, 0.0F));

		PartDefinition rotation_3 = rods1.addOrReplaceChild("rotation_3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -3.1416F, -1.0472F, 3.1416F));

		PartDefinition rod_3 = rotation_3.addOrReplaceChild("rod_3", CubeListBuilder.create().texOffs(-4, 15).addBox(-3.0F, -31.0F, -2.6424F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -3.0F, 0.3927F, 0.0F, 0.0F));

		PartDefinition rods2 = partdefinition.addOrReplaceChild("rods2", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 0.0F));

		PartDefinition rotation_4 = rods2.addOrReplaceChild("rotation_4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition rod_7_r1 = rotation_4.addOrReplaceChild("rod_7_r1", CubeListBuilder.create().texOffs(0, 34).addBox(-3.0F, -21.0F, -10.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 0.0F, -8.0F, 0.0F, 1.0472F, 0.0F));

		PartDefinition rod_6_r1 = rotation_4.addOrReplaceChild("rod_6_r1", CubeListBuilder.create().texOffs(0, 34).addBox(-3.0F, -21.0F, -10.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.0F, 8.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition rod_5_r1 = rotation_4.addOrReplaceChild("rod_5_r1", CubeListBuilder.create().texOffs(0, 34).addBox(-3.0F, -21.0F, -10.0F, 4.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 0.0F, -8.0F, 0.0F, -1.0472F, 0.0F));

		PartDefinition heavycore = partdefinition.addOrReplaceChild("heavycore", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -34.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(64, 0).addBox(-4.0F, -36.25F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 96, 66);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack,VertexConsumer vertexConsumer,int packedLight,int packedOverlay,float red,float green,float blue,float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		rods1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		rods2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		heavycore.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}