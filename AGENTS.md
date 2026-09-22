# AGENTS.md

Guidance for agents working in this repository.

## Project

Multiloader Data Extensions ports NeoForge's data maps (and related systems) to Fabric, plus multi-loader
abstractions. Gradle subprojects: `Common`, `Fabric`, `NeoForge` (see `settings.gradle.kts`, composed by the
`fuzs.multiloader` convention plugins). Java 25 toolchain; use the Gradle wrapper.

The Fabric module vendors a subset of NeoForge's own classes (relocated) and keeps them in sync with upstream
automatically. **Most of the non-obvious work happens in that sync system and its patch conventions.**

## Build and verify

```sh
./gradlew :Fabric:build            # compile + drift check + spotless + jar
./gradlew :NeoForge:build
./gradlew :Fabric:compileJava      # fast compile only
./gradlew :Fabric:checkNeoForgeSources   # vendored-source drift check (part of `check`)
./gradlew :Fabric:spotlessJavaCheck      # formatting / unused imports
```

Java version comes from the `sharedLibs` version catalog (`java` key); NeoForge version comes from
`neoforge.version` in the same catalog.

## Vendored NeoForge sources (Fabric)

Vendored classes live under `Fabric/src/main/java/fuzs/multiloaderdataextensions/fabric/impl/neoforge/**`,
relocated from `net.neoforged.neoforge.*` to
`fuzs.multiloaderdataextensions.fabric.impl.neoforge.*`.

`Fabric/neoforge-sync.manifest` is the source of truth. Three modes:

- `generated` — fetched from the `net.neoforged:neoforge:<version>:sources` artifact; if a matching
  `Fabric/neoforge-patches/<path>.patch` exists it is applied, then the package is relocated.
  **Never edit these files by hand — edit the patch.**
- `owned` — hand-maintained file that has an upstream counterpart. Upstream is fetched only to report drift.
- `local` — hand-maintained file with no upstream counterpart.

`Fabric/neoforge-sync.lock` records the pinned NeoForge version and upstream hashes for `owned` files.
Do not edit it by hand.

Tasks:

- `./gradlew :Fabric:syncNeoForgeSources` — regenerate `generated` sources and update the lock.
- `./gradlew :Fabric:checkNeoForgeSources` — regenerate into `build/`, compare, and fail on any drift
  (out-of-date generated source, changed upstream `owned` hash, or stale lock).

Updating to a new NeoForge version:

1. Bump `neoforge.version` in the shared catalog.
2. `./gradlew :Fabric:syncNeoForgeSources`
3. Review any patch failures and `owned` drift reports; port manually where needed.
4. `./gradlew :Fabric:build`

## Patch conventions

Patches are authored against pristine upstream and mirror upstream paths
(`Fabric/neoforge-patches/<path>.patch`, unified diff with `a/` `b/` labels). Keep them minimal and stable:

- **Do not add imports.** Reference relocated/helper types fully-qualified instead.
- **Do not make javadoc/comment-only changes.** Dangling `@link`/`@value` warnings are acceptable.
- Only **remove** imports for types that do not exist on Fabric.
- **Prefer adding methods/classes (shims) over patching upstream call sites.** Adding is more stable than
  patching. Examples already in use:
  - `owned` shim `resource/ContextAwareReloadListener.java` lets `DataMapLoader` keep `extends
    ContextAwareReloadListener` and its `getRegistryLookup()` / `makeConditionalOps()` calls.
  - `owned` shim `network/codec/NeoForgeStreamCodecs.java` (subset) keeps `NeoForgeStreamCodecs.registryKey()`.
  - vendored `common/util/NeoForgeExtraCodecs.java` (generated, verbatim) keeps its codec helpers in use.
  - Fabric glue helpers live **outside** the mirror tree, e.g.
    `...fabric.impl.network.FriendlyByteBufHelper`, `...fabric.impl.registries.datamaps.DataMapSyncHelper`.

A new patch is created by diffing a pristine upstream copy against your edited copy, e.g.
`diff -u --label a/<path> --label b/<path> <upstream> <edited>`.

## Conventions and gotchas

- The vendored tree is excluded from Spotless and marked generated in `.gitattributes`; do not reformat it.
- Keep the `multiloaderdataextensions$` method prefix — it avoids mixin-interface method collisions with
  other Fabric mods.
- `Common/src/main/resources/multiloaderdataextensions.classtweaker` injects our interfaces into vanilla
  classes. Update it when an injected interface is moved/renamed.
- Do not edit `generated` files (`...impl/neoforge/**` listed as `generated` in the manifest); edit the patch
  and run `syncNeoForgeSources`.
- Do not commit generated output by hand — always let `syncNeoForgeSources` write it, then run
  `checkNeoForgeSources`.
