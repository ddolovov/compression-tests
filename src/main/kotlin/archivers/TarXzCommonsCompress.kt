package ddol.compression.tests.archivers

import ddol.compression.tests.Payload
import ddol.compression.tests.SpecificCompressionLevel
import ddol.compression.tests.cli.Tool.UNXZ
import ddol.compression.tests.cli.Tool.XZ
import java.io.File

@Suppress("unused")
data object TarXzCommonsCompress : Tar("tar + xz(commons-compress)") {
    override fun compress(compressionLevel: SpecificCompressionLevel, uncompressed: Payload, targetDirectory: File): Payload {
        val tarPayload = super.compress(compressionLevel, uncompressed, targetDirectory)
        val compressed = tarPayload.toCompressedPayload(XZ, targetDirectory)

        CommonsCompressFacade.xzCompress(compressionLevel, payloadFile = tarPayload.path, archiveFile = compressed.path)

        return compressed
    }

    override fun uncompress(compressed: Payload, targetDirectory: File): Payload {
        val tarPayload = compressed.toUncompressedPayload(UNXZ, targetDirectory)

        CommonsCompressFacade.xzUncompress(archiveFile = compressed.path, payloadFile = tarPayload.path)

        return super.uncompress(tarPayload, targetDirectory)
    }
}
