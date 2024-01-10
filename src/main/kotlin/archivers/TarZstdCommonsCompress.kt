package ddol.compression.tests.archivers

import ddol.compression.tests.GenericCompressionLevel
import ddol.compression.tests.Payload
import ddol.compression.tests.SpecificCompressionLevel
import ddol.compression.tests.cli.Tool.*
import java.io.File

@Suppress("unused")
data object TarZstdCommonsCompress : Tar("tar + zstd(commons-compress)") {
    override fun computeSpecificCompressionLevel(genericCompressionLevel: GenericCompressionLevel) =
        TarZstd.computeSpecificCompressionLevel(genericCompressionLevel)

    override fun compress(compressionLevel: SpecificCompressionLevel, uncompressed: Payload, targetDirectory: File): Payload {
        val tarPayload = super.compress(compressionLevel, uncompressed, targetDirectory)
        val compressed = tarPayload.toCompressedPayload(ZSTD, targetDirectory)

        CommonsCompressFacade.zstdCompress(compressionLevel, payloadFile = tarPayload.path, archiveFile = compressed.path)

        return compressed
    }

    override fun uncompress(compressed: Payload, targetDirectory: File): Payload {
        val tarPayload = compressed.toUncompressedPayload(UNZSTD, targetDirectory)

        CommonsCompressFacade.zstdUncompress(archiveFile = compressed.path, payloadFile = tarPayload.path)

        return super.uncompress(tarPayload, targetDirectory)
    }
}
