package org.valkyrienskies.mod.mixin.mod_compat.sodium;

import me.jellysquid.mods.sodium.client.render.chunk.map.ChunkTracker;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = ChunkTracker.class, remap = false)
public class MixinChunkTracker {
//    @Shadow
//    @Final
//    private Long2IntOpenHashMap chunkStatus;

    //Remove this for now... the method was removed in SodiumWorldRenderer
//    @Inject(method = "onLightDataAdded", at = @At("HEAD"), cancellable = true)
//    private void cancelDataLight(final int x, final int z, final CallbackInfo ci) {
//        final long key = ChunkPos.asLong(x, z);
//        final int existingFlags = this.chunkStatus.get(key);
//        if ((existingFlags & 1) == 0) {
//            ci.cancel(); // Cancel instead of throwing an error for now
//        }
//    }

}
