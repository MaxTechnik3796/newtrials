package cz.maxtechnik.ntrials.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BreezeModel extends EntityModel<BreezeEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("ntrials", "breeze"), "main");
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

    public BreezeModel(ModelPart root) {
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

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
        .texOffs(8, 16).addBox(-4.5F, -8.25F, -4.5F, 9.0F, 8.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition rods1 = partdefinition.addOrReplaceChild("rods1", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));

        PartDefinition rotation_1 = rods1.addOrReplaceChild("rotation_1", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition rod_1 = rotation_1.addOrReplaceChild("rod_1", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -12.9343F, 1.3576F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -3.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition rotation_2 = rods1.addOrReplaceChild("rotation_2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -3.1416F, 1.0472F, -3.1416F));
        PartDefinition rod_2 = rotation_2.addOrReplaceChild("rod_2", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -12.9343F, 1.3576F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -3.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition rotation_3 = rods1.addOrReplaceChild("rotation_3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.0F, 0.0F, -3.1416F, -1.0472F, 3.1416F));
        PartDefinition rod_3 = rotation_3.addOrReplaceChild("rod_3", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -12.9343F, 1.3576F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.0F, -3.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition rods2 = partdefinition.addOrReplaceChild("rods2", CubeListBuilder.create(), PartPose.offset(0.0F, 9.0F, 0.0F));
        PartDefinition rotation_4 = rods2.addOrReplaceChild("rotation_4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        rotation_4.addOrReplaceChild("rod_7_r1", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -4.0F, -10.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.0472F, 0.0F));
        rotation_4.addOrReplaceChild("rod_6_r1", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -4.0F, -10.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));
        rotation_4.addOrReplaceChild("rod_5_r1", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, -4.0F, -10.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.0472F, 0.0F));

        PartDefinition heavycore = partdefinition.addOrReplaceChild("heavycore", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -17.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
        .texOffs(32, 0).addBox(-2.0F, -18.25F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 48, 33);
    }

    @Override
    public void setupAnim(BreezeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
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
        this.heavycore.y = 24.0F + hoverOffset;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rods1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        rods2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        heavycore.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}