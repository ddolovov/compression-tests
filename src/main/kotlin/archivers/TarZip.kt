package ddol.compression.tests.archivers

import ddol.compression.tests.*
import ddol.compression.tests.cli.Tool.*
import java.io.File

@Suppress("unused")
data object TarZip : Tar("tar + zip", ZIP, UNZIP) {
    override fun computeSpecificCompressionLevel(genericCompressionLevel: GenericCompressionLevel) =
        SpecificCompressionLevel.Default(genericCompressionLevel)

    override fun compress(compressionLevel: SpecificCompressionLevel, uncompressed: Payload, targetDirectory: File) =
        Zip.compress(
            compressionLevel,
            super.compress(compressionLevel, uncompressed, targetDirectory),
            targetDirectory
        )

    override fun uncompress(compressed: Payload, targetDirectory: File) =
        super.uncompress(Zip.uncompress(compressed, targetDirectory), targetDirectory)
}
