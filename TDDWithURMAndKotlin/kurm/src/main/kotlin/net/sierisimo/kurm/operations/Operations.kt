package net.sierisimo.kurm.operations

import net.sierisimo.kurm.InstructionSet
import net.sierisimo.kurm.Registry

/**
 * Function that simulates the `Z(x)` functionality of URM. It takes one position in the registry and sets the value to 0
 *
 * @param registry An implementation of [Registry]
 * @param position the value of `x` that will be set to 0
 *
 * @throws IllegalArgumentException if the position is less than 0
 */
fun zero(registry: Registry, position: Int) {
    require(position > 0) { "Position must be positive number" }

    registry.setValueAtPosition(position, 0)
}

/**
 * Function that simulates the `I(x)` functionality of URM. It takes the value of a position in registry and adds 1 to it.
 * It works in a _strict_ mode where a register without value cannot be incremented
 *
 * @param registry An implementation of [Registry]
 * @param position the register position in the [Registry] that will get it's value incremented in 1
 *
 * @throws IllegalArgumentException if the position is less than 0
 * @throws IllegalStateException if the register doesn't have a value
 */
fun increment(registry: Registry, position: Int) {
    require(position > 0) { "Position must be positive number" }

    val currentValue = registry.getValueAtPosition(position)
    checkNotNull(currentValue) { "Register must be initialized" }

    registry.setValueAtPosition(position, currentValue + 1)
}

/**
 *
 */
fun jump(registry: Registry, positionX: Int, positionY: Int, instructionSet: InstructionSet, instruction: Int) {
    require(positionX > 0) { "Position must be positive number" }
    require(positionY > 0) { "Position must be positive number" }
    require(instruction > 0) { "Instruction must be positive number" }

    val xValue = registry.getValueAtPosition(positionX)
    val yValue = registry.getValueAtPosition(positionY)

    checkNotNull(xValue) { "Register must be initialized" }
    checkNotNull(yValue) { "Register must be initialized" }

    if (xValue == yValue) {
        instructionSet.current = instruction
    } else {
        instructionSet.current++
    }
}