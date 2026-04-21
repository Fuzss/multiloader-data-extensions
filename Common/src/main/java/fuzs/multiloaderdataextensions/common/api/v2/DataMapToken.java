package fuzs.multiloaderdataextensions.common.api.v2;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * TODO move this to {@code api.v2}
 */
@ApiStatus.NonExtendable
public interface DataMapToken<R, T> {

    /**
     * {@return the key of the registry this data map is for}
     */
    ResourceKey<Registry<R>> registryKey();

    /**
     * {@return the ID of this data map}
     */
    Identifier id();

    /**
     * {@return the codec used to decode values}
     */
    Codec<T> codec();

    /**
     * {@return the codec used to sync values}
     */
    @Nullable Codec<T> networkCodec();

    /**
     * {@return {@code true} if this data map must be present on the client, and {@code false} otherwise}
     */
    boolean mandatorySync();
}
