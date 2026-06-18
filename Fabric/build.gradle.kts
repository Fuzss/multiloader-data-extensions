plugins {
    id("fuzs.multiloader.multiloader-convention-plugins-fabric")
}

dependencies {
    modApi(sharedLibs.fabricapi.fabric)
}

multiloader {
    modFile {
        packagePrefix.set("impl")
        library.set(true)
    }

    mixins {
        mixin(
            "Holder\$ReferenceFabricMixin",
            "HolderFabricMixin",
            "HolderLookup\$RegistryLookup\$DelegateFabricMixin",
            "HolderLookup\$RegistryLookupFabricMixin",
            "MappedRegistryFabricMixin",
            "RegistryFabricMixin",
            "ReloadableServerResourcesFabricMixin",
            "TypedInstanceFabricMixin"
        )
    }
}
