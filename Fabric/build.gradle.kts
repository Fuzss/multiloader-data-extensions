import neoforgesync.CheckNeoForgeSourcesTask
import neoforgesync.SyncNeoForgeSourcesTask
import org.gradle.api.artifacts.VersionCatalogsExtension

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

val neoforgeSourceVersion: String = extensions.getByType<VersionCatalogsExtension>()
    .named("sharedLibs")
    .findVersion("neoforge.version")
    .get()
    .requiredVersion

val vendoredPackagePrefix = "fuzs.multiloaderdataextensions.fabric.impl.neoforge"

repositories {
    maven("https://maven.neoforged.net/releases/") {
        name = "NeoForged"
        content {
            includeGroup("net.neoforged")
        }
    }
}

val neoforgeSources by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    neoforgeSources("net.neoforged:neoforge:$neoforgeSourceVersion:sources")
}

val syncNeoForgeSources = tasks.register<SyncNeoForgeSourcesTask>("syncNeoForgeSources") {
    group = "neoforge sync"
    description = "Regenerates the vendored NeoForge sources from upstream plus the committed patches."

    manifestFile.set(layout.projectDirectory.file("neoforge-sync.manifest"))
    patchesDir.set(layout.projectDirectory.dir("neoforge-patches"))
    packagePrefix.set(vendoredPackagePrefix)
    neoforgeVersion.set(neoforgeSourceVersion)
    committedDir.set(layout.projectDirectory.dir("src/main/java/${vendoredPackagePrefix.replace('.', '/')}"))
    workDir.set(layout.buildDirectory.dir("neoforge-sync"))
    lockFile.set(layout.projectDirectory.file("neoforge-sync.lock"))
    upstreamSources.from(neoforgeSources)
}

val checkNeoForgeSources = tasks.register<CheckNeoForgeSourcesTask>("checkNeoForgeSources") {
    group = "neoforge sync"
    description = "Verifies the vendored NeoForge sources match upstream plus the committed patches."

    manifestFile.set(layout.projectDirectory.file("neoforge-sync.manifest"))
    patchesDir.set(layout.projectDirectory.dir("neoforge-patches"))
    packagePrefix.set(vendoredPackagePrefix)
    neoforgeVersion.set(neoforgeSourceVersion)
    committedDir.set(layout.projectDirectory.dir("src/main/java/${vendoredPackagePrefix.replace('.', '/')}"))
    workDir.set(layout.buildDirectory.dir("neoforge-sync-check"))
    lockFile.set(layout.projectDirectory.file("neoforge-sync.lock"))
    upstreamSources.from(neoforgeSources)
}

tasks.named("check") {
    dependsOn(checkNeoForgeSources)
}

// Generated files must not be reformatted, otherwise the checked-in sources diverge from a fresh sync.
spotless {
    java {
        targetExclude("src/main/java/${vendoredPackagePrefix.replace('.', '/')}/**")
    }
}
