# Vendored source patches

This directory is the source of truth for the NeoForge classes that the Fabric module vendors. It lets the
vendored sources be regenerated from upstream at any time by applying small, reviewable patches instead of
maintaining copies by hand.

Everything here is operated through two Gradle tasks. You do not need any external tooling beyond the Gradle
wrapper, a `patch` binary (macOS/Linux provide one), and network access to resolve the upstream sources jar
(cached by Gradle after the first run).

## Pipeline overview

```
            net.neoforged:neoforge:<version>:sources        (upstream, resolved by Gradle)
                          |
                          v
                  extract into build/vendored-sources/upstream/neoforge/
                          |
        manifest entry?   |   apply patches/<full/path>.patch   (if present)
                          v
                  relocate package (SourceSpec.relocateTo)
                          |
                          v
   Fabric/src/main/java/fuzs/multiloaderdataextensions/fabric/impl/neoforge/...   (committed)
```

1. The source jar is extracted to a scratch directory.
2. Each `generated` file listed in `manifest` is copied from the extracted upstream, its patch (if any) is
   applied with `patch -p1`, and then the package is relocated.
3. The result is written into the Fabric source tree; commit it like any other generated file.
4. `checkVendoredSources` performs the same steps into a scratch directory and fails if the committed sources
   differ.

## Files in this directory

- `manifest` — lists every vendored file and how it is handled. **Source of truth.**
- `lock` — records the pinned upstream version(s) and the upstream hashes of `owned` files. Generated; do not
  edit by hand.
- `<full/path>.patch` — a unified diff for one file, stored at the file's **full package path** (mirroring
  upstream), e.g. `net/neoforged/neoforge/registries/DataMapLoader.java.patch`.

## Gradle tasks

Run from the repository root.

```sh
# Regenerate the vendored sources and refresh the lock file.
./gradlew :Fabric:syncVendoredSources

# Verify the committed sources match a fresh sync (also runs as part of `check`/`build`).
./gradlew :Fabric:checkVendoredSources

# Full build (compiles, runs checkVendoredSources, Spotless, jars).
./gradlew :Fabric:build
```

`syncVendoredSources` prints one line per manifest entry, tagged `[patched]`, `[verbatim]`, `[unchanged]` or
`[changed]`, plus a summary. Add `--info` to also see the raw `patch` output for each patched file.

`checkVendoredSources` is quiet on success (`Vendored sources are in sync.`). On failure it lists every problem
and tells you to run the sync task.

Scratch output (safe to delete, not committed):

- `Fabric/build/vendored-sources/upstream/neoforge/<full/path>` — pristine extracted upstream (use this when
  authoring patches).
- `Fabric/build/vendored-sources/staging/<full/path>` — upstream with the patch applied, before relocation.
- `Fabric/build/vendored-sources/owned-upstream/<full/path>` — copy of an `owned` file's upstream counterpart,
  written when it changes so you can review it.

## Configuration

The sync is configured in `Fabric/build.gradle.kts` as a list of `SourceSpec`s passed to the tasks:

```kotlin
SourceSpec(
    name = "neoforge",                                  // referenced by manifest entries
    version = neoforgeSourceVersion,                    // recorded in lock
    packageRoot = "net.neoforged.neoforge",             // dot-form upstream package root
    relocateTo = vendoredPackagePrefix,                 // dot-form target, or null to keep the package
    sourcesJar = neoforgeSources.singleFile,            // the upstream sources jar
)
```

The relocation target is derived from the mod, never hardcoded:
`"${mod.group}.${packageName}.impl.neoforge"`.

To add another upstream source (e.g. FML or Minecraft Forge), add a `SourceSpec` with its own
`packageRoot`/`relocateTo`/`sourcesJar` and reference it by `name` in the manifest.

## `manifest` format

One entry per line; `#` starts a comment; blank lines are ignored. Whitespace-separated.

```
<mode> <source> <path>     # generated and owned
local <path>               # local (no upstream counterpart)
```

- `<source>` — the `SourceSpec.name` the file comes from.
- `<path>` — the **full package path** of the upstream file, e.g.
  `net/neoforged/neoforge/registries/DataMapLoader.java`.

Modes:

- **`generated`** — fetched from upstream, patch applied if `patches/<path>.patch` exists, then relocated and
  written to the source tree. Never edit these files by hand; edit the patch instead.
- **`owned`** — a hand-maintained file that has an upstream counterpart. The sync never writes it; it only
  fetches upstream to report drift (its hash is recorded in `lock`).
- **`local`** — a hand-maintained file with no upstream counterpart. Ignored by the sync.

## `lock` format

```
version.<source>=<version>            # pinned upstream version per source
<full/path>=<sha256>                  # upstream hash of each `owned` file
```

Generated by `syncVendoredSources`. It must be committed. `checkVendoredSources` fails if it is missing, if a
source's version changed, or if an `owned` file's upstream hash changed.

## Authoring and editing patches

Patches are plain unified diffs applied with `patch -p1`, so the labels must be `a/<full/path>` and
`b/<full/path>`. Always author them against **pristine upstream**, never against the relocated/generated file.

### Create or update a patch for a file

1. Populate the upstream sources (any sync run does this):

   ```sh
   ./gradlew :Fabric:syncVendoredSources
   ```

2. Copy the pristine upstream file to a scratch location (keep the upstream copy untouched):

   ```sh
   P=net/neoforged/neoforge/registries/DataMapLoader.java
   cp "Fabric/build/vendored-sources/upstream/neoforge/$P" /tmp/DataMapLoader.java
   ```

