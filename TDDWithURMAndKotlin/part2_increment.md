---
title: TDD with URM + Kotlin: Increment
published: true
description: A continuation on the approach to generate a URM interpreter using Kotlin and TDD
tags: kotlin, tdd
cover_image:
series: TDD With URM and Kotlin
---

[In the previous article](https://dev.to/sierisimo/tdd-with-urm-and-kotlin-1dj7) we mentioned a way to start writing our implementation of URM using TDD. We also mentioned a lot of questions that came up while writing the `zero` function.

Now it's time to write the increment function.

## Increment function

We know that the `increment` function will be `I(x)`, where `x` represents a register in our _Unlimited Register Machine_. We are only writing only the internal that will be used to represent our functionality –for now.

Following the same approach we used for the `zero` function, we'll start by writing a test on how we expect the function to behave with negative numbers:

```kotlin
@ParameterizedTest
@ValueSource(ints = [-2, -8, -20, -100])
fun `increment function throws exception with negative position`(position: Int) {
    assertThrows<IllegalArgumentException> { increment(registry, position) }

    assertNull(registry.getValueAtPosition(position))
}
```

This initial test is in pro of the defensive model, but remember that we first need to make sure that the function fails for some invalid cases. We could start with a different part of the code but this is the easiest to write because we already wrote this on the `zero` function:

```kotlin
fun increment(registry: Registry, position: Int){
    require(position > 0) { "Position must be positive number" }
}
```

For our second test, we need to validate a "strict" rule: when a register has no value, it cannot be incremented. Our test should represent this and we should place a simple exception for it:

```kotlin
@ParameterizedTest
@ValueSource(ints = [2, 4, 20, 40])
fun `increment function cannot work with empty register`(position: Int) {
    assertThrows<IllegalStateException> { increment(registry, position) }

    assertNull(registry.getValueAtPosition(position))
}
```

If we run our test at this point, it will fail:

```
Expected java.lang.IllegalStateException to be thrown, but nothing was thrown.
org.opentest4j.AssertionFailedError: Expected java.lang.IllegalStateException to be thrown, but nothing was thrown.
```

This exception tells us what we already know: our function is not throwing the right exception for empty registers. Let's fix that by using the contract `checkNotNull` which is available on the `kotlin stdlib` (I'll write about contracts in another article):

```kotlin
fun increment(registry: Registry, position: Int){
    require(position > 0) { "Position must be positive number" }
    checkNotNull(registry.getValueAtPosition(position)) { "Register must be initialized" }
}
```

Now our tests are passing again. Let's write the actual implementation.

## The real implementation

The test for the real implementation will need a little more setup. We know that the current approach will throw an exception if our test is just:

```kotlin
@ParameterizedTest
@ValueSource(ints = [11, 15, 110, 200, Int.MAX_VALUE])
fun `increment function adds 1 to value of position`(position: Int) {
    increment(registry, position)

    assertEquals(1, registry.getValueAtPosition(position))
}
```

This is because we wrote a test that will throw an exception when a registry does not contain a value and someone calls the `increment` function on it. But fortunately we already wrote tests for the `zero` function which means we can initialize our registry before calling `increment`:

```kotlin
@ParameterizedTest
@ValueSource(ints = [11, 15, 110, 200, Int.MAX_VALUE])
fun `increment function adds 1 to value of position`(position: Int) {
    zero(registry, position)
    increment(registry, position)

    assertEquals(1, registry.getValueAtPosition(position))
}
```

To fix the breaking test we need to modify our function:

```kotlin
fun increment(registry: Registry, position: Int) {
    require(position > 0) { "Position must be positive number" }

    val currentValue = registry.getValueAtPosition(position)
    checkNotNull(currentValue) { "Register must be initialized" }

    registry.setValueAtPosition(position, currentValue + 1)
}
```

We store the value of `registry.getValueAtPosition(position)` and then call the contract `checkNotNull` (I'll write about contracts soon, I promise!)

But testing with a single `1` is boring… Let's write something that operates over different values. To test dynamically we would need to check a parameter, then call `increment` and get an expected value. We could simply call `assertEquals(parameter + 1, registry.getValueAtPosition(parameter))` but this somehow feels like cheating…

Using _parameterized tests_ from JUnit 5 we can create a single test that will cover different cases in a single method, and we will use `@CsvSource` to pass different parameters. `@CsvSource` takes a group of comma-separated-strings for every set of parameters and our method will ask for this parameters in the appropiate types:

```kotlin
@ParameterizedTest
@CsvSource("50,1,2", "50,5,6", "50,100,101")
fun `increment function adds 1 to any integer value`(
    position: Int,
    original: Int,
    expected: Int
){
    registry.setValueAtPosition(position, original)

    increment(registry, position)

    assertEquals(expected, registry.getValueAtPosition(position))
}
```

This allows the test to be more dynamic and lets us add more cases having more flexibility!

Let's write one test mixing what we have learned:

```kotlin
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
```

Wow! That's a test. We start by manually setting a value on a position. Then we increment it to assert that the increment works as expected -we know it does because we have a test to validate it!- and then we do something "new": we reset a register to 0. Here we are validating something we didn't check before: that the `zero` function can reset any registry regardless of the value. Finally, we `increment` and validate that `increment` works regardless of the value.

---

## Conclusion

We added a new function and also validated that our previous work is not affecting the new code we just added. Everything was achieved through writing tests and fixing code!

There are still things that make the tests feel like cheating… calling manually `registry.setValueAtPosition` and having a custom implementation for example. We will fix this in the next article where we will talk about mocks!

Thanks for reading :)
