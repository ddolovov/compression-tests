package ddol.compression.tests

enum class CompressionLevel(val rawLevel: UInt) {
    L1(1u),
    L2(2u),
    L3(3u),
    L4(4u),
    L5(5u),
    L6(6u),
    L7(7u),
    L8(8u),
    L9(9u);

    val isMeasured: Boolean get() = rawLevel % 2u != 0u

    override fun toString() = rawLevel.toString()
}
