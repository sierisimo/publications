# Unit testing with JUnit

I already explained that [writing tests is not that hard](https://dev.to/sierisimo/testing-is-not-that-hard-54e7) 
but I got some comments about it… I can summarize those comments in just one:

> Where are the code examples?

So this time I will focus more in the code part with some simple examples.

Note: I won't follow TDD, BDD or any specific methodology thingy for this article. I want to explain how to think tests and how to write them as a first approach. Also worth to mention once more: I'll be providing most of the code in Kotlin for JVM, the concepts still apply for other tech.

## Unit Tests

When you write code, you want to see it working, you want to see magic that formed in your mind working in the computer, you want to corroborate that everything is ready for the next steps in you NITRO-TURBO-SUPER-MEGA-AWESOME-PROJECT. But running the whole thing every single time, doing a compilation of the whole dependent code, passing previous screens/steps to arrive at the part you want to test, or simply adding one thousand _logs_ as part of the process to check everything works is annoying and takes a lot of time. Also, you need to do this for every big change (or even smaller changes) to validate this new thing doesn't break other stuff.

Writing Unit tests can save you from this. A unit test is a single test that validates the smallest part of a system. It generally doesn't require too much setup and it is easy to read. Performing a unit test involves checking that one single functionality works as expected for both good and bad cases.

Let's begin with a simple (and classic) example: **FizzBuzz**.

The rules for FizzBuzz are quite simple:

> A group of children are counting numbers, they go in turns and say something depending on the number they got:
>
> 1. When number is multiple of 3 they should say "Fizz" instead of the number
>
> 2. When number is multiple of 5 they should say "Buzz" instead of the number
>
> 3. When number is both multiple of 3 and 5 they should say "FizzBuzz" instead of the number
>
> 4. Otherwise they just say the number

These rules seem easy and fun, so how does it go for our code? Well, we decided to go for a simple function:

```kotlin
fun fizzBuzz(countUntil: Int): List<String>
```

This function will work correctly for every single element of the FizzBuzz game and will give us back the results. To keep the focus in the tests I removed the body of this function.

### Our setup

Imagine that the previous function is located at filesystem in: `project/src/main/kotlin/net/sierisimo/games/FizzBuzz.kt`

Then our tests will be at: `project/src/test/kotlin/net/sierisio/games/FizzBuzzTest.kt`

This follows the standard of Java and is what Kotlin suggests in the official docs.

We are going to use JUnit 5 for our tests, so you should check the [official docs](https://junit.org/junit5/docs/current/user-guide/) if something's missing on this article. The only thing you need to know by now is that JUnit is a tool for running test frameworks on the JVM. It will help us run our tests and do some checks.

Let's write our tests cases!

## Valid cases

First of all we need to focus on valid cases to check the function works as expected (quotes added to represent they are Strings):

1. When we pass the number 1 we should get back: `["1"]`
2. When we pass the number 3 we should get back: `["1","2","Fizz"]`
3. When we pass the number 5 we should get back: `["1","2","Fizz","4","Buzz"]`
4. When we pass the number 60 we should get back: `["1", "2", "Fizz", "4", "Buzz", "Fizz", "7", "8", "Fizz", "Buzz", "11", "Fizz", "13", "14", "FizzBuzz", "16", "17", "Fizz", "19", "Buzz", "Fizz", "22", "23", "Fizz", "Buzz", "26", "Fizz", "28", "29", "FizzBuzz", "31", "32", "Fizz", "34", "Buzz", "Fizz", "37", "38", "Fizz", "Buzz", "41", "Fizz", "43", "44", "FizzBuzz", "46", "47", "Fizz", "49", "Buzz", "Fizz", "52", "53", "Fizz", "Buzz", "56", "Fizz", "58", "59", "FizzBuzz"]`

This list can go on and on. For now we are going to stay with these four cases. 

The first thing is to create a class that will hold our tests and set some methods on it to represent our valid cases:

```kotlin
import org.junit.jupiter.api.Test

class FizzBuzzTest {
    @Test
    fun whenWePass1WeGotASingleItemList() {
        //…
    }

    @Test
    fun whenWePass3TheLastItemIsFizz(){
        //…
    }

    @Test
    fun whenWePass5TheLastItemIsBuzz(){
        //…
    }

    @Test
    fun whenWePass60TheLastElementIsFizzBuzz(){
        //…
    }
}
```

Things you can notice in this class:

- The annotation `@Test` before each method. This tells JUnit which methods are test cases and should run individually.
- Names are longer and descriptive

There's also the chance in Kotlin along with JUnit 5 to use the **backtick** notation for method names:

```kotlin
internal class FizzBuzzTest {
    @Test
    fun `when 1 is passed a single item list is returned`() {
        //…
    }
}
```

Or if the names in this notation aren't what you like, you can also just use an annotation to put a fancy name on the test report:

```kotlin
@DisplayName("when 3 is passed the last item is 'Fizz'")
@Test
fun whenWePass3TheLastItemIsFizz(){
    //…
}
```

The style depends on you and there are even ways to make the displaying more fancy 
or complex.

Once we have the tests, let's say what parts are involved in each one.

I generally think on a test like 3 parts:

```kotlin
@Test
fun myTestCase(){
  // Setup of data
  // Call to my SUT (System Under Test)
  // Assertions/Verifications
}
```

Our first test then goes:

```kotlin
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
```

If you run this on IntelliJ/Android Studio or with Gradle/Maven you will notice if your function is working.

The assertions like `assertEquals` and `assertTrue` are included with JUnit 5 but you can search the internet for other assertion libraries that include a more versatile and variant set of assertion (like [AssertJ](http://joel-costigliola.github.io/assertj/))

Here's a simple example of how the class can end with the tests:

```kotlin
package net.sierisimo.games

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

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
}
```

## Invalid Cases

Testing for the happy path is great and shows that the function works correctly, but we know users don't work like that. They don't follow the happy path, they always try to break our stuff and they send weird stuff to our code. 

The best thing we can do is to write tests for invalid cases too. The first ones that come to mind are:

1. Zero is not a valid parameter and the function should return an empty list: `[]`
2. Negative numbers are not valid parameters and function should return empty list: `[]`

> Optional for other languages: Checking the data type is correct is also worth testing. In the case of Kotlin there's no need because this is checked at compile time.

These two cases are quite easy and we can even put them in a single test (and introduce new annotations):

```kotlin
@ParameterizedTest
@ValueSource(ints = [0, -1, -50, -1000, -123459, Int.MIN_VALUE])
fun whenLimitIsLessOrEqualThanZeroReturnsEmptyList(limit: Int) {
    val actual = fizzBuzz(limit)

    assertTrue(actual.isEmpty())
}
```

`@ParameterizedTest` allows the test to be executed multiple times passing the values found in `@ValueSource` individually to the test method. The test method then needs to take one parameter.

One big advantage of this kind of test is that we are now able to test all the invalid test cases we want with a single method and if a new case that behaves in the same way is needed we can just add it to the `@ValueSource` list.

---

## Conclusion

This is just a simple introduction on how to write unit tests for a single function, things could get more complex when we need to pass more complex data or we need to test other kind of elements, but we can still see that _testing is not that hard_ and we can easily validate the code we work with. If you are interested in the final test class it's available [here, along with the markdown of this article](https://gist.github.com/sierisimo/a6db59570f606e487f6602b1eba329b4).

Once more: Thanks for reading!
