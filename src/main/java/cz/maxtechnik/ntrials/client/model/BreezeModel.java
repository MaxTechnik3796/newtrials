package cz.maxtechnik.ntrials.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import cz.maxtechnik.ntrials.entity.BreezeEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class BreezeModel extends EntityModel<BreezeEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("ntrials", "breeze_model"), "main");
    private final ModelPart head;
    private final ModelPart rods1;
	private final ModelPart rods2;
	private final ModelPart heavy_core;

    public BreezeModel(ModelPart root) {
        this.head = root.getChild("head");
        this.rods1 = root.getChild("rods1");
		ModelPart rotation_1=this.rods1.getChild("rotation_1");
		rotation_1.getChild("rod_1");
		ModelPart rotation_2=this.rods1.getChild("rotation_2");
		rotation_2.getChild("rod_2");
		ModelPart rotation_3=this.rods1.getChild("rotation_3");
		rotation_3.getChild("rod_3");
		this.rods2 = root.getChild("rods2");
		this.rods2.getChild("rotation_4");
		this.heavy_core = root.getChild("heavy_core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4.0F,-8.0F,-4.0F,8.0F,8.0F,8.0F,new CubeDeformation(0.0F)).texOffs(8,16).addBox(-4.5F,-8.25F,-4.5F,9.0F,8.0F,9.0F,new CubeDeformation(0.0F)),PartPose.offset(0.0F,4.0F,0.0F));

		PartDefinition rods1 = partdefinition.addOrReplaceChild("rods1", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition rotation_1 = rods1.addOrReplaceChild("rotation_1", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));
		rotation_1.addOrReplaceChild("rod_1",CubeListBuilder.create().texOffs(0,17).addBox(-1.0F,-12.9343F,1.3576F,2.0F,8.0F,2.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,3.0F,-3.0F,0.3927F,0.0F,0.0F));

		PartDefinition rotation_2 = rods1.addOrReplaceChild("rotation_2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -3.1416F, 1.0472F, -3.1416F));
		rotation_2.addOrReplaceChild("rod_2",CubeListBuilder.create().texOffs(0,17).addBox(-1.0F,-12.9343F,1.3576F,2.0F,8.0F,2.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,3.0F,-3.0F,0.3927F,0.0F,0.0F));

		PartDefinition rotation_3 = rods1.addOrReplaceChild("rotation_3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -3.1416F, -1.0472F, 3.1416F));
		rotation_3.addOrReplaceChild("rod_3",CubeListBuilder.create().texOffs(0,17).addBox(-1.0F,-12.9343F,1.3576F,2.0F,8.0F,2.0F,new CubeDeformation(0.0F)),PartPose.offsetAndRotation(0.0F,3.0F,-3.0F,0.3927F,0.0F,0.0F));

		PartDefinition rods2 = partdefinition.addOrReplaceChild("rods2", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 0.0F));
        PartDefinition rotation_4 = rods2.addOrReplaceChild("rotation_4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        rotation_4.addOrReplaceChild("rod_7_r1", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -4.0F, -10.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.0472F, 0.0F));
        rotation_4.addOrReplaceChild("rod_6_r1", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -4.0F, -10.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));
        rotation_4.addOrReplaceChild("rod_5_r1", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -4.0F, -10.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

		partdefinition.addOrReplaceChild("heavy_core",CubeListBuilder.create().texOffs(0,0).addBox(-1.0F,-17.0F,-1.0F,2.0F,2.0F,2.0F,new CubeDeformation(0.0F)).texOffs(32,0).addBox(-2.0F,-18.25F,-2.0F,4.0F,4.0F,4.0F,new CubeDeformation(0.0F)),PartPose.offset(0.0F,24.0F,0.0F));

		return LayerDefinition.create(meshdefinition, 48, 33);
    }

    @Override
    public void setupAnim(@NotNull BreezeEntity entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch) {
        // Head rotation
        this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
        this.head.xRot = headPitch * ((float)Math.PI / 180F);

        // Rod rotations
        float rodRotation = ageInTicks * 0.1F;
        this.rods1.yRot = rodRotation;
        this.rods2.yRot = -rodRotation * 0.8F;

        // Hovering animation
        float hoverOffset = Mth.sin(ageInTicks * 0.1F) * 0.1F;
        this.head.y = 4.0F + hoverOffset;
        this.rods1.y = 8.0F + hoverOffset;
        this.rods2.y = 9.0F + hoverOffset;
        this.heavy_core.y = 24.0F + hoverOffset;
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack,@NotNull VertexConsumer vertexConsumer,int packedLight,int packedOverlay,float red,float green,float blue,float alpha){
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rods1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rods2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        heavy_core.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}