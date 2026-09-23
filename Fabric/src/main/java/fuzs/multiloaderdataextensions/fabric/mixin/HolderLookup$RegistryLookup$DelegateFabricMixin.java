package fuzs.multiloaderdataextensions.fabric.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import fuzs.multiloaderdataextensions.fabric.neoforge.registries.datamaps.DataMapType;
import fuzs.multiloaderdataextensions.fabric.impl.registries.datamaps.ILookupWithData;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HolderLookup.RegistryLookup.Delegate.class)
interface HolderLookup$RegistryLookup$DelegateFabricMixin<T> extends HolderLookup.RegistryLookup<T>, ILookupWithData<T> {

    @Shadow
    RegistryLookup<T> parent();

    @Override
    default <A> @Nullable A multiloaderdataextensions$getData(DataMapType<T, A> type, ResourceKey<T> key) {
        return this.parent().multiloaderdataextensions$getData(type, key);
    }
}
