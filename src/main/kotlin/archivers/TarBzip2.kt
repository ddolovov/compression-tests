package ddol.compression.tests.archivers

import ddol.compression.tests.*
import ddol.compression.tests.cli.CliCommandRunner
import ddol.compression.tests.cli.Tool.*
import java.io.File

@Suppress("unused")
data object TarBzip2 : Tar("tar + bzip2", BZIP2, BUNZIP2) {
    override fun compress(compressionLevel: SpecificCompressionLevel, uncompressed: Payload, targetDirectory: File): Payload {
        @Suppress("NAME_SHADOWING") val uncompressed = super.compress(compressionLevel, uncompressed, targetDirectory)
        val bzip2Tool = state.getTool(BZIP2)

        CliCommandRunner(workDir = uncompressed.path.parentFile, timeoutSeconds = 10)
            .runCommand(bzip2Tool, "-k", compressionLevel.cliKey, uncompressed.path)
            .ensureSuccess()

        return uncompressed.toCompressedPayload(BZIP2, targetDirectory)
    }

    override fun uncompress(compressed: Payload, targetDirectory: File): Payload {
        val bunzip2Tool = state.getTool(BUNZIP2)
        val uncompressed = compressed.toUncompressedPayload(BUNZIP2, targetDirectory)

        CliCommandRunner(workDir = targetDirectory, timeoutSeconds = 10)
            .runCommand(bunzip2Tool, "-k", compressed.path)
            .ensureSuccess()

        return super.uncompress(uncompressed, targetDirectory)
    }
}
