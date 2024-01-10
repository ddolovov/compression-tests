package ddol.compression.tests.archivers

import ddol.compression.tests.GenericCompressionLevel
import ddol.compression.tests.Payload
import ddol.compression.tests.SpecificCompressionLevel
import ddol.compression.tests.cli.Tool.SEVEN_ZIP
import java.io.File

@Suppress("unused")
data object SevenZCommonsCompress : Archiver("7z(commons-compress)") {
    override fun computeSpecificCompressionLevel(genericCompressionLevel: GenericCompressionLevel) =
        SevenZ.computeSpecificCompressionLevel(genericCompressionLevel)

    override fun compress(
        compressionLevel: SpecificCompressionLevel,
        uncompressed: Payload,
        targetDirectory: File
    ): Payload {
        val compressed = uncompressed.toCompressedPayload(SEVEN_ZIP, targetDirectory)
        CommonsCompressFacade.sevenZCompress(
            compressionLevel = compressionLevel,
            payload = uncompressed.path,
            archiveFile = compressed.path
        )
        return compressed
    }

    override fun uncompress(compressed: Payload, targetDirectory: File): Payload {
        val uncompressed = compressed.toUncompressedPayload(SEVEN_ZIP, targetDirectory)
        CommonsCompressFacade.sevenZUncompress(
            archiveFile = compressed.path,
            payload = uncompressed.path
        )
        return uncompressed
    }
}