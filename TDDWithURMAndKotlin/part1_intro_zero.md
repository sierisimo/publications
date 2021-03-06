# TDD with URM + Kotlin: Increment

In one of my previous articles [I wrote about URM](https://dev.to/sierisimo/my-favorite-computer-theory-to-create-examples-15cn) and how it works. In another one [I mentioned test cases](https://dev.to/sierisimo/unit-testing-with-junit-3mcd) and how to keep ideas for a simple function. Now it's time to wrap up with TDD.

First of all…

## What's TDD?

**TDD** is the acronym for _Testing Driven Development_. It works as a technique for building software based on first writing the tests for the given software.

It's worth to mention this technique does not solve all your problems but allows you to identify problems before they get too messy. Also, it's a different way of writing code, which means it probably won't make total sense at first sight or won't stick too much with you. I think that once you get used to the process it will be easy to just go with it.

Some friends have complained about delivery times and the issue of TDD taking longer than just throwing a lot of code, the biggest argument about this being that if something breaks in the future and you don't have the tests for it, you would end up taking the same time fixing that as if you would have taken the time to do TDD since the begining. Still I think is worth the effort to learn why it is so popular and try it at least once -where once is a period of time bigger than 1 day.

TDD has only three rules to follow:

1. You are not allowed to write any production code unless it is to make a failing unit test pass
2. You are not allowed to write any more of a unit test than it is sufficient to fail; compilation failures are still failures
3. You are not allowed to write any more production code than it is sufficient to pass the one failing unit test

With these three rules you can follow TDD kind of easily. The whole process involves more than just following these rules, as it involves changing mindsets. For the rules to work, you need to think as 3 different roles:

1. External developer
2. QA/Tester/Someone with partial knowledge of the design of the software
3. The actual developer

This means you'll be in a loop following the rules along with changing the current mindset. The whole process you would get use to, is here:

1. How would I like to use this code from outside the code? Would I like to have a way to pass a callback or would I get a value from the call? Which dependencies are needed before the actual call to the code?
2. Let's write code for validating that the response will work and that the value is returned/sent as it should be
3. Let's write the actual code
4. Refactor some names
5. Repeat from step 1 if something needs changing

The whole process for getting into TDD works differently for each person, and on a daily basis when you're facing tight deadlines it's also difficult to get used to it. Still, I insist: it's worth a try.

## Project and setup

The accompanying project is a simple implementation of a working URM. The goal is to write a project capable of executing files in the URM notation:

```
X: []

1: Z(2)
2: Z(5)
3: I(2)
4: J(2,5,7)
```

As mentioned before, I'll be using **_Kotlin_** in the JVM flavor along with **_JUnit 5_**. I will also be using **_Gradle_** as the task runner and build tool. The final project can be found in [this Github project](https://github.com/sierisimo/publications/tree/master/TDDWithURMAndKotlin/kurm)

## Zero function

The first thing we are going to build is the operations for the URM machine to work, this is the easiest part because we don't have to worry about the internals of the _registers_ or the parsing process.

The `Z(x)` function will be our first feature to write. The function will take a number that represent a register position and will put a zero on it. We will build this function to be invoked in our code. It should be easy, right? Well… I have some questions:

- Should the function be part of each register like: `register.zero()`?
  - I dislike this idea, the registers should only know how to store a value, nothing else
  - This would involve creating an object to represent each register, we don't need that complexity
- Should the function be part of a bigger object like: `URMinstance.zero()`?
  - This is a little improvement on where the function will live
  - This approach has a problem, makes the function work only with the local group of registers (a.k.a registry) and having a bit knowledge of the implementation
  - We depend on the existence of `URMinstance` to test the function (not as bad as I want to make it sound)
- Which parameters should it actually take?
  - The function should take the position of the register and should know about the group of registers in order to modify it
  - The function should be agnostic of the way the registry is implemented but still know about how to set a value on it

With these questions and answers we decide on a simple way of calling our function:

```kotlin
zero(registry, position)
```

This way the function is not tied to a certain registry, it's not dependant on the inner implementation of the URM, and we can test it without involving too many things.

### Our initial test

Our first test (in pseudocode) will be something close to:

```
position = 5

zero(registry, position)

checkEquals(0, registry.getPositionValue(position))
```

This is pretty close to our first kotlin test:

```kotlin
import org.junit.jupiter.api.Test

internal class OperationsTest {
    @DisplayName("Zero Function sets to 0 register at position")
    @Test
    fun zeroFunctionSetsTo0TheRegisterAtPosition() {
        val position = 5
        //val registry: ?

        zero(registry, position)

        val expected = 0
        val actual = registry[position]

        assertEquals(expected, actual)
    }
}
```

Using gradle we should be able to run the test:

```
sierisimo@computer:$ gradle check
…
e: /Dev/sierisimo/TDD_URM_Kotlin/src/test/kotlin/net/sierisimo/kurm/operations/OperationsTest.kt: (14, 9): Unresolved reference: zero
e: /Dev/sierisimo/TDD_URM_Kotlin/src/test/kotlin/net/sierisimo/kurm/operations/OperationsTest.kt: (14, 14): Unresolved reference: registry
e: /Dev/sierisimo/TDD_URM_Kotlin/src/test/kotlin/net/sierisimo/kurm/operations/OperationsTest.kt: (17, 22): Unresolved reference: registry
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':compileTestKotlin'.
> Compilation error. See log for more details
* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.
* Get more help at https://help.gradle.org
BUILD FAILED in 9s
2 actionable tasks: 1 executed, 1 up-to-date
Compilation error. See log for more details
```

As we can see, we have a compilation error…

> DUH! THAT'S OBVIOUS BECAUSE WE DIDN'T CREATE THAT FUNCTION, IT IS A VERY SIMPLE FUNCTION YOU DUMB…

Hey! Wait a minute… before saying that you should remember the rules of TDD:

1. You are not allowed to write any production code unless it is to make a failing unit test pass
2. You are not allowed to write any more of a unit test than it is sufficient to fail; and **compilation failures are failures**
3. You are not allowed to write any more production code than it is sufficient to pass the one failing unit test

So that means I couldn't write the `zero` function before writing the test. But now we have a failing unit test, which means we are allowed to write production code!

Note: _Production code_ can be a term that people understand as code that will be inmediatly put on a server/app to answer to user interactions. This is not 100% true, production code is basically code that will be on the final project and satisfies part of the functionality of the project.

### Writing code and fixing

To write the code, let's hear what the errors have to say:

```
e: /Dev/sierisimo/TDD_URM_Kotlin/src/test/kotlin/net/sierisimo/kurm/operations/OperationsTest.kt: (14, 9): Unresolved reference: zero
e: /Dev/sierisimo/TDD_URM_Kotlin/src/test/kotlin/net/sierisimo/kurm/operations/OperationsTest.kt: (14, 14): Unresolved reference: registry
e: /Dev/sierisimo/TDD_URM_Kotlin/src/test/kotlin/net/sierisimo/kurm/operations/OperationsTest.kt: (17, 22): Unresolved reference: registry
```

This means we don't have a `zero` function! Let's create one in the package: `net.sierisimo.kurm.operations.Operations.kt`

```kotlin
fun zero(){

}
```

If we invoke `gradle check` again we get:

```
e: /Users/sierisimo/Documents/Dev/kurm/src/test/kotlin/net/sierisimo/kurm/operations/OperationsTest.kt: (15, 14): Too many arguments for public fun zero(): Unit defined in net.sierisimo.kurm.operations
```

Which means we forgot to add the arguments for the function. So our function will end looking like:

```kotlin
fun zero(registry: ?, position: Int){

}
```

I left something out in the function… what's the type of `registry`? Is it an array? I don't think so, our program is the "_**Unlimited** Register Machine_", and arrays have a limit size. Is it a list? Could be, as long as the list is initialized or has a way to grow to certain size to hold empty slots. The real question is: what functionality does the registry has? well, it should be able to retrieve the value of a register in a given position and set the value of a register in a given position. Then a list could work but still has the issue with empty slots.

We decide that, in this precise moment, we don't care about how the registry works, and because we don't care about how it works —as long as it works— we can create a definition for that:

```kotlin
interface Registry {
    fun getValueAtPosition(position: Int): Int?

    fun setValueAtPosition(position: Int, value: Int)
}
```

In that way, we can delegate later the responsability of the implementation. Now our first approach of the zero function goes like this:

```kotlin
fun zero(registry: Registry, position: Int) {

}
```

Since we don't care about the details of the registry itself, we can create an implementation on the test:

```kotlin
internal class OperationsTest {
    val registry = object : Registry {
        val positionValueMap = mutableMapOf<Int, Int>()

        override fun getValueAtPosition(position: Int): Int? = positionValueMap[position]

        override fun setValueAtPosition(position: Int, value: Int) {
            positionValueMap[position] = value
        }
    }

    @DisplayName("Zero Function sets to 0 register at position")
    @Test
    fun zeroFunctionSetsTo0TheRegisterAtPosition() {
        val position = 5

        zero(registry, position)

        val expected = 0
        val actual = registry.getValueAtPosition(position)

        assertEquals(expected, actual)
    }
}
```

This can be a final implementation, but writing this final implementation will violate the rules of TDD. We stay with this local implementation just for our test now.

If we run `gradle check` now it shows:

```
> Task :test

net.sierisimo.kurm.operations.OperationsTest > zero function should set a 0 in position X() FAILED
    org.opentest4j.AssertionFailedError at OperationsTest.kt:27

1 test completed, 1 failed

> Task :test FAILED

FAILURE: Build failed with an exception.
```

Which means the project successfully compiled but our test did not pass. And we know that we currently don't have a body in our function, let's fix that:

```kotlin
fun zero(registry: Registry, position: Int) {
    registry.setValueAtPosition(position, 0)
}
```

And finally `gradle check` will show:

```
> Task :test

net.sierisimo.kurm.operations.OperationsTest > zeroFunctionSetsTo0TheRegisterAtPosition() PASSED

BUILD SUCCESSFUL in 1s
```

### A bad case

Our test is passing. Now we should write another test to validate everything works as expected. For example, we know that URM works with positions in registers, but what happens to negative positions? They are not valid positions… let's write a test for it:

```kotlin
@Test
@DisplayName("Zero Function throws exception with negative position")
fun zeroFunctionThrowsExceptionWithNegativePosition() {
    val position = -1

    assertThrows<IllegalArgumentException> { zero(registry, position) }

    assertNull(registry.getValueAtPosition(position))
}
```

In this test we check that our function doesn't work with negative positions. But also we state in the test we expect the function to _throw_ an `IllegalArgumentException`. If we run the `gradle check` we will see this test is not passing, but the previous one is.

We fix the `zero` function adding Kotlin contracts:

```kotlin
fun zero(registry: Registry, position: Int) {
    require(position > 0) { "Position must be positive number" }

    registry.setValueAtPosition(position, 0)
}
```

Now if we run the check, both of our tests will be passing and we have made sure that even after we modified the way the function works internally, it works as expected for different scenarios.

Before moving into the next function, we should check for more cases, instead of having one method per case we go with parameterizing the test:

```kotlin
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
```

We also take advantage of **_Kotlin_** support for function names with spaces using backticks to remove the `@DisplayName` and put directly the name in the function. And we can test our function just adding the values we want into `@ValueSource(ints = [])` without having to write more test cases.

---

## Conclusion

For now, we have understood/learned the basics of following the _design-test-check-fix-repeat_ loop of TDD. All of this with a simple function.

There's still so much to do, like creating tests for the other functions (`increment`, `jump`), but we are going to take that for the next article.

Thanks for reading!
