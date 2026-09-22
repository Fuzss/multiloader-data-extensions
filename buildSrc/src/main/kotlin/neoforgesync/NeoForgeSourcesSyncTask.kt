package neoforgesync

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class SyncNeoForgeSourcesTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val manifestFile: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val patchesDir: DirectoryProperty

    @get:Input
    abstract val packagePrefix: Property<String>

    @get:Input
    abstract val neoforgeVersion: Property<String>

    @get:Internal
    abstract val committedDir: DirectoryProperty

    @get:Internal
    abstract val workDir: DirectoryProperty

    @get:Internal
    abstract val lockFile: RegularFileProperty

    @get:Internal
    abstract val upstreamSources: ConfigurableFileCollection

    @get:Inject
    abstract val execOps: ExecOperations

    @TaskAction
    fun sync() {
        val previous = NeoForgeLock.ownedHashes(NeoForgeLock.read(lockFile.get().asFile))
        val reportDir = File(workDir.get().asFile, "owned-upstream")
        val result = generator().generate(
            manifestFile.get().asFile,
            patchesDir.get().asFile,
            committedDir.get().asFile,
            workDir.get().asFile,
            sourcesJar(),
            packagePrefix.get(),
            reportDir = reportDir,
        )

        val changed = result.ownedHashes.filter { (path, hash) -> previous[path] != null && previous[path] != hash }
        if (changed.isNotEmpty()) {
            logger.warn("Upstream sources changed for 'owned' files; review and port manually:")
            changed.keys.sorted().forEach { path -> logger.warn("  - $path (see ${File(reportDir, path)})") }
        }

        NeoForgeLock.write(lockFile.get().asFile, neoforgeVersion.get(), result.ownedHashes)
        logger.lifecycle("NeoForge sources synced from version ${neoforgeVersion.get()}.")
    }

    private fun generator(): NeoForgeSourcesGenerator = NeoForgeSourcesGenerator { dir, command ->
        execOps.exec {
            commandLine(command)
            workingDir = dir
            isIgnoreExitValue = true
        }.exitValue
    }

    private fun sourcesJar(): File {
        val jars = upstreamSources.files
        if (jars.size != 1) {
            throw GradleException("Expected exactly one NeoForge sources artifact but found: $jars")
        }
        return jars.single()
    }
}

abstract class CheckNeoForgeSourcesTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val manifestFile: RegularFileProperty

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val patchesDir: DirectoryProperty

    @get:Input
    abstract val packagePrefix: Property<String>

    @get:Input
    abstract val neoforgeVersion: Property<String>

    @get:Internal
    abstract val committedDir: DirectoryProperty

    @get:Internal
    abstract val workDir: DirectoryProperty

    @get:Internal
    abstract val lockFile: RegularFileProperty

    @get:Internal
    abstract val upstreamSources: ConfigurableFileCollection

    @get:Inject
    abstract val execOps: ExecOperations

    @TaskAction
    fun check() {
        val work = workDir.get().asFile
        work.deleteRecursively()
        val outputDir = File(work, "output")
        val reportDir = File(work, "owned-upstream")

        val result = NeoForgeSourcesGenerator { dir, command ->
            execOps.exec {
                commandLine(command)
                workingDir = dir
                isIgnoreExitValue = true
            }.exitValue
        }.generate(
            manifestFile.get().asFile,
            patchesDir.get().asFile,
            outputDir,
            File(work, "work"),
            upstreamSources.files.single(),
            packagePrefix.get(),
            reportDir,
        )

        val problems = mutableListOf<String>()

        for (entry in NeoForgeSourcesSync.parseManifest(manifestFile.get().asFile)) {
            if (entry.mode != SyncMode.GENERATED) continue
            val produced = File(outputDir, entry.path)
            val committed = File(committedDir.get().asFile, entry.path)
            if (!committed.isFile || produced.readText() != committed.readText()) {
                problems += "generated source is out of date: ${entry.path}"
            }
        }

        val lock = NeoForgeLock.read(lockFile.get().asFile)
        if (lock.isEmpty()) {
            problems += "lock file '${lockFile.get().asFile}' is missing"
        } else {
            val lockedVersion = NeoForgeLock.version(lock)
            if (lockedVersion != neoforgeVersion.get()) {
                problems += "lock file targets NeoForge $lockedVersion but ${neoforgeVersion.get()} is configured"
            }
            NeoForgeLock.ownedHashes(lock).forEach { (path, hash) ->
                val current = result.ownedHashes[path]
                if (current != hash) {
                    problems += "owned source changed upstream: $path (review ${File(reportDir, path)})"
                }
            }
            result.ownedHashes.forEach { (path, _) ->
                if (!lock.containsKey(path)) problems += "owned source missing from lock file: $path"
            }
        }

        if (problems.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("NeoForge sources are out of sync:")
                    problems.forEach { appendLine("  - $it") }
                    appendLine()
                    appendLine("Run './gradlew :Fabric:syncNeoForgeSources' and review the result.")
                }
            )
        }
        logger.lifecycle("NeoForge sources are in sync with version ${neoforgeVersion.get()}.")
    }
}
