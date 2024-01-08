package ddol.compression.tests

enum class GenericCompressionLevel(val rawValue: UInt) {
    LOW(1u),
    LOW_TO_MEDIUM(2u),
    MEDIUM(3u),
    MEDIUM_TO_HIGH(4u),
    HIGH(5u);

    override fun toString() = "$name($rawValue)"
}

abstract class SpecificCompressionLevel : Comparable<SpecificCompressionLevel> {
    abstract val genericCompressionLevel: GenericCompressionLevel
    abstract val cliKey: String

    final override fun equals(other: Any?) =
        other is SpecificCompressionLevel && genericCompressionLevel == other.genericCompressionLevel && cliKey == other.cliKey

    final override fun hashCode() = genericCompressionLevel.hashCode() + 31 * cliKey.hashCode()

    final override fun toString() = cliKey

    final override fun compareTo(other: SpecificCompressionLevel) =
        genericCompressionLevel.compareTo(other.genericCompressionLevel)

    class Default(
        override val genericCompressionLevel: GenericCompressionLevel,
        private val prefix: String = "-"
    ) : SpecificCompressionLevel() {
        override val cliKey get() = "$prefix${genericCompressionLevel.rawValue * 2u - 1u}"
    }
}
