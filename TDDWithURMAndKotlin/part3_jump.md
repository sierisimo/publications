---
title: TDD with URM + Kotlin: Jump 
published: false
description: 
tags: 
---

Here we are, once again, trying to build a URM interpreter.

This time we are going to take the hard part, the `jump` function. It should be easy right? Let's remember what the jump function does:

> "It takes two positions as parameters and an instruction number. If the value of registers at positions are equal, then it moves execution to the instruction number"

#### What?!?

Relax, it's easier than you think. Let's see an example:

You have the the registry:

```none
X: [,,4]
```

And the set of instructions:

```none
1: Z(2)
2: J(2, 3, 10)
3: I(2)
4: J(2, 2, 2)
```

This set of instructions will only clone a number in a registry into another registry. In this case the registry at position 2 will end having the same value of registry 3. Don't believe me? No problem! Let's do a dry run seeing what happens in memory:

```none
1: Z(2)        -> X: [,0,4]
2: J(2, 3, 10) -> X: [,0,4] // 0 != 4 so we continue execution at line 3
3: I(2)        -> X: [,1,4]
4: J(2, 2, 2)  -> X: [,1,4] // 1 == 1 comparing to the same position is always true, so we move back to line 2

2: J(2, 3, 10) -> X: [,1,4] // 1 != 4 so we continue execution at line 3
3: I(2)        -> X: [,2,4]
4: J(2, 2, 2)  -> X: [,2,4] // 2 == 2 comparing to the same position is always true, so we move back to line 2

2: J(2, 3, 10) -> X: [,2,4] // 2 != 4 so we continue execution at line 3
3: I(2)        -> X: [,3,4]
4: J(2, 2, 2)  -> X: [,3,4] // 3 == 3 comparing to the same position is always true, so we move back to line 2

2: J(2, 3, 10) -> X: [,3,4] // 3 != 4 so we continue execution at line 3
3: I(2)        -> X: [,4,4]
4: J(2, 2, 2)  -> X: [,4,4] // 4 == 4 comparing to the same position is always true, so we move back to line 2

2: J(2, 3, 10) -> X: [,3,4] // 4 == 4 so let's move to instruction 10… wait… there's no instruction 10, so we finish the execution
```

As you can see the `J` a.k.a `jump` function is a simple `go-to` in our machine. It moves the execution from one point to another. We need to create a way to represent this on our interpreter.

## But first… tests

To begin with our `jump` function we need a test for it. We know the function will take a registry to fetch the values to compare and an integer to move the execution to that line. for now, we will pass the set of instructions to the function to allow the `jump` function to move the _current_ index instruction.
We can take advantage of our other functions already created to setup some registers and then check the function works as expected:

```kotlin
@Test
fun `jump function should move to instruction X when registries are equal`() {
    zero(registry, 5)
    zero(registry, 10)

    jump(registry, 5, 10, instructionSet, 1)

    assertEquals(1, instructionSet.current)
}
```

Wow, that's actually an ugly function. It takes 5 parameters, probably Uncle Bob is looking at us and feeling disappointed… well, we will refactor it later. For now let's see if it works… well, actually we know it doesn't work, as [we have seen previously](https://dev.to/sierisimo/tdd-with-urm-and-kotlin-1dj7), if we run `gradle check` to run the tests, it will generate a _compile-time error_ because we haven't created the `jump` function yet.

Let's fix that:

```kotlin
fun jump(registry: Registry, positionX: Int, positionY: Int, instructionSet: ?, instruction: Int) {

}
```

Before moving on, we need to define how our `instructionSet` will behave or what type of data it will be. Things to keep in mind:

- It should hold as many instructions as need it
- Needs to know which is the current instruction and which one is the next one based on the index
- Needs to easily change the pointer of execution to any other instruction

We could need more functionality from the `instructionSet` but having these 3 requirements tells us we need a class and that will let us at least write the minimum necessary to fulfill our failing test. First we need to change the test adding a type for `instructionSet`:

```kotlin
internal class OperationsTest {
    //…

    val instructionSet = InstructionSet()
}
```

And we can use it in the test, but wait, the test requires the `instructionSet` to have a `current` property. Let's create the class and add the property to remove the _compile-time error_:

```kotlin
class InstructionSet {
    var current: Int = 0
}
```

We use `var` because we will be setting the value to a different number on every execution of the instruction set. This class just holds data and actually doesn't need to do anything else for our current purpose. Our test should pass now, right? No compile time errors and it's quite straightforward.

Well, not exactly. If we run the classic: `gradle check` we will get:

```none
expected: <1> but was: <0>
org.opentest4j.AssertionFailedError: expected: <1> but was: <0>
...
```

Hmmm we forgot to add an implementation to the function! Duh! Well… time to write the actual code:

```kotlin
fun jump(registry: Registry, positionX: Int, positionY: Int, instructionSet: InstructionSet, instruction: Int) {
    instructionSet.current = instruction
}
```

Easy peasy, now the test is passing! You can check it yourself. Please [go to this commit](https://github.com/sierisimo/publications/commit/929e3bb3dfff455fed5dccb51a2f716e5e43ebeb), which contains all the code for this part and check it. It'll be worth it.

At this point you are probably thinking I'm crazy, but I'm not. This is a fair thing happening on TDD: you created a test, then you created code to pass the test, let's move on. No one said the implementation is right but the test is passing. We need to test deeper:

```kotlin
@Test
fun `jump function won't move to instruction X when registries are different`() {
    //Setup
    instructionSet.current = 3

    zero(registry, 5)
    zero(registry, 10)
    increment(registry, 10)

    jump(registry, 5, 10, instructionSet, 10)

    //Assertions
    assertNotEquals(10, instructionSet.current)
    //If the jump function did not matched it should go in the next instruction
    assertEquals(4, instructionSet.current)
}
```

Now if we `check` the project we will get a failing test! This means our other test was right in the way we wrote it but the implementation of the function is actually wrong. Let's fix that.
We need to validate that values at registers are equal, only then we will update the position on the `instructionSet`. Otherwise we should just continue execution on the next instruction. Adding the minimum to pass the test our test will pass again:

```kotlin
fun jump(registry: Registry, positionX: Int, positionY: Int, instructionSet: InstructionSet, instruction: Int) {
    val xValue = registry.getValueAtPosition(positionX)
    val yValue = registry.getValueAtPosition(positionY)

    if (xValue == yValue) {
        instructionSet.current = instruction
    } else {
        instructionSet.current++
    }
}
```

## Conclusion

We finally figured out a way to do the `jump` function! We add the final tests and fixes to match the other operators of our URM (not included here, so you can do it as homework or cheat taking a look at the final code):

- `jump` function cannot operate over negative positions (should throw an `IllegalArgumentException`)
- `jump` function cannot work over empty registers (should throw an `IllegalStateException`)

This was quite easy and we can now move to the next part of our URM implementation.

We have some pending things that we will achieve later:

- The `jump` function has 5 parameters. That's ugly
- All the functions take a `Registry` as first parameter. We can fix that
- This is not the final implementation, we will do a lot of refactors as it is part of the cycle of building with TDD
- `InstructionSet` doesn't have operations and we should add tests if we add them
- We are still using a stub `Registry`. We need to fix this later to make the tests more trustworthy
- Tests are living code as well as our project, be open to fix and refactor our tests
- Some tests fail from time to time because the data is in a wrong state, our `Registry` needs to reset on every test…
