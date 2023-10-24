package org.valkyrienskies.mod.fabric.mixin.compat.sodium;

import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderListIterable;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.viewport.CameraTransform;
import org.joml.Matrix4f;
import org.joml.Vector3dc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.valkyrienskies.mod.common.VSClientGameUtils;
import org.valkyrienskies.mod.mixinducks.mod_compat.sodium.RegionChunkRendererDuck;
import org.valkyrienskies.mod.mixinducks.mod_compat.sodium.RenderSectionManagerDuck;

@Mixin(value = RenderSectionManager.class, remap = false)
public class MixinRenderSectionManager {

    @Shadow
    @Final
    private ChunkRenderer chunkRenderer;

    @Redirect(at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/gl/device/CommandList;flush()V"),
        method = "renderLayer")
    private void redirectRenderLayer(final CommandList list, final ChunkRenderMatrices matrices,
        final TerrainRenderPass pass, final double camX, final double camY, final double camZ) {

        RenderDevice.INSTANCE.makeActive();

        ((RenderSectionManagerDuck) this).getShipRenderLists().forEach((ship, renderList) -> {
            final Matrix4f newModelView = new Matrix4f(matrices.modelView());
            final Vector3dc center = ship.getRenderTransform().getPositionInShip();
            VSClientGameUtils.transformRenderWithShip(ship.getRenderTransform(), newModelView, center.x(), center.y(),
                center.z(), camX, camY, camZ);

            final ChunkRenderMatrices newMatrices = new ChunkRenderMatrices(matrices.projection(), newModelView);
            ((RegionChunkRendererDuck) chunkRenderer).setCameraForCulling(camX, camY, camZ);
            chunkRenderer.render(newMatrices, list, (ChunkRenderListIterable) renderList, pass,
                new CameraTransform(center.x(), center.y(), center.z()));
            list.close();
        });
    }

}
