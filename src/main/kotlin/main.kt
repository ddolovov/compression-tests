package ddol.compression.tests

import ddol.compression.tests.archivers.Archiver
import java.io.File
import kotlin.enums.enumEntries
import kotlin.time.measureTimedValue

fun main() {
    Environment.printDiagnostics()
    Archiver.printDiagnostics()

    val report = Report()

    Environment.payloads.forEach { originalPayload ->
        Archiver.all.forEach archiver@{ archiver ->
            if (archiver.state is Archiver.State.Unavailable) return@archiver

            @OptIn(ExperimentalStdlibApi::class)
            enumEntries<GenericCompressionLevel>()
                .dropWhile { it < GenericCompressionLevel.MEDIUM }
                .forEach { genericCompressionLevel ->
                    for (round in 1..Environment.rounds) {
                        println()
                        println("=== ${archiver.name}, compression $genericCompressionLevel, payload '${originalPayload.path.name}', round $round of ${Environment.rounds} ===")

                        val specificCompressionLevel = archiver.computeSpecificCompressionLevel(genericCompressionLevel)
                        val compressedPayloadDir = prepareCompressedPayloadDir()
                        val (compressedPayload, compressionDuration) = measureTimedValue {
                            archiver.compress(specificCompressionLevel, originalPayload, compressedPayloadDir)
                        }
                        val compressionRatio = CompressionRatio(originalPayload.size, compressedPayload.size)

                        println("    Compression:")
                        println("        Ratio:        ${compressionRatio.ratioAsText}")
                        println("        Space saving: ${compressionRatio.spaceSavingAsText}")
                        println("        Duration:     $compressionDuration")

                        val uncompressedPayloadDir = prepareUncompressedPayloadDir()
                        val compressedPayloadInUncompressedPayloadDir = compressedPayload.copyTo(uncompressedPayloadDir)
                        val (uncompressedPayload, uncompressionDuration) = measureTimedValue {
                            archiver.uncompress(compressedPayloadInUncompressedPayloadDir, uncompressedPayloadDir)
                        }

                        check(uncompressedPayload.name == originalPayload.name)
                        check(uncompressedPayload.size == originalPayload.size)

                        println("    Uncompression:")
                        println("        Duration:     $uncompressionDuration")

                        report.report(
                            archiver = archiver,
                            originalPayload = originalPayload,
                            specificCompressionLevel = specificCompressionLevel,
                            compressionRatio = compressionRatio,
                            compressionDuration = compressionDuration,
                            uncompressionDuration = uncompressionDuration
                        )
                    }
                }
        }
    }

    report.saveToFile(Environment.reportFile)
    println()
    println("Report saved to ${Environment.reportFile}")
}

private fun prepareCompressedPayloadDir(): File =
    Environment.outputDir.resolve("compressed-payload").createAsEmptyDir()

private fun prepareUncompressedPayloadDir(): File =
    Environment.outputDir.resolve("uncompressed-payload").createAsEmptyDir()
