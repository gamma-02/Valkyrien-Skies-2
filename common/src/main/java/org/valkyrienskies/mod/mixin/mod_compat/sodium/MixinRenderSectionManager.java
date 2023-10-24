package org.valkyrienskies.mod.mixin.mod_compat.sodium;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ChunkUpdateType;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSection;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import me.jellysquid.mods.sodium.client.render.chunk.lists.SortedRenderLists;
import me.jellysquid.mods.sodium.client.render.viewport.Viewport;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.primitives.AABBd;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.mixinducks.mod_compat.sodium.RenderSectionManagerDuck;

@Mixin(value = RenderSectionManager.class, remap = false)
public abstract class MixinRenderSectionManager implements RenderSectionManagerDuck {

    @Unique
    private final WeakHashMap<ClientShip, SortedRenderLists> shipRenderLists = new WeakHashMap<>();

    @Override
    public WeakHashMap<ClientShip, SortedRenderLists> getShipRenderLists() {
        return shipRenderLists;
    }

    @Shadow
    @Final
    private ClientLevel world;
    @Shadow
    @Final
    private ChunkRenderer chunkRenderer;
    @Shadow
    private @NotNull Map<ChunkUpdateType, ArrayDeque<RenderSection>> rebuildLists;


    @Shadow
    protected abstract RenderSection getRenderSection(int x, int y, int z);


    @Shadow
    private @NotNull SortedRenderLists renderLists;

    @Shadow
    @Final
    private static float NEARBY_REBUILD_DISTANCE;

    @Shadow
    private @Nullable BlockPos lastCameraPosition;

    @Inject(at = @At("TAIL"), method = "update")
    private void afterIterateChunks(final Camera camera, final Viewport viewport, final int frame,
        final boolean spectator, final CallbackInfo ci) {
        for (final ClientShip ship : VSGameUtilsKt.getShipObjectWorld(Minecraft.getInstance()).getLoadedShips()) {
            ship.getActiveChunksSet().forEach((x, z) -> {
                for (int y = world.getMinSection(); y < world.getMaxSection(); y++) {
                    final RenderSection section = getRenderSection(x, y, z);

                    if (section == null) {
                        continue;
                    }

                    if (section.getPendingUpdate() != null) {
                        final ArrayDeque<RenderSection> queue = this.rebuildLists.get(section.getPendingUpdate());
                        if (queue.size() < (2 << 4) - 1) {
                            queue.push(section);
                        }
                    }



//                    final ChunkRenderBounds b = section.getInfo;
                    int x1 = section.getOriginX();
                    int y1 = section.getOriginY();
                    int z1 = section.getOriginZ();
                    int x2 = x1 + 16;
                    int y2 = y1 + 16;
                    int z2 = z1 + 16;

                    final AABBd b2 = new AABBd(x1 - 6e-1, y1 - 6e-1, z1 - 6e-1,
                        x2 + 6e-1, y2 + 6e-1, z2 + 6e-1)
                        .transform(ship.getRenderTransform().getShipToWorld());


                    //works???
                    shipRenderLists.computeIfAbsent(ship, k -> {
                        var list = new SortedRenderLists.Builder(frame);
                        list.add(section);
                        return list.build();
                    });

                    LevelChunk chunk = world.getChunk(section.getChunkX(), section.getChunkZ());
                    LevelChunkSection chunkSection = chunk.getSections()[this.world.getSectionIndex(y)];


                    if (chunkSection.hasOnlyAir() ||
                        !viewport.isBoxVisible(x1, y1, z1,
                            8.0f)) {
                        continue;
                    }




                    //removing for now, unsure...
//                    addEntitiesToRenderLists(section);
                }
            });
        }
    }

    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lme/jellysquid/mods/sodium/client/render/chunk/RenderSectionManager;shouldPrioritizeRebuild(Lme/jellysquid/mods/sodium/client/render/chunk/RenderSection;)Z"
        ),
        method = "scheduleRebuild"
    )
    private boolean redirectIsChunkPrioritized(final RenderSectionManager instance, final RenderSection render) {
        return VSGameUtilsKt.squaredDistanceBetweenInclShips(world,
            render.getOriginX() + 8, render.getOriginY() + 8, render.getOriginZ() + 8,
            this.lastCameraPosition.getX(), this.lastCameraPosition.getX(), this.lastCameraPosition.getX()) <= NEARBY_REBUILD_DISTANCE;
    }

    @Inject(at = @At("TAIL"), method = "resetRenderLists")
    private void afterResetLists(final CallbackInfo ci) {
            shipRenderLists.values().forEach((renderList) ->{
            final Iterator var1 = renderList.iterator();

            while(var1.hasNext()) {
                ArrayDeque list = (ArrayDeque)var1.next();
                list.clear();
            }
        });
    }
}
