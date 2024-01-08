package ddol.compression.tests.archivers

import ddol.compression.tests.SpecificCompressionLevel
import ddol.compression.tests.GenericCompressionLevel
import ddol.compression.tests.Payload
import ddol.compression.tests.cli.CliCommandRunner
import ddol.compression.tests.cli.Tool.UNZSTD
import ddol.compression.tests.cli.Tool.ZSTD
import java.io.File

@Suppress("unused")
data object TarZstd : Tar("tar + zstd", ZSTD, UNZSTD) {
    override fun computeSpecificCompressionLevel(genericCompressionLevel: GenericCompressionLevel) =
        object : SpecificCompressionLevel() {
            override val genericCompressionLevel get() = genericCompressionLevel
            override val cliKey
                get() = "-" + when (genericCompressionLevel) {
                    GenericCompressionLevel.LOW -> 1
                    GenericCompressionLevel.LOW_TO_MEDIUM -> 6
                    GenericCompressionLevel.MEDIUM -> 11
                    GenericCompressionLevel.MEDIUM_TO_HIGH -> 16
                    GenericCompressionLevel.HIGH -> 22
                }
        }

    override fun compress(compressionLevel: SpecificCompressionLevel, uncompressed: Payload, targetDirectory: File): Payload {
        @Suppress("NAME_SHADOWING") val uncompressed = super.compress(compressionLevel, uncompressed, targetDirectory)
        val zstdTool = state.getTool(ZSTD)

        CliCommandRunner(workDir = uncompressed.path.parentFile, timeoutSeconds = 10)
            .runCommand(zstdTool, "-k", "--ultra", compressionLevel.cliKey, "-T1", uncompressed.path)
            .ensureSuccess()

        return uncompressed.toCompressedPayload(ZSTD, targetDirectory)
    }

    override fun uncompress(compressed: Payload, targetDirectory: File): Payload {
        val unzstdTool = state.getTool(UNZSTD)
        val uncompressed = compressed.toUncompressedPayload(UNZSTD, targetDirectory)

        CliCommandRunner(workDir = targetDirectory, timeoutSeconds = 10)
            .runCommand(unzstdTool, "-k", compressed.path)
            .ensureSuccess()

        return super.uncompress(uncompressed, targetDirectory)
    }
}