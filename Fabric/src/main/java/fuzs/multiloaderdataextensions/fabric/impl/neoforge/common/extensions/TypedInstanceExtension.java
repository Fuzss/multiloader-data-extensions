/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package fuzs.multiloaderdataextensions.fabric.impl.neoforge.common.extensions;

import net.minecraft.core.TypedInstance;
import fuzs.multiloaderdataextensions.fabric.impl.neoforge.registries.datamaps.DataMapType;
import fuzs.multiloaderdataextensions.fabric.impl.neoforge.registries.datamaps.IWithData;
import org.jspecify.annotations.Nullable;

public interface TypedInstanceExtension<T> extends IWithData<T> {
    @Nullable
    @Override
    default <D> D multiloaderdataextensions$getData(DataMapType<T, D> type) {
        return self().typeHolder().multiloaderdataextensions$getData(type);
    }

    private TypedInstance<T> self() {
        return (TypedInstance<T>) this;
    }
}
