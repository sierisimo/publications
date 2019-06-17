package net.sierisimo.kurm.operations

import net.sierisimo.kurm.Registry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
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
}