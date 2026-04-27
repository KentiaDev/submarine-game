package com.example.submarine

import com.badlogic.gdx.math.Rectangle

class Treasure(@JvmField var x: Float, @JvmField var y: Float, @JvmField var width: Float, @JvmField var height: Float) {
    @JvmField
    var collected: Boolean = false

    val bounds: Rectangle
        get() = Rectangle(x, y, width, height)
}
