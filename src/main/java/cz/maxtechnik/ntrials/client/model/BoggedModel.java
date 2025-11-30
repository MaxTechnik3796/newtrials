package cz.maxtechnik.ntrials.client.model;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
// Made with Blockbench 5.0.0
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
public class BoggedModel<T extends Mob&RangedAttackMob> extends HumanoidModel<T>{
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION=new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("ntrials","bogged_model"),"main");
	public final ModelPart hat;
	public final ModelPart head;
	public final ModelPart body;
	public final ModelPart left_arm;
	public final ModelPart right_arm;
	public final ModelPart left_leg;
	public final ModelPart right_leg;
	public BoggedModel(ModelPart root){
		super(root);
		this.hat=root.getChild("hat");
		this.head=root.getChild("head");
		this.body=root.getChild("body");
		this.left_arm=root.getChild("left_arm");
		this.right_arm=root.getChild("right_arm");
		this.left_leg=root.getChild("left_leg");
		this.right_leg=root.getChild("right_leg");
	}
	public static LayerDefinition createBodyLayer(){
		MeshDefinition meshdefinition=new MeshDefinition();
		PartDefinition partdefinition=meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.offset(0.0F,0.0F,0.0F));
		partdefinition.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0,0).addBox(-4.0F,-8.0F,-4.0F,8.0F,8.0F,8.0F,new CubeDeformation(0.0F)).texOffs(0,32).addBox(-4.0F,-8.0F,-4.0F,8.0F,8.0F,8.0F,new CubeDeformation(0.25F)),
				PartPose.offset(0.0F,0.0F,0.0F));
		partdefinition.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(16,16).addBox(-4.0F,0.0F,-2.0F,8.0F,12.0F,4.0F,new CubeDeformation(0.0F)).texOffs(36,31).addBox(-4.0F,0.0F,-2.0F,8.0F,12.0F,4.0F,new CubeDeformation(0.25F)),
				PartPose.offset(0.0F,0.0F,0.0F));
		partdefinition.addOrReplaceChild("left_arm",CubeListBuilder.create().texOffs(40,2).mirror().addBox(0.0F,-2.0F,-1.0F,2.0F,12.0F,2.0F,new CubeDeformation(0.0F)).mirror(false).texOffs(48,0).mirror().addBox(-1.0F,-2.0F,-2.0F,4.0F,12.0F,4.0F,new CubeDeformation(0.25F)).mirror(false),PartPose.offset(5.0F,2.0F,0.0F));
		partdefinition.addOrReplaceChild("right_arm",CubeListBuilder.create().texOffs(40,16).addBox(-1.0F,-2.0F,-1.0F,2.0F,12.0F,2.0F,new CubeDeformation(0.0F)),PartPose.offset(-5.0F,2.0F,0.0F));
		partdefinition.addOrReplaceChild("left_leg",CubeListBuilder.create().texOffs(0,16).mirror().addBox(-1.0F,0.0F,-1.1F,2.0F,12.0F,2.0F,new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(2.0F,12.0F,0.1F));
		partdefinition.addOrReplaceChild("right_leg",CubeListBuilder.create().texOffs(0,16).addBox(-1.0F,0.0F,-1.1F,2.0F,12.0F,2.0F,new CubeDeformation(0.0F)),PartPose.offset(-2.0F,12.0F,0.1F));
		return LayerDefinition.create(meshdefinition,64,48);
	}
	@Override
	public void prepareMobModel(T p_103793_,float p_103794_,float p_103795_,float p_103796_){
		this.rightArmPose=ArmPose.EMPTY;
		this.leftArmPose=ArmPose.EMPTY;
		ItemStack $$4=p_103793_.getItemInHand(InteractionHand.MAIN_HAND);
		if($$4.is(Items.BOW)&&p_103793_.isAggressive()){
			if(p_103793_.getMainArm()==HumanoidArm.RIGHT){
				this.rightArmPose=ArmPose.BOW_AND_ARROW;
			}else{
				this.leftArmPose=ArmPose.BOW_AND_ARROW;
			}
		}
		super.prepareMobModel(p_103793_,p_103794_,p_103795_,p_103796_);
	}
	@Override
	public void renderToBuffer(@NotNull PoseStack poseStack,@NotNull VertexConsumer vertexConsumer,int packedLight,int packedOverlay,float red,float green,float blue,float alpha){
		hat.render(poseStack,vertexConsumer,packedLight,packedOverlay,red,green,blue,alpha);
		head.render(poseStack,vertexConsumer,packedLight,packedOverlay,red,green,blue,alpha);
		body.render(poseStack,vertexConsumer,packedLight,packedOverlay,red,green,blue,alpha);
		left_arm.render(poseStack,vertexConsumer,packedLight,packedOverlay,red,green,blue,alpha);
		right_arm.render(poseStack,vertexConsumer,packedLight,packedOverlay,red,green,blue,alpha);
		left_leg.render(poseStack,vertexConsumer,packedLight,packedOverlay,red,green,blue,alpha);
		right_leg.render(poseStack,vertexConsumer,packedLight,packedOverlay,red,green,blue,alpha);
	}
	@Override
	public void setupAnim(@NotNull T p_103798_,float p_103799_,float p_103800_,float p_103801_,float p_103802_,float p_103803_){
		super.setupAnim(p_103798_,p_103799_,p_103800_,p_103801_,p_103802_,p_103803_);
		ItemStack $$6=p_103798_.getMainHandItem();
		if(p_103798_.isAggressive()&&($$6.isEmpty()||!$$6.is(Items.BOW))){
			float $$7=Mth.sin(this.attackTime*3.1415927F);
			float $$8=Mth.sin((1.0F-(1.0F-this.attackTime)*(1.0F-this.attackTime))*3.1415927F);
			this.rightArm.zRot=0.0F;
			this.leftArm.zRot=0.0F;
			this.rightArm.yRot=-(0.1F-$$7*0.6F);
			this.leftArm.yRot=0.1F-$$7*0.6F;
			this.rightArm.xRot=-1.5707964F;
			this.leftArm.xRot=-1.5707964F;
			ModelPart var10000=this.rightArm;
			var10000.xRot-=$$7*1.2F-$$8*0.4F;
			var10000=this.leftArm;
			var10000.xRot-=$$7*1.2F-$$8*0.4F;
			AnimationUtils.bobArms(this.rightArm,this.leftArm,p_103801_);
		}
	}
	@Override
	public void translateToHand(@NotNull HumanoidArm p_103778_,@NotNull PoseStack p_103779_){
		float $$2=p_103778_==HumanoidArm.RIGHT?1.0F:-1.0F;
		ModelPart $$3=this.getArm(p_103778_);
		$$3.x+=$$2;
		$$3.translateAndRotate(p_103779_);
		$$3.x-=$$2;
	}
}
