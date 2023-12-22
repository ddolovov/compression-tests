package ddol.compression.tests

import ddol.compression.tests.archivers.Archiver
import ddol.compression.tests.archivers.Zip
import java.io.File
import kotlin.time.measureTimedValue

fun main() {
    Environment.printDiagnostics()
    Archiver.printDiagnostics()

    val report = Report()

    for (round in 1 .. Environment.rounds) {
        println()
        println("=== Round $round of ${Environment.rounds} ===")
        Archiver.all.forEach archiver@{ archiver ->
            if (archiver.state is Archiver.State.Unavailable) return@archiver

            Environment.payloads.forEach payload@{ originalPayload ->
                CompressionLevel.entries.forEach compressionLevel@{ compressionLevel ->
                    if (!compressionLevel.isMeasured) return@compressionLevel

                    println()
                    println("${archiver.name}, compression $compressionLevel, payload '${originalPayload.path.name}'")

                    val compressedPayloadDir = prepareCompressedPayloadDir()
                    val (compressedPayload, compressionDuration) = measureTimedValue {
                        archiver.compress(compressionLevel, originalPayload, compressedPayloadDir)
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
                        compressionLevel = compressionLevel,
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
