package ddol.compression.tests.archivers

import ddol.compression.tests.SpecificCompressionLevel
import ddol.compression.tests.GenericCompressionLevel
import ddol.compression.tests.Payload
import ddol.compression.tests.cli.CliCommandRunner
import ddol.compression.tests.cli.Tool.SEVEN_ZIP
import java.io.File

@Suppress("unused")
data object SevenZ : Archiver("7z", SEVEN_ZIP) {
    override fun computeSpecificCompressionLevel(genericCompressionLevel: GenericCompressionLevel) =
        SpecificCompressionLevel.Default(genericCompressionLevel, "-mx")

    override fun compress(compressionLevel: SpecificCompressionLevel, uncompressed: Payload, targetDirectory: File): Payload {
        val sevenZipTool = state.getTool(SEVEN_ZIP)
        val compressed = uncompressed.toCompressedPayload(SEVEN_ZIP, targetDirectory)

        CliCommandRunner(workDir = uncompressed.path.parentFile, timeoutSeconds = 10)
            .runCommand(sevenZipTool, "a", compressionLevel.cliKey, compressed.path, uncompressed.name)
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