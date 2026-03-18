package dev.ripiters.create_frequency.common.link.controller;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.ripiters.create_frequency.common.CFItems;
import net.createmod.catnip.math.AngleHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class LecternFrequencyControllerRenderer extends SafeBlockEntityRenderer<LecternFrequencyControllerBlockEntity> {

    public LecternFrequencyControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(LecternFrequencyControllerBlockEntity be, float partialTicks, PoseStack ms,
                              MultiBufferSource buffer, int light, int overlay) {

        ItemStack stack = CFItems.FREQUENCY_CONTROLLER.asStack();
        ItemDisplayContext transformType = ItemDisplayContext.NONE;
        CustomRenderedItemModel mainModel = (CustomRenderedItemModel) Minecraft.getInstance()
                .getItemRenderer()
                .getModel(stack, be.getLevel(), null, 0);
        PartialItemModelRenderer renderer = PartialItemModelRenderer.of(stack, transformType, ms, buffer, overlay);
        boolean active = be.hasUser();
        boolean renderDepression = be.isUsedBy(Minecraft.getInstance().player);

        Direction facing = be.getBlockState().getValue(LecternFrequencyControllerBlock.FACING);
        var msr = TransformStack.of(ms);

        ms.pushPose();
        msr.translate(0.5, 1.45, 0.5);
        msr.rotateYDegrees(AngleHelper.horizontalAngle(facing) - 90);
        msr.translate(0.28, 0, 0);
        msr.rotateZDegrees(-22.0f);
        FrequencyControllerItemRenderer.renderInLectern(stack, mainModel, renderer, transformType, ms, light, active, renderDepression);
        ms.popPose();
    }

}
