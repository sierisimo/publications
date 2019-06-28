package net.sierisimo.kurm

interface Registry {
    fun getValueAtPosition(position: Int): Int?

    fun setValueAtPosition(position: Int, value: Int)
}