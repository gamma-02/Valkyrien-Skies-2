package org.valkyrienskies.mod.mixin.mod_compat.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.jellysquid.mods.sodium.client.gl.device.MultiDrawBatch;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptions.PerformanceSettings;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.DefaultChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.LocalSectionIndex;
import me.jellysquid.mods.sodium.client.render.chunk.data.SectionRenderDataStorage;
import me.jellysquid.mods.sodium.client.render.chunk.data.SectionRenderDataUnsafe;
import me.jellysquid.mods.sodium.client.render.chunk.lists.ChunkRenderList;
import me.jellysquid.mods.sodium.client.render.chunk.region.RenderRegion;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.viewport.CameraTransform;
import me.jellysquid.mods.sodium.client.util.iterator.ByteIterator;
import net.minecraft.client.Minecraft;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.mixinducks.mod_compat.sodium.RegionChunkRendererDuck;

@Mixin(value = DefaultChunkRenderer.class, remap = false, priority = 1101)
public abstract class MixinRegionChunkRenderer implements RegionChunkRendererDuck {

    @Shadow
    protected static int getVisibleFaces(int originX, int originY, int originZ, int chunkX, int chunkY, int chunkZ) {
        return 0;
    }

    @Shadow
    protected static void addDrawCommands(MultiDrawBatch batch, long pMeshData, int mask) {
    }

    @Unique
    private final Vector3d camInWorld = new Vector3d();

    @Unique
    private final Vector3d camInShip = new Vector3d();

    @WrapOperation(
        at = @At(
            value = "INVOKE",
            target = "Lme/jellysquid/mods/sodium/client/render/chunk/DefaultChunkRenderer;fillCommandBuffer(Lme/jellysquid/mods/sodium/client/gl/device/MultiDrawBatch;Lme/jellysquid/mods/sodium/client/render/chunk/region/RenderRegion;Lme/jellysquid/mods/sodium/client/render/chunk/data/SectionRenderDataStorage;Lme/jellysquid/mods/sodium/client/render/chunk/lists/ChunkRenderList;Lme/jellysquid/mods/sodium/client/render/viewport/CameraTransform;Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;Z)V"
        ),
        method = "render"
    )
    private void injectBuildDrawBatches(MultiDrawBatch batch, RenderRegion renderRegion,
        SectionRenderDataStorage renderDataStorage, ChunkRenderList renderList, CameraTransform camera,
        TerrainRenderPass pass, boolean useBlockFaceCulling, Operation<Void> original) {
        final ClientShip ship = VSGameUtilsKt.getShipObjectManagingPos(Minecraft.getInstance().level,
            renderRegion.getChunkX(), renderRegion.getChunkZ());
        CameraTransform camInShip;
        if (ship != null) {
            ship.getRenderTransform().getWorldToShip().transformPosition(camInWorld, this.camInShip);
            camInShip = new CameraTransform(this.camInShip.x, this.camInShip.y, this.camInShip.z);
            inflateFillCommandBuffer(batch, renderRegion, renderDataStorage, renderList, camInShip, pass,
                useBlockFaceCulling);
        } else {
            this.camInShip.set(camInWorld);
            camInShip = new CameraTransform(this.camInShip.x, this.camInShip.y, this.camInShip.z);
            original.call(batch, renderRegion, renderDataStorage, renderList, camInShip, pass, useBlockFaceCulling);
        }
    }

    private static void inflateFillCommandBuffer(MultiDrawBatch batch, RenderRegion renderRegion,
        SectionRenderDataStorage renderDataStorage, ChunkRenderList renderList, CameraTransform camera,
        TerrainRenderPass pass, boolean useBlockFaceCulling) {
        batch.clear();
        ByteIterator iterator = renderList.sectionsWithGeometryIterator(pass.isReverseOrder());


        if (iterator != null) {
            int originX = renderRegion.getChunkX() - 2;//this may need to be 1 but we'll see
            int originY = renderRegion.getChunkY() - 2;
            int originZ = renderRegion.getChunkZ() - 2;

            while (iterator.hasNext()) {
                int sectionIndex = iterator.nextByteAsInt();
                int chunkX = originX + LocalSectionIndex.unpackX(sectionIndex) + 2;
                int chunkY = originY + LocalSectionIndex.unpackY(sectionIndex) + 2;
                int chunkZ = originZ + LocalSectionIndex.unpackZ(sectionIndex) + 2;
                long pMeshData = renderDataStorage.getDataPointer(sectionIndex);
                int slices;
                if (useBlockFaceCulling) {
                    slices = getVisibleFaces((int) camera.x, (int) camera.y, (int) camera.z, chunkX, chunkY, chunkZ);
                } else {
                    slices = ModelQuadFacing.ALL;
                }

                slices &= SectionRenderDataUnsafe.getSliceMask(pMeshData);
                if (slices != 0) {
                    addDrawCommands(batch, pMeshData, slices);
                }
            }

        }
    }

    @WrapOperation(
        at = @At(
            value = "FIELD",
            target = "Lme/jellysquid/mods/sodium/client/gui/SodiumGameOptions$PerformanceSettings;useBlockFaceCulling:Z"
        ),
        method = "render"
    )
    private boolean redirectEnabledCulling(final PerformanceSettings instance, final Operation<Boolean> operation) {
        return false;
    }

//    @Redirect(
//        at = @At(
//            value = "FIELD",
//            target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkCameraContext;posX:F"
//        ),
//        method = "buildDrawBatches"
//    )
//    private float redirectCameraPosX(final ChunkCameraContext instance) {
//        return (float) camInShip.x;
//    }
//
//    @Redirect(
//        at = @At(
//            value = "FIELD",
//            target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkCameraContext;posY:F"
//        ),
//        method = "buildDrawBatches"
//    )
//    private float redirectCameraPosY(final ChunkCameraContext instance) {
//        return (float) camInShip.y;
//    }
//
//    @Redirect(
//        at = @At(
//            value = "FIELD",
//            target = "Lme/jellysquid/mods/sodium/client/render/chunk/ChunkCameraContext;posZ:F"
//        ),
//        method = "buildDrawBatches"
//    )
//    private float redirectCameraPosZ(final ChunkCameraContext instance) {
//        return (float) camInShip.z;
//    }

    @Override
    public void setCameraForCulling(final double x, final double y, final double z) {
        camInWorld.set(x, y, z);
    }
}
