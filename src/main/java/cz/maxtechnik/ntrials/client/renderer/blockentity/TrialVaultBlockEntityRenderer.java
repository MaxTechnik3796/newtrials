package cz.maxtechnik.ntrials.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import cz.maxtechnik.ntrials.block.TrialVaultBlock;
import cz.maxtechnik.ntrials.block.entity.TrialVaultBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class TrialVaultBlockEntityRenderer implements BlockEntityRenderer<TrialVaultBlockEntity> {

    public TrialVaultBlockEntityRenderer() {
    }

    @Override
    public void render(TrialVaultBlockEntity vaultEntity, float partialTick, @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Level level = vaultEntity.getLevel();
        if (level == null) {
            return;
        }

        TrialVaultBlock.VaultState state = vaultEntity.getBlockState().getValue(TrialVaultBlock.STATE);

        if (state != TrialVaultBlock.VaultState.ACTIVE || !vaultEntity.hasDisplayItems()) {
            return;
        }

        ItemStack currentItem = vaultEntity.getCurrentDisplayItem();
        if (currentItem.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);

        float rotation = vaultEntity.getItemRotation() + partialTick * 2.0f;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        float time = (level.getGameTime() + partialTick) * 0.1f;
        float bobbing = (float) Math.sin(time) * 0.1f;
        poseStack.translate(0, bobbing, 0);

        poseStack.scale(1.0f, 1.0f, 1.0f);

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BakedModel model = itemRenderer.getModel(currentItem, level, null, 0);
        itemRenderer.render(currentItem, ItemDisplayContext.GROUND, false, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY, model);

        poseStack.popPose();
    }
}
