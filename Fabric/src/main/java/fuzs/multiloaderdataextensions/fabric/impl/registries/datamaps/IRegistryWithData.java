package fuzs.multiloaderdataextensions.fabric.impl.registries.datamaps;

import net.minecraft.resources.ResourceKey;
import fuzs.multiloaderdataextensions.fabric.impl.neoforge.registries.datamaps.DataMapType;

import java.util.Map;

public interface IRegistryWithData<T> {
    Map<DataMapType<T, ?>, Map<ResourceKey<T>, ?>> multiloaderdataextensions$getDataMaps();
}
