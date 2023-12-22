package ddol.compression.tests.cli

import java.io.File
import java.nio.file.Files
import java.util.concurrent.TimeUnit

class CliCommandRunner(
    private val workDir: File? = null,
    private val timeoutSeconds: Long? = 0
) {
    fun runCommand(command: Any, vararg arguments: Any): CliCommandResult {
        val stdoutFile = createTempFile(command, "stdout")
        val stderrFile = createTempFile(command, "stderr")

        val process = ProcessBuilder()
            .apply { workDir?.let { directory(it) } }
            .command(
                buildList {
                    this += command.toString()
                    arguments.mapTo(this, Any::toString)
                }
            )
            .redirectOutput(stdoutFile)
            .redirectError(stderrFile)
            .start()

        val exitCode = when (val timeoutSeconds = timeoutSeconds) {
            null -> process.waitFor()
            else -> {
                if (process.waitFor(timeoutSeconds, TimeUnit.SECONDS))
                    process.exitValue()
                else {
                    runSilently { process.destroyForcibly() }
                    null
                }
            }
        }

        val stdout = stdoutFile.readText()
        val stderr = stderrFile.readText()

        runSilently { stdoutFile.delete() }
        runSilently { stderrFile.delete() }

        return CliCommandResult(stdout, stderr, exitCode)
    }

    companion object {
        private fun createTempFile(command: Any, suffix: String): File {
            val prefix = command.toString().substringAfterLast('/').substringAfterLast('\\')
            return Files.createTempFile(prefix, suffix).toFile()
        }

        private inline fun runSilently(block: () -> Unit) {
            try {
                block()
            } catch (_: Exception) {
                // Do nothing.
            }
        }
    }
}

class CliCommandResult(val stdout: String, val stderr: String, val exitCode: Int?) {
    fun ensureSuccess(): CliCommandResult {
        check(exitCode == 0) { "Command has finished with non-zero exit code: $exitCode" }
        return this
    }
}
