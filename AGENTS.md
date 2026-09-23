# AGENTS.md

Guidance for agents working in this repository.

## Project

Multiloader Data Extensions ports NeoForge's data maps (and related systems) to Fabric, plus multi-loader
abstractions. Gradle subprojects: `Common`, `Fabric`, `NeoForge` (see `settings.gradle.kts`, composed by the
`fuzs.multiloader` convention plugins). Java 25 toolchain; use the Gradle wrapper.

The Fabric module vendors a subset of upstream loader classes (relocated) and keeps them in sync automatically.
**Most of the non-obvious work happens in that sync system and its patch conventions.**

## Build and verify

```sh
./gradlew :Fabric:build                    # compile + drift check + spotless + jar
./gradlew :NeoForge:build
./gradlew :Fabric:compileJava              # fast compile only
./gradlew :Fabric:checkVendoredSources     # vendored-source drift check (part of `check`)
./gradlew :Fabric:spotlessJavaCheck        # formatting / unused imports
```

Java version comes from the `sharedLibs` version catalog (`java` key); the NeoForge version comes from
`neoforge.version` in the same catalog.

## Vendored upstream sources (Fabric)

Vendored classes live under `Fabric/src/main/java/fuzs/multiloaderdataextensions/fabric/impl/neoforge/**`,
relocated from `net.neoforged.neoforge.*` to `fuzs.multiloaderdataextensions.fabric.impl.neoforge.*`. The
relocation target is derived from the mod group (`mod.group` / `packageName`), never hardcoded.

The sync is driven by `buildSrc` (`fuzs.multiloader.vendoredsources` package) and configured in
`Fabric/build.gradle.kts` as a
list of `SourceSpec`s (name, version, package root, relocation target, sources jar). All sync state lives in
`Fabric/patches/`:

- `patches/manifest` — the source of truth. Format: `<mode> <source> <path>` (or `local <path>`), where
  `<path>` is the **full package path** of the upstream file. Modes:
  - `generated` — fetched from the source's `:sources` artifact; if `patches/<path>.patch` exists it is
    applied, then the package is relocated. **Never edit these files by hand — edit the patch.**
  - `owned` — hand-maintained file that has an upstream counterpart. Upstream is fetched only to report drift.
  - `local` — hand-maintained file with no upstream counterpart.
- `patches/lock` — pinned source versions (`version.<source>=...`) and upstream hashes for `owned` files.
  Do not edit it by hand.
- `patches/<full/path>.patch` — patches, stored at the full package path (mirroring upstream).

Tasks:

- `./gradlew :Fabric:syncVendoredSources` — regenerate `generated` sources and update the lock.
- `./gradlew :Fabric:checkVendoredSources` — regenerate into `build/`, compare, and fail on any drift
  (out-of-date generated source, changed upstream `owned` hash, or stale lock).

Updating to a new upstream version:

1. Bump the version in the version catalog (and the matching `SourceSpec.version`).
2. `./gradlew :Fabric:syncVendoredSources`
3. Review any patch failures and `owned` drift reports; port manually where needed.
4. `./gradlew :Fabric:build`

## Patch conventions

Patches are authored against pristine upstream and stored at the full upstream path
(`patches/<full/path>.patch`, unified diff with `a/` `b/` labels). Keep them minimal and stable:

- **Do not add imports.** Reference relocated/helper types fully-qualified instead.
- **Do not make javadoc/comment-only changes.** Dangling `@link`/`@value` warnings are acceptable.
- Only **remove** imports for types that do not exist on the target platform.
- **Prefer adding methods/classes (shims) over patching upstream call sites.** Adding is more stable than
  patching. Examples already in use:
  - `owned` shim `.../neoforge/resource/ContextAwareReloadListener.java` lets `DataMapLoader` keep `extends
    ContextAwareReloadListener` and its `getRegistryLookup()` / `makeConditionalOps()` calls.
  - `owned` shim `.../neoforge/network/codec/NeoForgeStreamCodecs.java` (subset) keeps
    `NeoForgeStreamCodecs.registryKey()`.
  - vendored `.../neoforge/common/util/NeoForgeExtraCodecs.java` (generated, verbatim) keeps its codec helpers.
  - Fabric glue helpers live **outside** the mirror tree, e.g.
    `...fabric.impl.network.FriendlyByteBufHelper`, `...fabric.impl.registries.datamaps.DataMapSyncHelper`.
- **For large, self-contained removals, comment the block out with `/* ... */` instead of deleting it.** It
  yields a smaller patch whose hunks depend only on the block boundaries (not its body), so upstream edits
  inside the block do not break the patch. Do this only when the block contains no javadoc (`/** ... */`) —
  block comments cannot nest; delete outright in that case. The trade-off is that the generated file keeps the
  code as commented-out.
- **Do not vendor extra classes just to avoid patching call sites in non-relocatable modules.** Where classes
  keep their original package (e.g. a shared common module), vendoring more upstream classes increases the
  chance of clashes with other mods that bundle the same classes. Patch the call sites instead.

A new patch is created by diffing a pristine upstream copy against your edited copy, e.g.
`diff -u --label a/<full/path> --label b/<full/path> <upstream> <edited>`. Do not hand-write hunk headers.

## Conventions and gotchas

- The vendored tree is excluded from Spotless and marked generated in `.gitattributes`; do not reformat it.
- Keep the `multiloaderdataextensions$` method prefix — it avoids mixin-interface method collisions with
  other Fabric mods.
- `Common/src/main/resources/multiloaderdataextensions.classtweaker` injects our interfaces into vanilla
  classes. Update it when an injected interface is moved/renamed.
- Do not edit `generated` files (listed as `generated` in the manifest); edit the patch and run
  `syncVendoredSources`.
- Do not commit generated output by hand — always let `syncVendoredSources` write it, then run
  `checkVendoredSources`.
