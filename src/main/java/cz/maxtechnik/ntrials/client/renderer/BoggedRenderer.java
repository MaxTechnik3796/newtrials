package cz.maxtechnik.ntrials.client.renderer;


import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import cz.maxtechnik.ntrials.entity.BoggedEntity;
import cz.maxtechnik.ntrials.client.model.ModelBogged;
import org.jetbrains.annotations.NotNull;

public class BoggedRenderer extends MobRenderer<BoggedEntity,ModelBogged<BoggedEntity>> {
    public BoggedRenderer(EntityRendererProvider.Context context){
        super(context,new ModelBogged<>(context.bakeLayer(ModelBogged.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull BoggedEntity entity){
        return ResourceLocation.fromNamespaceAndPath("ntrials","textures/entities/bogged.png");
    }
}
