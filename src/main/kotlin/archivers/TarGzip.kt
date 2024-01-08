package ddol.compression.tests.archivers

import ddol.compression.tests.*
import ddol.compression.tests.cli.CliCommandRunner
import ddol.compression.tests.cli.Tool.*
import java.io.File

@Suppress("unused")
data object TarGzip : Tar("tar + gzip", GZIP, GUNZIP) {
    override fun computeSpecificCompressionLevel(genericCompressionLevel: GenericCompressionLevel) =
        SpecificCompressionLevel.Default(genericCompressionLevel)

    override fun compress(compressionLevel: SpecificCompressionLevel, uncompressed: Payload, targetDirectory: File): Payload {
        @Suppress("NAME_SHADOWING") val uncompressed = super.compress(compressionLevel, uncompressed, targetDirectory)
        val gzipTool = state.getTool(GZIP)

        CliCommandRunner(workDir = uncompressed.path.parentFile, timeoutSeconds = 10)
            .runCommand(gzipTool, "-k", compressionLevel.cliKey, uncompressed.path)
            .ensureSuccess()

        return uncompressed.toCompressedPayload(GZIP, targetDirectory)
    }

    override fun uncompress(compressed: Payload, targetDirectory: File): Payload {
        val gunzipTool = state.getTool(GUNZIP)
        val uncompressed = compressed.toUncompressedPayload(GUNZIP, targetDirectory)

        CliCommandRunner(workDir = targetDirectory, timeoutSeconds = 10)
            .runCommand(gunzipTool, "-k", compressed.path)
            .ensureSuccess()

        return super.uncompress(uncompressed, targetDirectory)
    }
}
