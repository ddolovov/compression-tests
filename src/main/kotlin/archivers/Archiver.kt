@file:Suppress("MemberVisibilityCanBePrivate")

package ddol.compression.tests.archivers

import ddol.compression.tests.*
import ddol.compression.tests.cli.Tool
import java.io.File
import kotlin.reflect.KClass

sealed class Archiver(val name: String, requiredTools: List<Tool>) {
    constructor(name: String, vararg requiredTools: Tool) : this(name, requiredTools.asList())

    sealed class State(val tools: Map<Tool, File>) {
        class Available(tools: Map<Tool, File>) : State(tools)
        class Unavailable(tools: Map<Tool, File>, val missingTools: List<Tool>) : State(tools)

        fun getTool(tool: Tool): File = tools[tool] ?: error("Tool not found: ${tool.alias}")
    }

    val state: State by lazy { computeStatus(requiredTools) }

    open fun computeSpecificCompressionLevel(genericCompressionLevel: GenericCompressionLevel): SpecificCompressionLevel =
        SpecificCompressionLevel.Default(genericCompressionLevel)

    abstract fun compress(compressionLevel: SpecificCompressionLevel, uncompressed: Payload, targetDirectory: File): Payload
    abstract fun uncompress(compressed: Payload, targetDirectory: File): Payload

    protected fun Payload.toCompressedPayload(tool: Tool, targetDirectory: File): Payload {
        val compressedName = "$name.${tool.fileExtension}"
        val compressedLocation = targetDirectory.resolve(compressedName)
        return Payload(compressedName, compressedLocation)
    }

    protected fun Payload.toUncompressedPayload(tool: Tool, targetDirectory: File): Payload {
        val uncompressedName = name.removeSuffix(".${tool.fileExtension}")
        val uncompressedLocation = targetDirectory.resolve(uncompressedName)
        return Payload(uncompressedName, uncompressedLocation)
    }

    companion object {
        val all: List<Archiver> =
            buildSet { Archiver::class.sealedSubclasses.forEach { it.extractArchiverSubclasses(this) } }
                .map { it.objectInstance ?: error("Can not get object instance for Archiver implementation: $it") }
                .sortedBy { it.name }
                .also { check(it.isNotEmpty()) { "No registered Archiver implementations" } }

        fun printDiagnostics() {
            println("Archivers = ${all.size}")
            all.forEachIndexed { index, archiver ->
                val prefix = when (val state = archiver.state) {
                    is State.Unavailable -> ", MISSING TOOLS: ${state.missingTools.joinToString { it.alias }}"
                    is State.Available -> ""
                }
                println("    ${index + 1}. '${archiver.name}'$prefix")
            }
        }

        private fun KClass<out Archiver>.extractArchiverSubclasses(output: MutableSet<KClass<out Archiver>>) {
            if (isSealed)
                sealedSubclasses.forEach { it.extractArchiverSubclasses(output) }
            else
                output += this
        }

        private fun computeStatus(requiredTools: List<Tool>) : State {
            val tools = mutableMapOf<Tool, File>()
            val missingTools = mutableListOf<Tool>()

            LinkedHashSet(requiredTools).forEach { tool ->
                when (val location = tool.locate()) {
                    null -> missingTools += tool
                    else -> tools[tool] = location
                }
            }

            return if (missingTools.isEmpty()) State.Available(tools) else State.Unavailable(tools, missingTools)
        }
    }
}
