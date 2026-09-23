/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package fuzs.multiloaderdataextensions.fabric.neoforge.resource;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.PreparableReloadListener;

/**
 * Fabric equivalent of NeoForge's {@code ContextAwareReloadListener}, without the conditions context. The registry
 * lookup is injected by the Fabric platform instead of NeoForge's reload system.
 */
public abstract class ContextAwareReloadListener implements PreparableReloadListener {
    private HolderLookup.Provider registryLookup = RegistryAccess.EMPTY;

    public void injectContext(HolderLookup.Provider registryLookup) {
        this.registryLookup = registryLookup;
    }

    /**
     * {@return the registry lookup held by this listener, or {@link RegistryAccess#EMPTY} if it is unavailable}
     */
    protected final HolderLookup.Provider getRegistryLookup() {
        return this.registryLookup;
    }

    /**
     * {@return serialization ops created from {@link #getRegistryLookup()}}
     */
    protected final RegistryOps<JsonElement> makeConditionalOps() {
        return RegistryOps.create(JsonOps.INSTANCE, this.registryLookup);
    }
}
