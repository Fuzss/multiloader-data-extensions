package fuzs.multiloaderdataextensions.fabric.mixin;

import fuzs.multiloaderdataextensions.fabric.impl.registries.datamaps.IRegistryWithData;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.IRegistryExtension;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.IdentityHashMap;
import java.util.Map;

@Mixin(MappedRegistry.class)
abstract class MappedRegistryFabricMixin<T> implements IRegistryExtension<T>, IRegistryWithData<T> {
    @Unique
    final Map<DataMapType<T, ?>, Map<ResourceKey<T>, ?>> multiloaderdataextensions$dataMaps = new IdentityHashMap<>();

    @Override
    public <A> @Nullable A multiloaderdataextensions$getData(DataMapType<T, A> type, ResourceKey<T> key) {
        final var innerMap = this.multiloaderdataextensions$dataMaps.get(type);
        return innerMap == null ? null : (A) innerMap.get(key);
    }

    @Override
    public <A> Map<ResourceKey<T>, A> multiloaderdataextensions$getDataMap(DataMapType<T, A> type) {
        return (Map<ResourceKey<T>, A>) this.multiloaderdataextensions$dataMaps.getOrDefault(type, Map.of());
    }

    @Override
    public Map<DataMapType<T, ?>, Map<ResourceKey<T>, ?>> multiloaderdataextensions$getDataMaps() {
        return this.multiloaderdataextensions$dataMaps;
    }
}
