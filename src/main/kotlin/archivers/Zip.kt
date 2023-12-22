package ddol.compression.tests.archivers

import ddol.compression.tests.*
import ddol.compression.tests.cli.CliCommandRunner
import ddol.compression.tests.cli.Tool.UNZIP
import ddol.compression.tests.cli.Tool.ZIP
import java.io.File

@Suppress("unused")
data object Zip : Archiver("zip", ZIP, UNZIP) {
    override fun compress(compressionLevel: CompressionLevel, uncompressed: Payload, targetDirectory: File): Payload {
        val zipTool = state.getTool(ZIP)
        val compressed = uncompressed.toCompressedPayload(ZIP, targetDirectory)

        CliCommandRunner(workDir = uncompressed.path.parentFile, timeoutSeconds = 10)
            .runCommand(zipTool, "-r", compressed.path, "-${compressionLevel.rawLevel}", uncompressed.name)
            .ensureSuccess()

        return compressed
    }

    override fun uncompress(compressed: Payload, targetDirectory: File): Payload {
        val unzipTool = state.getTool(UNZIP)
        val uncompressed = compressed.toUncompressedPayload(UNZIP, targetDirectory)

        CliCommandRunner(workDir = targetDirectory, timeoutSeconds = 10)
            .runCommand(unzipTool, compressed.path)
            .ensureSuccess()

        return uncompressed
    }
}
