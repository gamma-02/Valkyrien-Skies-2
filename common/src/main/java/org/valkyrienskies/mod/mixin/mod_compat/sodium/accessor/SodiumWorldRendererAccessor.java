package org.valkyrienskies.mod.mixin.mod_compat.sodium.accessor;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.RenderSectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SodiumWorldRenderer.class)
public interface SodiumWorldRendererAccessor {

    @Accessor(value = "renderSectionManager")
    RenderSectionManager getRenderSectionManager();
}
