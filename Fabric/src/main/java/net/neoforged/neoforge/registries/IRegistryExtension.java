/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.ILookupWithData;

import java.util.Map;

/**
 * An extension for {@link Registry}, adding some additional functionality to vanilla registries, such as callbacks and
 * ID limits.
 *
 * @param <T> the type of registry entries
 */
public interface IRegistryExtension<T> extends ILookupWithData<T> {

    /**
     * {@return the data map of the given {@code type}}
     *
     * @param <A> the data type
     */
    default <A> Map<ResourceKey<T>, A> neoforgedatapackextensions$getDataMap(DataMapType<T, A> type) {
        return Map.of();
    }
}
