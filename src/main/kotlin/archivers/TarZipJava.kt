package ddol.compression.tests.archivers

import ddol.compression.tests.*
import ddol.compression.tests.cli.Tool.*
import java.io.File

@Suppress("unused")
data object TarZipJava : Tar("tar + zip(java)", ZIP, UNZIP) {
    override fun compress(compressionLevel: SpecificCompressionLevel, uncompressed: Payload, targetDirectory: File) =
        ZipJava.compress(
            compressionLevel,
            super.compress(compressionLevel, uncompressed, targetDirectory),
            targetDirectory
        )

    override fun uncompress(compressed: Payload, targetDirectory: File) =
        super.uncompress(ZipJava.uncompress(compressed, targetDirectory), targetDirectory)
}
