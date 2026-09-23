package fuzs.multiloaderdataextensions.fabric.impl.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import fuzs.multiloaderdataextensions.fabric.neoforge.client.registries.ClientRegistryManager;
import fuzs.multiloaderdataextensions.fabric.neoforge.network.payload.KnownRegistryDataMapsPayload;
import fuzs.multiloaderdataextensions.fabric.neoforge.network.payload.RegistryDataMapSyncPayload;

public class MultiloaderDataExtensionsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerNetworkMessages();
    }

    private static void registerNetworkMessages() {
        ClientConfigurationNetworking.registerGlobalReceiver(KnownRegistryDataMapsPayload.TYPE,
                ClientRegistryManager::handleKnownDataMaps);
        ClientPlayNetworking.registerGlobalReceiver(RegistryDataMapSyncPayload.TYPE,
                ClientRegistryManager::handleDataMapSync);
    }
}
