package ddol.compression.tests.archivers

import ddol.compression.tests.CompressionLevel
import ddol.compression.tests.Payload
import ddol.compression.tests.cli.CliCommandRunner
import ddol.compression.tests.cli.Tool.SEVEN_ZIP
import java.io.File

@Suppress("unused")
data object SevenZip : Archiver("7z", SEVEN_ZIP) {
    override fun compress(compressionLevel: CompressionLevel, uncompressed: Payload, targetDirectory: File): Payload {
        val sevenZipTool = state.getTool(SEVEN_ZIP)
        val compressed = uncompressed.toCompressedPayload(SEVEN_ZIP, targetDirectory)

        CliCommandRunner(workDir = uncompressed.path.parentFile, timeoutSeconds = 10)
            .runCommand(sevenZipTool, "a", "-mx${compressionLevel.rawLevel}", compressed.path, uncompressed.name)
            .ensureSuccess()

        return compressed
    }

    override fun uncompress(compressed: Payload, targetDirectory: File): Payload {
        val sevenZipTool = state.getTool(SEVEN_ZIP)
        val uncompressed = compressed.toUncompressedPayload(SEVEN_ZIP, targetDirectory)

        CliCommandRunner(workDir = targetDirectory, timeoutSeconds = 10)
            .runCommand(sevenZipTool, "x", compressed.path)
            .ensureSuccess()

        return uncompressed
    }
}