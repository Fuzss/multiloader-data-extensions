import fuzs.multiloader.extension.mod
import fuzs.multiloader.extension.packageName
import fuzs.multiloader.vendoredsources.CheckVendoredSourcesTask
import fuzs.multiloader.vendoredsources.SourceSpec
import fuzs.multiloader.vendoredsources.SyncVendoredSourcesTask
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

// Relocation target for the vendored NeoForge sources, derived from the mod group instead of hardcoded.
val vendoredPackagePrefix = "${mod.group}.${packageName}.neoforge"

repositories {
    maven("https://maven.neoforged.net/releases/") {
        name = "NeoForge"
        content {
            includeGroup("net.neoforged")
        }
    }
}

val neoforgeSources = configurations.create("neoforgeSources") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    add(neoforgeSources.name, "net.neoforged:neoforge:$neoforgeSourceVersion:sources")
}

val vendoredSources = provider {
    listOf(
        SourceSpec(
            name = "neoforge",
            version = neoforgeSourceVersion,
            packageRoot = "net.neoforged.neoforge",
            relocateTo = vendoredPackagePrefix,
            sourcesJar = neoforgeSources.singleFile,
        )
    )
}

val syncVendoredSources = tasks.register<SyncVendoredSourcesTask>("syncVendoredSources") {
    group = "vendored sources"
    description = "Regenerates the vendored upstream sources from the committed patches."

    manifestFile.set(layout.projectDirectory.file("patches/manifest"))
    patchesDir.set(layout.projectDirectory.dir("patches"))
    outputDir.set(layout.projectDirectory.dir("src/main/java"))
    workDir.set(layout.buildDirectory.dir("vendored-sources"))
    lockFile.set(layout.projectDirectory.file("patches/lock"))
    sources.set(vendoredSources)
}

val checkVendoredSources = tasks.register<CheckVendoredSourcesTask>("checkVendoredSources") {
    group = "vendored sources"
    description = "Verifies the vendored upstream sources match upstream plus the committed patches."

    manifestFile.set(layout.projectDirectory.file("patches/manifest"))
    patchesDir.set(layout.projectDirectory.dir("patches"))
    committedDir.set(layout.projectDirectory.dir("src/main/java"))
    workDir.set(layout.buildDirectory.dir("vendored-sources-check"))
    lockFile.set(layout.projectDirectory.file("patches/lock"))
    sources.set(vendoredSources)
}

tasks.named("check") {
    dependsOn(checkVendoredSources)
}

// Generated files must not be reformatted, otherwise the checked-in sources diverge from a fresh sync.
spotless {
    java {
        targetExclude("src/main/java/${vendoredPackagePrefix.replace('.', '/')}/**")
    }
}
