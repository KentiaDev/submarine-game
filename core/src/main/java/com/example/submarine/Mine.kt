package com.example.submarine

import com.badlogic.gdx.math.Circle

class Mine(
    @JvmField var x: Float, @JvmField var y: Float, @JvmField var width: Float, @JvmField var height: Float,
    endY: Float, speed: Float, proximityRadius: Float, explosionRadius: Float
) {
    @JvmField
    var startY: Float
    @JvmField
    var endY: Float
    @JvmField
    var speed: Float
    @JvmField
    var movingUp: Boolean
    @JvmField
    var active: Boolean
    @JvmField
    var exploded: Boolean

    @JvmField
    var proximityRadius: Float
    @JvmField
    var explosionRadius: Float

    @JvmField
    var activationTime: Float = 0f
    @JvmField
    var explosionTimer: Float = 0f

    init {
        this.startY = y
        this.endY = endY
        this.speed = speed
        this.movingUp = true

        this.active = false
        this.exploded = false

        this.proximityRadius = proximityRadius
        this.explosionRadius = explosionRadius
    }

    val proximityCircle: Circle
        get() = Circle(x + width / 2f, y + height / 2f, proximityRadius)

    val explosionCircle: Circle
        get() = Circle(x + width / 2f, y + height / 2f, explosionRadius)
}
