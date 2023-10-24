package org.valkyrienskies.mod.compat;

import me.jellysquid.mods.sodium.client.render.SodiumWorldRenderer;
import org.valkyrienskies.mod.mixin.ValkyrienCommonMixinConfigPlugin;
import org.valkyrienskies.mod.mixin.mod_compat.sodium.accessor.SodiumWorldRendererAccessor;

public class SodiumCompat {

    public static void onChunkAdded(final int x, final int z) {
        if (ValkyrienCommonMixinConfigPlugin.getVSRenderer() == VSRenderer.SODIUM) {
            ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager().onChunkAdded(x, z);
        }
    }

    public static void onChunkRemoved(final int x, final int z) {
        if (ValkyrienCommonMixinConfigPlugin.getVSRenderer() == VSRenderer.SODIUM) {
            ((SodiumWorldRendererAccessor) SodiumWorldRenderer.instance()).getRenderSectionManager().onChunkRemoved(x, z);
        }
    }

}
