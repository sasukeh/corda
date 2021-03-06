package net.corda.demobench.explorer

import net.corda.core.createDirectories
import net.corda.core.div
import net.corda.core.list
import net.corda.core.utilities.loggerFor
import net.corda.demobench.model.JVMConfig
import net.corda.demobench.model.NodeConfig
import net.corda.demobench.model.forceDirectory
import net.corda.demobench.readErrorLines
import tornadofx.*
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.Executors

class Explorer internal constructor(private val explorerController: ExplorerController) : AutoCloseable {
    private companion object {
        val log = loggerFor<Explorer>()
    }

    private val executor = Executors.newSingleThreadExecutor()
    private var process: Process? = null

    @Throws(IOException::class)
    fun open(config: NodeConfig, onExit: (NodeConfig) -> Unit) {
        val explorerDir = config.explorerDir.toFile()

        if (!explorerDir.forceDirectory()) {
            log.warn("Failed to create working directory '{}'", explorerDir.absolutePath)
            onExit(config)
            return
        }

        try {
            installApps(config)

            val user = config.users.elementAt(0)
            val p = explorerController.process(
                    "--host=localhost",
                    "--port=${config.rpcPort}",
                    "--username=${user.username}",
                    "--password=${user.password}")
                    .directory(explorerDir)
                    .start()
            process = p

            log.info("Launched Node Explorer for '{}'", config.legalName)

            // Close these streams because no-one is using them.
            safeClose(p.outputStream)
            safeClose(p.inputStream)

            executor.submit {
                val exitValue = p.waitFor()
                val errors = p.readErrorLines()
                process = null

                if (errors.isEmpty()) {
                    log.info("Node Explorer for '{}' has exited (value={})", config.legalName, exitValue)
                } else {
                    log.error("Node Explorer for '{}' has exited (value={}, {})", config.legalName, exitValue, errors)
                }

                onExit(config)
            }
        } catch (e: IOException) {
            log.error("Failed to launch Node Explorer for '{}': {}", config.legalName, e.message)
            onExit(config)
            throw e
        }
    }

    private fun installApps(config: NodeConfig) {
        // Make sure that the explorer has cordapps on its class path. This is only necessary because currently apps
        // require the original class files to deserialise states: Kryo serialisation doesn't let us write generic
        // tools that work with serialised data structures. But the AMQP serialisation revamp will fix this by
        // integrating the class carpenter, so, we can eventually get rid of this function.
        //
        // Note: does not copy dependencies because we should soon be making all apps fat jars and dependencies implicit.
        //
        // TODO: Remove this code when serialisation has been upgraded.
        val pluginsDir = config.explorerDir / "plugins"
        pluginsDir.createDirectories()
        config.pluginDir.list {
            it.forEachOrdered { path ->
                val destPath = pluginsDir / path.fileName.toString()
                try {
                    // Try making a symlink to make things faster and use less disk space.
                    Files.createSymbolicLink(destPath, path)
                } catch(e: UnsupportedOperationException) {
                    // OS doesn't support symbolic links?
                    Files.copy(path, destPath)
                } catch(e: FileAlreadyExistsException) {
                    // OK, don't care ...
                }
            }
        }
    }

    override fun close() {
        executor.shutdown()
        process?.destroy()
    }

    private fun safeClose(c: AutoCloseable) {
        try {
            c.close()
        } catch (e: Exception) {
            log.error("Failed to close stream: '{}'", e.message)
        }
    }

}

class ExplorerController : Controller() {
    private val jvm by inject<JVMConfig>()
    private val explorerPath = jvm.applicationDir.resolve("explorer").resolve("node-explorer.jar")

    init {
        log.info("Explorer JAR: $explorerPath")
    }

    internal fun process(vararg args: String) = jvm.processFor(explorerPath, *args)

    fun explorer() = Explorer(this)
}