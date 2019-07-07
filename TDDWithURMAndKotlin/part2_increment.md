# TDD With URM and Kotlin - increment

In the previous article we mentioned a way to start writing our implementation of URM using TDD. We also mentioned a lot of questions that came up while writing the `zero` function.

Now it's time to write the increment function.

## Increment function

We know the `increment` function will be `I(x)`, where `x` represents a register in our _Unlimented Register Machine_. We are writing (for now) only the internal that will be used to represent our functionallity.

Following the approach we followed for the `zero` function, we start by writing a test on how we expect the function behaves with negative numbers:

```kotlin
@ParameterizedTest
@ValueSource(ints = [-2, -8, -20, -100])
fun `increment function throws exception with negative position`(position: Int) {
    assertThrows<IllegalArgumentException> { increment(registry, position) }

    assertNull(registry.getValueAtPosition(position))
}
```

This initial test is in pro of the defensive model, we first need to be sure the function fails for some invalid cases. We could start with a different part of the code but this is the easiest to write because we already wrote this on the `zero` function:

```kotlin
fun increment(registry: Registry, position: Int){
    require(position > 0) { "Position must be positive number" }
}
```

For our second test we need want to validate a "strict" rule: when a register has no value, it cannot be incremented. Our test should represent this and we should put a simple exception for it:

```kotlin
@ParameterizedTest
@ValueSource(ints = [2, 4, 20, 40])
fun `increment function cannot work with empty register`(position: Int) {
    assertThrows<IllegalStateException> { increment(registry, position) }

    assertNull(registry.getValueAtPosition(position))
}
```

If we run our test at this point it will fail:

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

The test for the real implementation will need a litle more setup. We know that the current approach will throw an expection if our test is just:

```kotlin
@ParameterizedTest
@ValueSource(ints = [11, 15, 110, 200, Int.MAX_VALUE])
fun `increment function adds 1 to value of position`(position: Int) {
    increment(registry, position)

    assertEquals(1, registry.getValueAtPosition(position))
}
```

This is because we wrote a test that will throw an exception when a registry does not contain a value and someone calls the `increment` function on it. But fortunately we already wrote tests for the `zero` function which means we can initialize our registry before calling increment:

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

But testing with a single `1` is boring… Let's write something that operates over different values. To test dinamycally we will need to check a parameter, then call `increment` and get an expected value. We can simply call `assertEquals(parameter + 1, registry.getValueAtPosition(parameter))` but somehow it feels like cheating…

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

This allow the test to be more dynamic and we can add more cases having more flexibility!

Let's write one test mixing what we learned:

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

Wow! That's a test. We start by setting manually a value on a position. Then we increment it to assert the increment works as expected (we know it does because we have a test to validate it!) and then we do something "new": we reset to 0 a register. We are validating something we didn't check before, that the `zero` function can reset any registry regardless of the value. Finally we `increment` and validate that `increment` works regardless of the value.

---

## Conclusion

We added a new function and also we validate our previous work is not affecting the new work we added. Everything was achieved through writing tests and fixing code!

There's still things that makes the tests feel like cheating… calling manually `registry.setValueAtPosition` and having a custom implementation for example. We will fix this in the next article where we will talk about mocks!

Thanks for reading :)
