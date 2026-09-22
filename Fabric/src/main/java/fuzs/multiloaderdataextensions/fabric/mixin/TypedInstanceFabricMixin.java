package fuzs.multiloaderdataextensions.fabric.mixin;

import net.minecraft.core.TypedInstance;
import fuzs.multiloaderdataextensions.fabric.impl.neoforge.common.extensions.TypedInstanceExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TypedInstance.class)
public interface TypedInstanceFabricMixin<T> extends TypedInstanceExtension<T> {

}
