package ddol.compression.tests.archivers

import ddol.compression.tests.Payload
import ddol.compression.tests.SpecificCompressionLevel
import ddol.compression.tests.cli.Tool.UNZIP
import ddol.compression.tests.cli.Tool.ZIP
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Suppress("unused")
data object ZipJava : Archiver("zip(java)") {
    override fun compress(compressionLevel: SpecificCompressionLevel, uncompressed: Payload, targetDirectory: File): Payload {
        val compressed = uncompressed.toCompressedPayload(ZIP, targetDirectory)

        FileOutputStream(compressed.path).use { fos ->
            ZipOutputStream(fos).use { zos ->
                zos.setLevel(compressionLevel.value.toInt())

                uncompressed.path.walkTopDown().forEach { file ->
                    val relativePath = file.relativeTo(uncompressed.path.parentFile)

                    when {
                        file.isFile -> {
                            val entry = ZipEntry(relativePath.path)
                            zos.putNextEntry(entry)
                            file.inputStream().use { fis -> fis.copyTo(zos) }
                            zos.closeEntry()
                        }
                        file.isDirectory -> {
                            val entry = ZipEntry(relativePath.path + "/")
                            zos.putNextEntry(entry)
                            zos.closeEntry()
                        }
                        else -> error("Unsupported file type: $file")
                    }
                }
            }
        }

        return compressed
    }

    override fun uncompress(compressed: Payload, targetDirectory: File): Payload {
        val uncompressed = compressed.toUncompressedPayload(UNZIP, targetDirectory)

        FileInputStream(compressed.path).use { fis ->
            ZipInputStream(fis).use { zis ->
                generateSequence { zis.nextEntry }.forEach { entry ->
                    val file = File(targetDirectory, entry.name)

                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        file.outputStream().use { fos -> zis.copyTo(fos) }
                    }
                }
            }
        }

        return uncompressed
    }
}
