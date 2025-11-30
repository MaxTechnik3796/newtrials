package cz.maxtechnik.ntrials.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import cz.maxtechnik.ntrials.entity.BoggedEntity;
import cz.maxtechnik.ntrials.client.model.BoggedModel;
import org.jetbrains.annotations.NotNull;
public class BoggedRenderer extends HumanoidMobRenderer<BoggedEntity,HumanoidModel<BoggedEntity>>{
	public BoggedRenderer(EntityRendererProvider.Context context){
		super(context,new BoggedModel<>(context.bakeLayer(BoggedModel.LAYER_LOCATION)),0.5f);
		this.addLayer(new HumanoidArmorLayer<>(this,new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),context.getModelManager()));
	}
	@Override
	public @NotNull ResourceLocation getTextureLocation(@NotNull BoggedEntity entity){
		return ResourceLocation.fromNamespaceAndPath("ntrials","textures/entities/bogged.png");
	}
}
