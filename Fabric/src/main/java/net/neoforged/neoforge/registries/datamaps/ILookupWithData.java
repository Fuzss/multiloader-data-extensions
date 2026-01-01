package net.neoforged.neoforge.registries.datamaps;

import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public interface ILookupWithData<T> {
    /**
     * {@return the data map value attached with the object with the key, or {@code null} if there's no attached value}
     *
     * @param type the type of the data map
     * @param key  the object to get the value for
     * @param <A>  the data type
     */
    default <A> @Nullable A neoforgedatapackextensions$getData(DataMapType<T, A> type, ResourceKey<T> key) {
        return null;
    }
}
