package fuzs.multiloaderdataextensions.common.impl;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiloaderDataExtensions {
    public static final String MOD_ID = "multiloaderdataextensions";
    public static final String MOD_NAME = "Multiloader Data Extensions";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
