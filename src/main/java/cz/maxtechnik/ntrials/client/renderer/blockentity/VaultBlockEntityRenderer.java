package cz.maxtechnik.ntrials.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.ntrials.block.VaultBlock;
import cz.maxtechnik.ntrials.block.entity.VaultBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class VaultBlockEntityRenderer implements BlockEntityRenderer<VaultBlockEntity> {

    public VaultBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(VaultBlockEntity vaultEntity, float partialTick, PoseStack poseStack,
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Level level = vaultEntity.getLevel();
        if (level == null) {
            return;
        }

        // Zobrazuje itemy pouze pokud je vault ACTIVE
        VaultBlock.VaultState state = vaultEntity.getBlockState().getValue(VaultBlock.STATE);

        if (state != VaultBlock.VaultState.ACTIVE || !vaultEntity.hasDisplayItems()) {
            return;
        }

        ItemStack currentItem = vaultEntity.getCurrentDisplayItem();
        if (currentItem.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        // posicion offset from block (in this case default is center)
        poseStack.translate(0.5, 0.5, 0.5); // Zvýšil z 0.7 na 1.5

        // Rotace kolem Y osy (vertikální rotace)
        float rotation = vaultEntity.getItemRotation() + partialTick * 2.0f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Mírné pohupování nahoru a dolů
        float time = (level.getGameTime() + partialTick) * 0.1f;
        float bobbing = (float) Math.sin(time) * 0.1f; // Zvětšil pohupování
        poseStack.translate(0, bobbing, 0);

        // percentage scaling
        poseStack.scale(1.0f, 1.0f, 1.0f);

        // Vykreslení itemu
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(currentItem, level, null, 0);

        System.out.println("DEBUG: Renderuji item na pozici Y=1.5, scale=1.5: " + currentItem.getItem().getDescriptionId());

        itemRenderer.render(currentItem, ItemDisplayContext.GROUND, false, poseStack,
                          bufferSource, packedLight, OverlayTexture.NO_OVERLAY, model);

        poseStack.popPose();
    }
}
