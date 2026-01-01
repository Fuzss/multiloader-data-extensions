package fuzs.neoforgedatapackextensions.impl.services;

import com.mojang.serialization.Codec;
import fuzs.neoforgedatapackextensions.api.v1.DataMapToken;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public interface CommonAbstractions {
    CommonAbstractions INSTANCE = ServiceProviderHelper.load(CommonAbstractions.class);

    <R, T> @Nullable T getData(DataMapToken<R, T> token, Holder<R> holder);

    <R, T> @Nullable T getData(HolderLookup.RegistryLookup<R> registryLookup, DataMapToken<R, T> token, ResourceKey<R> resourceKey);

    <R, T> Map<ResourceKey<R>, T> getDataMap(Registry<R> registry, DataMapToken<R, T> token);

    <R, T> DataMapToken<R, T> register(Identifier id, ResourceKey<Registry<R>> registryKey, Codec<T> codec);

    <R, T> DataMapToken<R, T> register(Identifier id, ResourceKey<Registry<R>> registryKey, Codec<T> codec, Codec<T> networkCodec, boolean mandatory);
}
