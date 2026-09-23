/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package fuzs.multiloaderdataextensions.fabric.neoforge.network.codec;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

/**
 * Fabric subset of NeoForge's {@code NeoForgeStreamCodecs}, providing only the codecs used by the vendored sources.
 * The remaining codecs depend on NeoForge's patched {@code RegistryFriendlyByteBuf} and connection handling.
 */
public final class NeoForgeStreamCodecs {
    private NeoForgeStreamCodecs() {}

    /**
     * Creates a stream codec to encode and decode a {@link ResourceKey} that identifies a registry.
     */
    public static <B extends FriendlyByteBuf> StreamCodec<B, ResourceKey<? extends Registry<?>>> registryKey() {
        return new StreamCodec<>() {
            @Override
            public ResourceKey<? extends Registry<?>> decode(B buf) {
                return ResourceKey.createRegistryKey(buf.readIdentifier());
            }

            @Override
            public void encode(B buf, ResourceKey<? extends Registry<?>> value) {
                buf.writeIdentifier(value.identifier());
            }
        };
    }
}
