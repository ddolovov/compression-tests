package ddol.compression.tests.archivers

import ddol.compression.tests.*
import ddol.compression.tests.cli.CliCommandRunner
import ddol.compression.tests.cli.Tool.*
import java.io.File

@Suppress("unused")
data object TarXz : Tar("tar + xz", XZ, UNXZ) {
    override fun compress(compressionLevel: SpecificCompressionLevel, uncompressed: Payload, targetDirectory: File): Payload {
        @Suppress("NAME_SHADOWING") val uncompressed = super.compress(compressionLevel, uncompressed, targetDirectory)
        val xzTool = state.getTool(XZ)

        CliCommandRunner(workDir = uncompressed.path.parentFile, timeoutSeconds = 10)
            .runCommand(xzTool, "-k", compressionLevel.cliKey, uncompressed.path)
            .ensureSuccess()

        return uncompressed.toCompressedPayload(XZ, targetDirectory)
    }

    override fun uncompress(compressed: Payload, targetDirectory: File): Payload {
        val unxzTool = state.getTool(UNXZ)
        val uncompressed = compressed.toUncompressedPayload(UNXZ, targetDirectory)

        CliCommandRunner(workDir = targetDirectory, timeoutSeconds = 10)
            .runCommand(unxzTool, "-k", compressed.path)
            .ensureSuccess()

        return super.uncompress(uncompressed, targetDirectory)
    }
}
