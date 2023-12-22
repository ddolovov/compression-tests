@file:Suppress("MemberVisibilityCanBePrivate", "MayBeConstant")

package ddol.compression.tests

import java.io.File

object Environment {
    val workDir: File = getSystemDir("user.dir").ensureIsDirectory { "Work directory does not exist: $it" }
    val outputDir: File = workDir.resolve("output").createAsEmptyDir()

    val reportFile: File = outputDir.resolve("report.csv")
    val rounds: Int = 10

    val payloads: List<Payload> = workDir.listFiles()
        ?.filter { it.isDirectory && it.name.startsWith("payload-") }
        ?.sorted()
        ?.map { Payload(it.relativeTo(workDir).path, it) }
        .orEmpty()

    init {
        check(payloads.isNotEmpty()) { "No payloads" }
    }

    fun printDiagnostics() {
        println("Payloads = ${payloads.size}")
        payloads.forEachIndexed { index, payload ->
            println("    ${index + 1}. '${payload.name}' (${payload.size})")
        }
        println("Output dir = '${outputDir.relativeTo(workDir)}'")
    }
}

@Suppress("SameParameterValue")
private fun getSystemDir(name: String): File = System.getProperty(name)?.let(::File) ?: error("Can't obtain '$name'")
