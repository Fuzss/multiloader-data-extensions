package fuzs.neoforgedatapackextensions.api.v2;

import com.mojang.serialization.Codec;
import fuzs.neoforgedatapackextensions.api.v1.DataMapToken;
import fuzs.neoforgedatapackextensions.impl.services.CommonAbstractions;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class DataMapRegistrar {

    private DataMapRegistrar() {
        // NO-OP
    }

    public static <R, T> DataMapToken<R, T> register(Identifier id, ResourceKey<Registry<R>> registryKey, Codec<T> codec) {
        return CommonAbstractions.INSTANCE.register(id, registryKey, codec);
    }

    public static <R, T> DataMapToken<R, T> register(Identifier id, ResourceKey<Registry<R>> registryKey, Codec<T> codec, Codec<T> networkCodec, boolean mandatory) {
        return CommonAbstractions.INSTANCE.register(id, registryKey, codec, networkCodec, mandatory);
    }
}
