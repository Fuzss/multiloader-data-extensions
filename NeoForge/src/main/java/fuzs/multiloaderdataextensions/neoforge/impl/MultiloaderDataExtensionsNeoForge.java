package fuzs.multiloaderdataextensions.neoforge.impl;

import fuzs.multiloaderdataextensions.common.impl.MultiloaderDataExtensions;
import net.minecraft.DetectedVersion;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(MultiloaderDataExtensions.MOD_ID)
public class MultiloaderDataExtensionsNeoForge {

    public MultiloaderDataExtensionsNeoForge(ModContainer modContainer) {
        registerLoadingHandlers(modContainer.getEventBus());
    }

    private static void registerLoadingHandlers(IEventBus eventBus) {
        eventBus.addListener((final GatherDataEvent.Client event) -> {
            // Set only the major version here to stay compatible across different minor Minecraft versions.
            event.getGenerator()
                    .addProvider(true,
                            new PackMetadataGenerator(event.getGenerator()
                                    .getPackOutput()).add(PackMetadataSection.SERVER_TYPE,
                                    new PackMetadataSection(Component.literal(event.getModContainer()
                                            .getModInfo()
                                            .getDescription()),
                                            PackFormat.of(DetectedVersion.BUILT_IN.packVersion(PackType.SERVER_DATA)
                                                    .major()).minorRange())));
        });
    }
}
