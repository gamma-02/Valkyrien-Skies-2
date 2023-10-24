package org.valkyrienskies.mod.mixin.mod_compat.sodium;

import static org.valkyrienskies.mod.common.VSClientGameUtils.transformRenderWithShip;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mixin(SodiumWorldRenderer.class)
public class MixinSodiumWorldRenderer {


    @Redirect(method = "renderBlockEntity", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderDispatcher;render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V"))
    private static <E extends BlockEntity> void renderShipChunkBlockEntity(
        BlockEntityRenderDispatcher instance, E blockEntity, float tickDelta, PoseStack f, MultiBufferSource buffer) {

        final BlockPos blockEntityPos = blockEntity.getBlockPos();
        final ClientShip shipObject = VSGameUtilsKt.getShipObjectManagingPos(((ClientLevel)blockEntity.getLevel()), blockEntityPos);
        if (shipObject != null) {
            final Vec3 cam = instance.camera.getPosition();
            f.popPose();
            f.pushPose();
            transformRenderWithShip(shipObject.getRenderTransform(), f, blockEntityPos, cam.x(), cam.y(),
                cam.z());
        }
        instance.render(blockEntity, tickDelta, f, buffer);
    }

}