3. If a patch already exists, apply it to the scratch copy so you start from the current edits instead of
   reproducing them by hand:

   ```sh
   patch /tmp/DataMapLoader.java "Fabric/patches/$P.patch"
   ```

   `patch` prints `patching file ...` and exits `0` on success. This is the same operation the sync performs.
   It fails when the upstream file changed since the patch was written (the hunks no longer match); in that
   case `patch` writes a `.rej` file with the rejected hunks next to the scratch copy. Apply those hunks
   manually, or fall back to editing the pristine file. See "Resolve a failed patch" below.

4. Edit the scratch copy.

5. Produce the patch with matching labels and place it at the full package path:

   ```sh
   mkdir -p "Fabric/patches/$(dirname "$P")"
   diff -u --label "a/$P" --label "b/$P" \
     "Fabric/build/vendored-sources/upstream/neoforge/$P" /tmp/DataMapLoader.java \
     > "Fabric/patches/$P.patch"
   ```

   The diff is always taken against the pristine upstream copy, so the result contains only your intended
   changes and never the package rename.

6. Regenerate and validate:

   ```sh
   ./gradlew :Fabric:syncVendoredSources :Fabric:checkVendoredSources
   ```

If you prefer not to touch the `.patch` file directly, the steps above are also the way to edit an existing
patch: apply it to the scratch copy, adjust, and re-diff.

### Patch conventions

Keep patches minimal and stable; they are re-applied on every upstream update, so small diffs conflict less:

- **Do not add imports.** Reference relocated or helper types fully-qualified instead. This keeps the patch out
  of the import block, which changes frequently upstream.
- **Do not make javadoc/comment-only changes.** Dangling `@link`/`@value` warnings are acceptable.
- Only **remove** imports for types that do not exist on Fabric.
- **Prefer adding a method or class over patching a call site.** Adding is more stable than patching. Existing
  examples:
  - `owned` shim `net/neoforged/neoforge/resource/ContextAwareReloadListener.java` lets `DataMapLoader` keep
    `extends ContextAwareReloadListener` and its `getRegistryLookup()`/`makeConditionalOps()` calls.
  - `owned` shim `net/neoforged/neoforge/network/codec/NeoForgeStreamCodecs.java` (a subset) keeps
    `NeoForgeStreamCodecs.registryKey()` in the payloads.
  - vendored `net/neoforged/neoforge/common/util/NeoForgeExtraCodecs.java` (generated, verbatim) keeps its
    codec helpers in use.
  - Fabric glue helpers live outside the mirror tree, e.g.
    `fuzs.multiloaderdataextensions.fabric.impl.network.FriendlyByteBufHelper` and
    `fuzs.multiloaderdataextensions.fabric.impl.registries.datamaps.DataMapSyncHelper`.
- A patch for a `generated` file **must not** contain the package rename; the pipeline does that.

## Common operations

### Add a new vendored file

1. Add a `generated <source> <full/path>` line to `manifest` (or `owned`/`local`).
2. Optionally author a patch (see above).
3. Run `./gradlew :Fabric:syncVendoredSources :Fabric:checkVendoredSources`.

### Stop vendoring a file

Remove its `manifest` line and delete `patches/<full/path>.patch`, then delete the generated source file. Run
sync/check to confirm.

### Add a new upstream source

1. Add a resolvable configuration and a `SourceSpec` in `Fabric/build.gradle.kts`.
2. Reference it by `name` from manifest entries, and store its version in the version catalog.
3. Run `syncVendoredSources`; the lock will gain a `version.<source>` line.

### Update the upstream version

1. Bump `neoforge.version` in the shared version catalog (this is the version the `SourceSpec.version` in
   `Fabric/build.gradle.kts` reads).
2. `./gradlew :Fabric:syncVendoredSources`
3. Read the output:
   - If a patch no longer applies, the task aborts and prints the raw `patch` output; resolve the conflict
     (see below).
   - `[changed]` owned files -> review `build/vendored-sources/owned-upstream/<path>` and port upstream changes
     into the hand-maintained file.
4. `./gradlew :Fabric:build`

### Resolve a failed patch

`syncVendoredSources` fails with the `patch` output. The usual cause is upstream context drift. Then:

1. Compare the upstream file (`build/vendored-sources/upstream/neoforge/<path>`) with your patch to see what
   changed.
2. Re-author the patch against the new upstream (steps above), preserving the intent of the original edits.
3. Re-run sync.

### "Generated file still references net.neoforged.neoforge."

The relocation step found an upstream-only reference that the patch did not remove or replace. Open the patch,
find the remaining reference (a loader-only class such as `NeoForge`, `DataMapsUpdatedEvent`, `BaseMappedRegistry`,
`IPayloadContext`, ...), and either delete it or replace it with a Fabric equivalent. Re-run sync.

### "generated source is out of date"

The committed source differs from a fresh sync. Run `./gradlew :Fabric:syncVendoredSources` and commit the
result. Never edit `generated` files by hand.

### "owned source changed upstream"

An `owned` file's upstream counterpart changed. Review
`Fabric/build/vendored-sources/owned-upstream/<full/path>` and port the change into the hand-maintained file,
then run `syncVendoredSources` to refresh the lock.

## Rules of thumb

- `patches/` is the source of truth; the files under
  `Fabric/src/main/java/fuzs/multiloaderdataextensions/fabric/impl/neoforge/**` are generated. Never edit them
  by hand — edit the patch and run `syncVendoredSources`.
- Always commit the regenerated sources, `manifest`, `lock` and any patch changes together.
- The vendored tree is excluded from Spotless and marked generated in `.gitattributes`; do not reformat it.
- Keep the `multiloaderdataextensions$` method prefix on injected interface methods — it avoids mixin method
  collisions with other Fabric mods.
