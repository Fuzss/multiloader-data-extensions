package fuzs.multiloaderdataextensions.fabric.impl.registries.datamaps;

import fuzs.multiloaderdataextensions.fabric.api.v2.DataMapsUpdatedCallback;
import fuzs.multiloaderdataextensions.fabric.neoforge.registries.RegistryManager;
import fuzs.multiloaderdataextensions.fabric.neoforge.registries.datamaps.DataMapType;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.Collections;
import java.util.Map;

/**
 * Bridges the NeoForge registry data-map internals (the {@code BaseMappedRegistry} field and the
 * {@code DataMapsUpdatedEvent} event bus) to the Fabric implementation, so the vendored NeoForge sources do not
 * need to know about either.
 */
public final class DataMapSyncHelper {
    private DataMapSyncHelper() {}

    @SuppressWarnings("unchecked")
    public static <T> Map<DataMapType<T, ?>, Map<ResourceKey<T>, ?>> getDataMaps(Registry<T> registry) {
        return ((IRegistryWithData<T>) registry).multiloaderdataextensions$getDataMaps();
    }

    public static void fireServerReload(RegistryAccess registryAccess, Registry<?> registry) {
        DataMapsUpdatedCallback.EVENT.invoker()
                .onDataMapsUpdated(registryAccess, registry, DataMapsUpdatedCallback.UpdateCause.SERVER_RELOAD);
    }

    public static <T> void applyClientSync(RegistryAccess registryAccess, ResourceKey<? extends Registry<T>> registryKey, Map<Identifier, Map<ResourceKey<T>, ?>> dataMaps) {
        Registry<T> registry = registryAccess.lookupOrThrow(registryKey);
        Map<DataMapType<T, ?>, Map<ResourceKey<T>, ?>> registryDataMaps = getDataMaps(registry);
        registryDataMaps.clear();
        dataMaps.forEach((attachKey, maps) -> registryDataMaps.put(RegistryManager.getDataMap(registryKey, attachKey), Collections.unmodifiableMap(maps)));
        DataMapsUpdatedCallback.EVENT.invoker()
                .onDataMapsUpdated(registryAccess, registry, DataMapsUpdatedCallback.UpdateCause.CLIENT_SYNC);
    }
}
