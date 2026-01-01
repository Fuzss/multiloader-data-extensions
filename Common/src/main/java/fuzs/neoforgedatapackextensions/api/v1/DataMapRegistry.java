package fuzs.neoforgedatapackextensions.api.v1;

import com.mojang.serialization.Codec;
import fuzs.neoforgedatapackextensions.impl.services.CommonAbstractions;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

@Deprecated(forRemoval = true)
public interface DataMapRegistry {
    DataMapRegistry INSTANCE = new DataMapRegistry() {
        @Override
        public @Nullable <R, T> T getData(DataMapToken<R, T> token, Holder<R> holder) {
            return CommonAbstractions.INSTANCE.getData(token, holder);
        }

        @Override
        public <R, T> DataMapToken<R, T> register(Identifier id, ResourceKey<Registry<R>> registryKey, Codec<T> codec) {
            return CommonAbstractions.INSTANCE.register(id, registryKey, codec);
        }

        @Override
        public <R, T> DataMapToken<R, T> register(Identifier id, ResourceKey<Registry<R>> registryKey, Codec<T> codec, Codec<T> networkCodec, boolean mandatory) {
            return CommonAbstractions.INSTANCE.register(id, registryKey, codec, networkCodec, mandatory);
        }
    };

    <R, T> @Nullable T getData(DataMapToken<R, T> token, Holder<R> holder);

    <R, T> DataMapToken<R, T> register(Identifier id, ResourceKey<Registry<R>> registryKey, Codec<T> codec);

    <R, T> DataMapToken<R, T> register(Identifier id, ResourceKey<Registry<R>> registryKey, Codec<T> codec, Codec<T> networkCodec, boolean mandatory);
}
