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
import org.jetbrains.annotations.NotNull;
public class BreezeWindBossModel<T extends Entity> extends EntityModel<T>{
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("ntrials","breeze_wind_boss_model"),"main");
	private final ModelPart wind_top;
	private final ModelPart wind_middle;
	private final ModelPart wind_bottom;
	public BreezeWindBossModel(ModelPart root){
		this.wind_top=root.getChild("wind_top");
		this.wind_middle=root.getChild("wind_middle");
		this.wind_bottom=root.getChild("wind_bottom");
	}
	public static LayerDefinition createBodyLayer(){
		MeshDefinition meshdefinition=new MeshDefinition();
		PartDefinition partdefinition=meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("wind_top",CubeListBuilder.create().texOffs(0,0).addBox(-18.0F,-28.0F,-18.0F,36.0F,16.0F,36.0F,new CubeDeformation(0.0F))
				.texOffs(0,53).addBox(-13.0F,-28.0F,-13.0F,25.0F,16.0F,26.0F,new CubeDeformation(0.0F)),PartPose.offset(0.0F,11.0F,0.0F));
		partdefinition.addOrReplaceChild("wind_middle",CubeListBuilder.create().texOffs(0,55).addBox(-12.0F,-19.0F,-12.0F,24.0F,12.0F,24.0F,new CubeDeformation(0.0F)),PartPose.offset(0.0F,17.0F,0.0F));
		partdefinition.addOrReplaceChild("wind_bottom",CubeListBuilder.create().texOffs(0,116).addBox(-4.5F,-14.0F,-4.5F,9.0F,14.0F,9.0F,new CubeDeformation(0.0F)),PartPose.offset(0.0F,24.0F,0.0F));
		return LayerDefinition.create(meshdefinition,256,256);
	}
	@Override
	public void setupAnim(@NotNull Entity entity,float limbSwing,float limbSwingAmount,float ageInTicks,float netHeadYaw,float headPitch){
	}
	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack,@NotNull VertexConsumer vertexConsumer,int packedLight,int packedOverlay,float red,float green,float blue,float alpha){
		wind_top.render(poseStack,vertexConsumer,packedLight,packedOverlay,red,green,blue,alpha);
		wind_middle.render(poseStack,vertexConsumer,packedLight,packedOverlay,red,green,blue,alpha);
		wind_bottom.render(poseStack,vertexConsumer,packedLight,packedOverlay,red,green,blue,alpha);
	}
}