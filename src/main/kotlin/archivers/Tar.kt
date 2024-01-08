package ddol.compression.tests.archivers

import ddol.compression.tests.SpecificCompressionLevel
import ddol.compression.tests.Payload
import ddol.compression.tests.cli.CliCommandRunner
import ddol.compression.tests.cli.Tool
import java.io.File

sealed class Tar(name: String, vararg requiredTools: Tool): Archiver(name, listOf(Tool.TAR) + requiredTools) {
    override fun compress(compressionLevel: SpecificCompressionLevel, uncompressed: Payload, targetDirectory: File): Payload {
        val tarTool = state.getTool(Tool.TAR)
        val merged = uncompressed.toCompressedPayload(Tool.TAR, targetDirectory)

        CliCommandRunner(workDir = uncompressed.path.parentFile, timeoutSeconds = 10)
            .runCommand(tarTool, "-cf", merged.path, uncompressed.name)
            .ensureSuccess()

        return merged
    }

    override fun uncompress(compressed: Payload, targetDirectory: File): Payload {
        val tarTool = state.getTool(Tool.TAR)
        val unmerged = compressed.toUncompressedPayload(Tool.TAR, targetDirectory)

        CliCommandRunner(workDir = compressed.path.parentFile, timeoutSeconds = 10)
            .runCommand(tarTool, "-xf", compressed.name)
            .ensureSuccess()

        return unmerged
    }
}
