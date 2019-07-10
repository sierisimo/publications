# My favorite computer theory to create examples

In university I learned a lot of different stuff but one of my favorite topics in the "_Computer Theory_" class was **URM** (Unlimited Register Machine).

## The theory

**URM** serves to demonstrate some concepts about computers and the _computability_ of some operations. I won't go very deep into details of the history or theory, but it's similar to the theory of [the Turin machine](https://en.wikipedia.org/wiki/Turing_machine), which states that given a set of ordered instructions (called algorithm) and an infinite tape, the machine is capable of run that algorithm and give a result for valid inputs.

## The registers

The registers in URM work much alike to an infinite sequential number of cells enumerated by position. You can only input numbers in the registers. The preconditions or information about this registers depends mostly on the author, but most agree that if the register hasn't been touched; it does not contain data. Other authors prefer to just go and say every register contains a zero before being touched by the machine.

## The operations

To obtain a result of a computable algorithm (or demonstrate it's not computable) **URM** provides us with 3 basic operations. Some books or authors mention a fourth operation, but the fourth operations I have seen on paper are achievable with just the 3 basics ones (names or letters may vary depending on the author):

- `Z(position)` Zero operation. Replaces whatever is in the register at `position` with a 0
- `I(position)` Increment operation. Adds 1 to whatever is on the register at `position`. If we are strict… if nothing is at the register it cannot add 1 to it (so you need to invoke `Z(position)` first).
- `J(positionX, positionY, instruction)` Jump operation. Compares the value of register at `positionX` and `positionY,` when the value is the same continues the execution at `instruction`. Is valid to send a `instruction` out of range, this will indicate the immediate end of the execution

## The Instructions

This also varies from author to author, but the idea is the same: You have an ordered and identified list of calls for operations on registers representing your algorithm. Once the list ends, URM will stop.

Using a generic notation (there is not a formal or standard one), the list will look like:

```
1:  Z(5)
2:  I(5)
3:  Z(2)
4:  J(2, 5, 7)
5:  I(5)
6:  I(2)
7:  J(2, 2, 6)
```

## The preconditions

It is also possible to provide preconditions to the machine to indicate a set of previously filled data, or how the algorithm works using variables (all lowercase):

```
X: [2,,,3]
```

Will indicate that the registers at position 0 and 3 started with a value. URM will proceed to read the instructions with this assumption and give a result.

or

```
X: [x,y]
```

Will indicate the existence of unknown numbers with names `x` and `y` . This will later execute with real values, the concept of variable does not exist in URM. There's also a chance to provide preconditions for the `x` and `y` like:

```
X: [x,y] -> x < y
```

This depends totally on the author and implementation. In most cases you state that on paper (or in some doc for your implementation).

## The result

There's no formal indication on where the result will be. I created a small set of instructions to copy the result to the register at position 0. In that way you can check for a specific result in that position. Copying to different positions can get complicated but you can simply state that every set of functions works on different URMs (multiple machines). Some say this is cheating, but it helps if you want to reuse ideas. Remember this works in theory.

## Example: Copy function

A set of instructions with the precondition of having a single number at register 1 and copying the value to the register 0

```
X: [,y]

1: J(0, 1, 6)
2: Z(0)
3: J(0, 1, 6)
4: I(0)
5: J(0, 0, 3)
```

You can then use this to invoke as a new operations (you will end up writing something similar but with different register position).

[I wrote a small implementation of URM in JavaScript a few years back](https://github.com/sierisimo/URMjs/) if you're interested in give it a check. It's code from when I was at university but still works (it has almost no dependencies).

This is something I wanted to share (and something I'll use for later examples) and I hope gives you the same amount of fun it gave to me. It's fun to code this as an exercise. I recommend you to give it a try (I know I will in Kotlin).
