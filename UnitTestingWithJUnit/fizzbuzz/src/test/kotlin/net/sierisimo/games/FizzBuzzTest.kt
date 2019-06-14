package net.sierisimo.games

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class FizzBuzzTest {
    @DisplayName("when 1 is passed a single item list is returned")
    @Test
    fun when1IsPassedASingleItemListIsReturned() {
        //Setup of data:
        val limit = 1
        //Call
        val actual = fizzBuzz(limit)
        //Assertions/Verification
        assertTrue(actual.isNotEmpty())

        assertEquals(1, actual.size)
        assertEquals("1", actual.first())
    }

    @DisplayName("when 3 is passed the last item is 'Fizz'")
    @Test
    fun whenWePass3TheLastItemIsFizz() {
        val limit = 3

        val expected = "Fizz"
        val actual = fizzBuzz(limit)

        assertTrue(actual.isNotEmpty())
        assertEquals(expected, actual.last())
    }

    @DisplayName("when 5 is passed the last item is 'Buzz'")
    @Test
    fun whenWePass5TheLastItemIsBuzz() {
        val limit = 5

        val expected = "Buzz"
        val actual = fizzBuzz(limit)

        assertTrue(actual.isNotEmpty())
        assertEquals(expected, actual.last())
    }

    @Test
    fun whenWePass60TheLastElementIsFizzBuzz() {
        val limit = 60

        val expected = "FizzBuzz"
        val actual = fizzBuzz(limit)

        assertTrue(actual.isNotEmpty())
        assertEquals(expected, actual.last())
    }

    @ParameterizedTest
    @ValueSource(ints = [0, -1, -50, -1000, -123459, Int.MIN_VALUE])
    fun whenLimitIsLessOrEqualThanZeroReturnsEmptyList(limit: Int) {
        val actual = fizzBuzz(limit)

        assertTrue(actual.isEmpty())
    }
}