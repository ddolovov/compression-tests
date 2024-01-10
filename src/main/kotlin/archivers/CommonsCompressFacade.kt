package ddol.compression.tests.archivers

import ddol.compression.tests.SpecificCompressionLevel
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZMethod
import org.apache.commons.compress.archivers.sevenz.SevenZMethodConfiguration
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import org.apache.commons.compress.compressors.CompressorInputStream
import org.apache.commons.compress.compressors.CompressorOutputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream
import org.tukaani.xz.LZMA2Options
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import org.apache.commons.compress.archivers.examples.Archiver as CommonsCompressArchiver
import org.apache.commons.compress.archivers.examples.Expander as CommonsCompressExpander

object CommonsCompressFacade {
    fun xzCompress(compressionLevel: SpecificCompressionLevel, payloadFile: File, archiveFile: File) {
        compress(payloadFile, archiveFile) { fos -> XZCompressorOutputStream(fos, compressionLevel.value.toInt()) }
    }

    fun xzUncompress(archiveFile: File, payloadFile: File) {
        uncompress(archiveFile, payloadFile, ::XZCompressorInputStream)
    }

    fun zstdCompress(compressionLevel: SpecificCompressionLevel, payloadFile: File, archiveFile: File) {
        compress(payloadFile, archiveFile) { fos -> ZstdCompressorOutputStream(fos, compressionLevel.value.toInt()) }
    }

    fun zstdUncompress(archiveFile: File, payloadFile: File) {
        uncompress(archiveFile, payloadFile, ::ZstdCompressorInputStream)
    }

    fun sevenZCompress(compressionLevel: SpecificCompressionLevel, payload: File, archiveFile: File) {
        val archive = SevenZOutputFile(archiveFile)
        archive.setContentMethods(
            listOf(
                SevenZMethodConfiguration(
                    SevenZMethod.LZMA2,
                    LZMA2Options(compressionLevel.value.toInt())
                )
            )
        )

        CommonsCompressArchiver().create(archive, payload)
    }

    fun sevenZUncompress(archiveFile: File, payload: File) {
        CommonsCompressExpander().expand(SevenZFile(archiveFile), payload)
    }

    private fun compress(payloadFile: File, archiveFile: File, compressorOutputStream: (FileOutputStream) -> CompressorOutputStream) {
        FileOutputStream(archiveFile).use { fos ->
            compressorOutputStream(fos).use { cos ->
                payloadFile.inputStream().use { fis -> fis.copyTo(cos) }
            }
        }
    }

    private fun uncompress(archiveFile: File, payloadFile: File, compressorInputStream: (FileInputStream) -> CompressorInputStream) {
        FileInputStream(archiveFile).use { fis ->
            compressorInputStream(fis).use { cis ->
                payloadFile.outputStream().use { fos -> cis.copyTo(fos) }
            }
        }
    }
}