package fuzs.neoforgedatapackextensions.fabric.impl.services;

import com.mojang.serialization.Codec;
import fuzs.neoforgedatapackextensions.api.v1.DataMapToken;
import fuzs.neoforgedatapackextensions.fabric.api.v1.FabricDataMapToken;
import fuzs.neoforgedatapackextensions.impl.services.CommonAbstractions;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.IRegistryExtension;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.ILookupWithData;
import net.neoforged.neoforge.registries.datamaps.IWithData;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public final class FabricAbstractions implements CommonAbstractions {

    @Override
    public <R, T> @Nullable T getData(DataMapToken<R, T> token, Holder<R> holder) {
        return ((IWithData<R>) holder).neoforgedatapackextensions$getData(FabricDataMapToken.unwrap(token));
    }

    @Override
    public @Nullable <R, T> T getData(HolderLookup.RegistryLookup<R> registryLookup, DataMapToken<R, T> token, ResourceKey<R> resourceKey) {
        return ((ILookupWithData<R>) registryLookup).neoforgedatapackextensions$getData(FabricDataMapToken.unwrap(token),
                resourceKey);
    }

    @Override
    public <R, T> Map<ResourceKey<R>, T> getDataMap(Registry<R> registry, DataMapToken<R, T> token) {
        return ((IRegistryExtension<R>) registry).neoforgedatapackextensions$getDataMap(FabricDataMapToken.unwrap(token));
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
        RegistryManager.registerDataMap(type);
        return new FabricDataMapToken<>(type);
    }
}
