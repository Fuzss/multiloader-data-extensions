package fuzs.multiloaderdataextensions.common.api.v2;

import fuzs.multiloaderdataextensions.common.impl.services.CommonAbstractions;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public final class DataMapLookup {

    private DataMapLookup() {
        // NO-OP
    }

    public static <R, T> @Nullable T getData(DataMapToken<R, T> token, Holder<R> holder) {
        return CommonAbstractions.INSTANCE.getData(token, holder);
    }

    public static <R, T> @Nullable T getData(HolderLookup.RegistryLookup<R> registryLookup, DataMapToken<R, T> token, ResourceKey<R> resourceKey) {
        return CommonAbstractions.INSTANCE.getData(registryLookup, token, resourceKey);
    }

    public static <R, T> Map<ResourceKey<R>, T> getDataMap(Registry<R> registry, DataMapToken<R, T> token) {
        return CommonAbstractions.INSTANCE.getDataMap(registry, token);
    }
}
