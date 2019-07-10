package net.sierisimo.kurm.operations

import net.sierisimo.kurm.Registry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource

internal class OperationsTest {
    val registry = object : Registry {
        val positionValueMap = mutableMapOf<Int, Int>()

        override fun getValueAtPosition(position: Int): Int? = positionValueMap[position]

        override fun setValueAtPosition(position: Int, value: Int) {
            positionValueMap[position] = value
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 5, 10, 1000, Int.MAX_VALUE])
    fun `zero function sets value to 0 at register in position`(position: Int) {
        zero(registry, position)

        val expected = 0
        val actual = registry.getValueAtPosition(position)

        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, -5, -10, -1000, Int.MIN_VALUE])
    fun `zero function throws exception with negative position`(position: Int) {
        assertThrows<IllegalArgumentException> { zero(registry, position) }

        assertNull(registry.getValueAtPosition(position))
    }

    @ParameterizedTest
    @ValueSource(ints = [-2, -8, -20, -100])
    fun `increment function throws exception with negative position`(position: Int) {
        assertThrows<IllegalArgumentException> { increment(registry, position) }

        assertNull(registry.getValueAtPosition(position))
    }

    @ParameterizedTest
    @ValueSource(ints = [2, 4, 20, 40])
    fun `increment function cannot work with empty register`(position: Int) {
        assertThrows<IllegalStateException> { increment(registry, position) }

        assertNull(registry.getValueAtPosition(position))
    }

    @ParameterizedTest
    @ValueSource(ints = [11, 15, 110, 200, Int.MAX_VALUE])
    fun `increment function adds 1 to value of position`(position: Int) {
        zero(registry, position)
        increment(registry, position)

        assertEquals(1, registry.getValueAtPosition(position))
    }

    @ParameterizedTest
    @CsvSource("50,1,2", "50,5,6", "50,100,101")
    fun `increment function adds 1 to any integer value`(
        position: Int,
        original: Int,
        expected: Int
    ) {
        registry.setValueAtPosition(position, original)

        increment(registry, position)

        assertEquals(expected, registry.getValueAtPosition(position))
    }

    @ParameterizedTest
    @CsvSource("10,23,24", "15,99,100", "1000,1000,1001")
    fun `check zero and increment can work together`(
        position: Int,
        original: Int,
        expected: Int
    ) {
        registry.setValueAtPosition(position, original)
        increment(registry, position)
        assertEquals(expected, registry.getValueAtPosition(position))

        zero(registry, position)
        assertEquals(0, registry.getValueAtPosition(position))

        increment(registry, position)
        assertEquals(1, registry.getValueAtPosition(position))
    }
}