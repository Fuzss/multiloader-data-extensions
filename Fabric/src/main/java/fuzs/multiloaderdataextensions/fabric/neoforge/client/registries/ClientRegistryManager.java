/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package fuzs.multiloaderdataextensions.fabric.neoforge.client.registries;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import fuzs.multiloaderdataextensions.fabric.neoforge.network.payload.KnownRegistryDataMapsPayload;
import fuzs.multiloaderdataextensions.fabric.neoforge.network.payload.KnownRegistryDataMapsReplyPayload;
import fuzs.multiloaderdataextensions.fabric.neoforge.network.payload.RegistryDataMapSyncPayload;
import fuzs.multiloaderdataextensions.fabric.neoforge.registries.RegistryManager;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

@ApiStatus.Internal
public class ClientRegistryManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <R> void handleDataMapSync(final RegistryDataMapSyncPayload<R> payload, final net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context context) {
        context.client().submit(() -> {
            try {
                var regAccess = Minecraft.getInstance().level.registryAccess();
                fuzs.multiloaderdataextensions.fabric.impl.registries.datamaps.DataMapSyncHelper.applyClientSync(regAccess, payload.registryKey(), payload.dataMaps());
            } catch (Throwable t) {
                LOGGER.error("Failed to handle registry data map sync: ", t);
                context.responseSender().disconnect(Component.translatable("neoforge.network.data_maps.failed", payload.registryKey().identifier().toString(), t.toString()));
            }
        });
    }

    public static void handleKnownDataMaps(final KnownRegistryDataMapsPayload payload, final net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking.Context context) {
        record MandatoryEntry(ResourceKey<? extends Registry<?>> registry, Identifier id) {}
        final Set<MandatoryEntry> ourMandatory = new HashSet<>();
        RegistryManager.getDataMaps().forEach((reg, values) -> values.values().forEach(attach -> {
            if (attach.mandatorySync()) {
                ourMandatory.add(new MandatoryEntry(reg, attach.id()));
            }
        }));

        final Set<MandatoryEntry> theirMandatory = new HashSet<>();
        payload.dataMaps().forEach((reg, values) -> values.forEach(attach -> {
            if (attach.mandatory()) {
                theirMandatory.add(new MandatoryEntry(reg, attach.id()));
            }
        }));

        final List<Component> messages = new ArrayList<>();
        final var missingOur = Sets.difference(ourMandatory, theirMandatory);
        if (!missingOur.isEmpty()) {
            messages.add(Component.translatable("neoforge.network.data_maps.missing_our", Component.literal(missingOur.stream()
                    .map(e -> e.id() + " (" + e.registry().identifier() + ")")
                    .collect(Collectors.joining(", "))).withStyle(ChatFormatting.GOLD)));
        }

        final var missingTheir = Sets.difference(theirMandatory, ourMandatory);
        if (!missingTheir.isEmpty()) {
            messages.add(Component.translatable("neoforge.network.data_maps.missing_their", Component.literal(missingTheir.stream()
                    .map(e -> e.id() + " (" + e.registry().identifier() + ")")
                    .collect(Collectors.joining(", "))).withStyle(ChatFormatting.GOLD)));
        }

        if (!messages.isEmpty()) {
            MutableComponent message = Component.empty();
            final var itr = messages.iterator();
            while (itr.hasNext()) {
                message = message.append(itr.next());
                if (itr.hasNext()) {
                    message = message.append("\n");
                }
            }

            context.responseSender().disconnect(message);
            return;
        }

        final var known = new HashMap<ResourceKey<? extends Registry<?>>, Collection<Identifier>>();
        RegistryManager.getDataMaps().forEach((key, vals) -> known.put(key, vals.keySet()));
        context.responseSender().sendPacket(new KnownRegistryDataMapsReplyPayload(known));
    }
}
