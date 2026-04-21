package fuzs.multiloaderdataextensions.neoforge.impl.services;

import com.mojang.serialization.Codec;
import fuzs.multiloaderdataextensions.common.api.v2.DataMapToken;
import fuzs.multiloaderdataextensions.common.impl.services.CommonAbstractions;
import fuzs.multiloaderdataextensions.neoforge.api.v2.NeoForgeDataMapToken;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public final class NeoForgeAbstractions implements CommonAbstractions {

    @Override
    public <R, T> @Nullable T getData(DataMapToken<R, T> token, Holder<R> holder) {
        return holder.getData(NeoForgeDataMapToken.unwrap(token));
    }

    @Override
    public @Nullable <R, T> T getData(HolderLookup.RegistryLookup<R> registryLookup, DataMapToken<R, T> token, ResourceKey<R> resourceKey) {
        return registryLookup.getData(NeoForgeDataMapToken.unwrap(token), resourceKey);
    }

    @Override
    public <R, T> Map<ResourceKey<R>, T> getDataMap(Registry<R> registry, DataMapToken<R, T> token) {
        return registry.getDataMap(NeoForgeDataMapToken.unwrap(token));
    }

    @Override
    public <R, T> DataMapToken<R, T> register(Identifier id, ResourceKey<Registry<R>> registryKey, Codec<T> codec) {
        return this.register(DataMapType.builder(id, registryKey, codec).build());
    }

    @Override
    public <R, T> DataMapToken<R, T> register(Identifier id, ResourceKey<Registry<R>> registryKey, Codec<T> codec, Codec<T> networkCodec, boolean mandatory) {
        return this.register(DataMapType.builder(id, registryKey, codec).synced(networkCodec, mandatory).build());
    }

    private <R, T> DataMapToken<R, T> register(DataMapType<R, T> type) {
        IEventBus eventBus = ModLoadingContext.get().getActiveContainer().getEventBus();
        Objects.requireNonNull(eventBus, "mod event bus is null");
        eventBus.addListener((final RegisterDataMapTypesEvent evt) -> {
            evt.register(type);
        });
        return new NeoForgeDataMapToken<>(type);
    }
}
