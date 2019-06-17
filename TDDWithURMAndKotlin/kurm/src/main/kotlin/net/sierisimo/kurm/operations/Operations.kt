package net.sierisimo.kurm.operations

import net.sierisimo.kurm.Registry

fun zero(registry: Registry, position: Int) {
    require(position > 0) { "Position must be positive number" }

    registry.setValueAtPosition(position, 0)
}