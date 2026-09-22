package fuzs.multiloaderdataextensions.fabric.mixin;

import net.minecraft.core.Holder;
import fuzs.multiloaderdataextensions.fabric.impl.neoforge.registries.datamaps.IWithData;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Holder.class)
interface HolderFabricMixin<T> extends IWithData<T> {

}
