package com.example.submarine

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Array
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class Main : ApplicationAdapter() {
    private var camera: OrthographicCamera? = null
    private var hudCamera: OrthographicCamera? = null

    private var map: TiledMap? = null
    private var renderer: OrthogonalTiledMapRenderer? = null
    private var collisionLayer: TiledMapTileLayer? = null

    private var batch: SpriteBatch? = null

    private var fondo: Texture? = null
    private var treasureTexture: Texture? = null
    private var bubbleTexture: Texture? = null
    private var mineTexture: Texture? = null
    private var plantTexture: Texture? = null
    private var chestIcon: Texture? = null
    private var whitePixel: Texture? = null

    private val swimAnimation: Animation<TextureRegion?>? = null
    private var stateTime = 0f

    private var playerX = 0f
    private var playerY = 0f
    private var velocityY = 0f

    private var treasures: Array<Treasure>? = null
    private var bubbles: Array<Treasure>? = null
    private var mines: Array<Mine>? = null
    private var plants: Array<Treasure>? = null

    private var score = 0
    private var oxygen = 0f

    private var gameOver = false
    private var win = false

    private var survivalTime = 0f
    private var collectedTreasures = 0

    private val maxOxygen = 100f
    private val oxygenDrainRate = 1.5f

    private var font: BitmapFont? = null
    private var idleAnimation: Animation<TextureRegion?>? = null
    private var upAnimation: Animation<TextureRegion?>? = null
    private var downAnimation: Animation<TextureRegion?>? = null
    private var sideAnimation: Animation<TextureRegion?>? = null

    private var facingRight = true

    private enum class PlayerState {
        IDLE,
        UP,
        DOWN,
        SIDE
    }

    private val playerWidth = 120f
    private val playerHeight = 60f

    private val hitboxWidth = 60f
    private val hitboxHeight = 28f
    private val hitboxOffsetX = 30f
    private val hitboxOffsetY = 16f

    private val horizontalSpeed = 220f
    private val verticalSpeed = 170f
    private val floatDownSpeed = 25f

    private val TILE_SIZE = 64

    private var damageTimer = 0f
    private val damageDuration = 0.5f

    private var shakeTimer = 0f
    private val shakeDuration = 0.5f

    override fun create() {
        camera = OrthographicCamera()
        camera!!.setToOrtho(false, 800f, 480f)

        hudCamera = OrthographicCamera()
        hudCamera!!.setToOrtho(false, 800f, 480f)

        map = TmxMapLoader().load("Mapa.tmx")
        renderer = OrthogonalTiledMapRenderer(map)
        collisionLayer = map!!.getLayers().get(0) as TiledMapTileLayer

        batch = SpriteBatch()

        fondo = Texture("Fondo5.png")
        treasureTexture = Texture("treasure.png")
        bubbleTexture = Texture("bubble.png")
        mineTexture = Texture("mine.png")
        plantTexture = Texture("plant.png")
        chestIcon = Texture("treasure.png")

        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        whitePixel = Texture(pixmap)
        pixmap.dispose()

        val sheet = Texture("player.png")
        val tmp = TextureRegion.split(sheet, 80, 80)

        val idleFrames = arrayOfNulls<TextureRegion>(1)
        idleFrames[0] = tmp[0]!![0]

        val upFrames = arrayOfNulls<TextureRegion>(2)
        upFrames[0] = tmp[0]!![1]
        upFrames[1] = tmp[0]!![2]

        val downFrames = arrayOfNulls<TextureRegion>(2)
        downFrames[0] = tmp[0]!![3]
        downFrames[1] = tmp[0]!![4]

        val sideFrames = arrayOfNulls<TextureRegion>(5)
        for (i in 0..4) {
            sideFrames[i] = tmp[0]!![i]
        }

        idleAnimation = Animation<TextureRegion?>(0.3f, *idleFrames)
        upAnimation = Animation<TextureRegion?>(0.15f, *upFrames)
        downAnimation = Animation<TextureRegion?>(0.15f, *downFrames)
        sideAnimation = Animation<TextureRegion?>(0.12f, *sideFrames)
        stateTime = 0f

        playerX = 120f
        playerY = 180f
        velocityY = 0f

        treasures = Array<Treasure>()
        treasures!!.add(Treasure(350f, 375f, 40f, 40f))
        treasures!!.add(Treasure(910f, 1397f, 40f, 40f))
        treasures!!.add(Treasure(2825f, 758f, 40f, 40f))
        treasures!!.add(Treasure(3000f, 1782f, 40f, 40f))
        treasures!!.add(Treasure(4940f, 820f, 40f, 40f))
        treasures!!.add(Treasure(5775f, 375f, 40f, 40f))
        treasures!!.add(Treasure(5700f, 1463f, 40f, 40f))
        treasures!!.add(Treasure(222f, 1718f, 40f, 40f))

        bubbles = Array<Treasure>()
        bubbles!!.add(Treasure(365f, 375f, 40f, 40f))
        bubbles!!.add(Treasure(925f, 120f, 40f, 40f))
        bubbles!!.add(Treasure(1885f, 120f, 40f, 40f))
        bubbles!!.add(Treasure(4225f, 120f, 40f, 40f))
        bubbles!!.add(Treasure(5595f, 185f, 40f, 40f))

        plants = Array<Treasure>()
        plants!!.add(Treasure(340f, 375f, 80f, 120f))
        plants!!.add(Treasure(900f, 120f, 70f, 110f))
        plants!!.add(Treasure(1850f, 120f, 90f, 130f))
        plants!!.add(Treasure(4200f, 120f, 80f, 120f))
        plants!!.add(Treasure(5570f, 185f, 80f, 120f))

        mines = Array<Mine>()

        val mitadPantalla = 300f

        val mine1 = Mine(1125f, 1725f, 50f, 50f, mitadPantalla, 70f, 120f, 80f)
        mine1.movingUp = false

        val mine2 = Mine(1750f, 1450f, 50f, 50f, mitadPantalla - 20, 65f, 120f, 120f)
        mine2.movingUp = false

        val mine3 = Mine(1500f, 800f, 50f, 50f, mitadPantalla + 20, 75f, 120f, 120f)
        mine3.movingUp = false

        val mine4 = Mine(3500f, 700f, 50f, 50f, mitadPantalla - 10, 68f, 120f, 120f)
        mine4.movingUp = false

        val mine5 = Mine(3800f, 800f, 50f, 50f, mitadPantalla + 10, 72f, 120f, 120f)
        mine5.movingUp = false

        val mine6 = Mine(5200f, 500f, 50f, 50f, mitadPantalla, 70f, 120f, 120f)
        mine6.movingUp = false

        val mine7 = Mine(3075f, 200f, 50f, 50f, mitadPantalla, 65f, 120f, 120f)
        val mine8 = Mine(6125f, 130f, 50f, 50f, mitadPantalla, 70f, 120f, 120f)

        mines!!.add(mine1)
        mines!!.add(mine2)
        mines!!.add(mine3)
        mines!!.add(mine4)
        mines!!.add(mine5)
        mines!!.add(mine6)
        mines!!.add(mine7)
        mines!!.add(mine8)

        score = 0
        oxygen = maxOxygen
        survivalTime = 0f
        collectedTreasures = 0
        gameOver = false
        win = false

        font = BitmapFont()
        font!!.getData().setScale(2f)
        font!!.setColor(Color.WHITE)
    }

    override fun render() {
        val delta = Gdx.graphics.getDeltaTime()

        if ((gameOver || win) && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetGame()
        }

        if (damageTimer > 0) damageTimer -= delta
        if (shakeTimer > 0) shakeTimer -= delta

        if (!gameOver && !win) {
            survivalTime += delta

            handleInput(delta)
            updatePlayer(delta)
            updateMines(delta)
            checkMineProximity()
            updateMinesExplosion(delta)
            updateOxygen(delta)
            updateBubbles(delta)
            checkTreasureCollection()
            checkBubbleCollection()
        }

        updateCamera()

        stateTime += delta
        val currentFrame = this.currentPlayerFrame

        camera!!.update()
        hudCamera!!.update()

        // FONDO
        batch!!.setProjectionMatrix(camera!!.combined)
        batch!!.begin()
        batch!!.draw(
            fondo,
            camera!!.position.x - camera!!.viewportWidth / 2,
            camera!!.position.y - camera!!.viewportHeight / 2,
            camera!!.viewportWidth,
            camera!!.viewportHeight
        )
        batch!!.end()

        // MAPA / PLATAFORMAS
        renderer!!.setView(camera)
        renderer!!.render()

        // OBJETOS DEL MUNDO
        batch!!.setProjectionMatrix(camera!!.combined)
        batch!!.begin()

        for (plant in plants!!) {
            val angle = sin((stateTime * 1 + plant.x * 0.01f).toDouble()).toFloat() * 3f

            batch!!.draw(
                plantTexture,
                plant.x,
                plant.y,
                plant.width / 2,
                0f,
                plant.width,
                plant.height,
                1f,
                1f,
                angle,
                0,
                0,
                plantTexture!!.getWidth(),
                plantTexture!!.getHeight(),
                false,
                false
            )
        }

        for (treasure in treasures!!) {
            if (!treasure.collected) {
                batch!!.draw(
                    treasureTexture,
                    treasure.x,
                    treasure.y,
                    treasure.width,
                    treasure.height
                )
            }
        }

        for (bubble in bubbles!!) {
            if (!bubble.collected) {
                batch!!.draw(bubbleTexture, bubble.x, bubble.y, bubble.width, bubble.height)
            }
        }

        for (mine in mines!!) {
            if (!mine.exploded) {
                if (mine.active) {
                    val blink = sin((mine.activationTime * 20).toDouble()).toFloat()
                    if (blink > 0) {
                        batch!!.setColor(1f, 0.2f, 0.2f, 1f)
                    } else {
                        batch!!.setColor(1f, 1f, 1f, 1f)
                    }
                } else {
                    batch!!.setColor(1f, 1f, 1f, 1f)
                }

                batch!!.draw(mineTexture, mine.x, mine.y, mine.width, mine.height)
            }

            if (mine.exploded && mine.explosionTimer < 0.5f) {
                val size = mine.width + mine.explosionTimer * 200
                batch!!.setColor(1f, 0.5f, 0.2f, 0.7f)

                batch!!.draw(
                    mineTexture,
                    mine.x + mine.width / 2 - size / 2,
                    mine.y + mine.height / 2 - size / 2,
                    size,
                    size
                )
            }
        }

        batch!!.setColor(1f, 1f, 1f, 1f)

        var drawX = playerX
        var drawY = playerY

        if (shakeTimer > 0) {
            drawX += ((Math.random() - 0.5f) * 20).toFloat()
            drawY += ((Math.random() - 0.5f) * 10).toFloat()
        }

        if (damageTimer > 0) {
            batch!!.setColor(1f, 0.3f, 0.3f, 1f)
        }

        if (facingRight) {
            batch!!.draw(currentFrame, drawX, drawY, playerWidth, playerHeight)
        } else {
            batch!!.draw(currentFrame, drawX + playerWidth, drawY, -playerWidth, playerHeight)
        }

        batch!!.setColor(1f, 1f, 1f, 1f)
        batch!!.end()

        // HUD FIJO EN PANTALLA
        batch!!.setProjectionMatrix(hudCamera!!.combined)
        batch!!.begin()

        batch!!.setColor(0f, 0f, 0f, 0.65f)
        batch!!.draw(whitePixel, 0f, 420f, 800f, 60f)
        batch!!.setColor(1f, 1f, 1f, 1f)

        val totalTreasures = treasures!!.size
        var collected = 0

        for (t in treasures!!) {
            if (t.collected) {
                collected++
            }
        }

        batch!!.draw(chestIcon, 25f, 430f, 40f, 40f)
        font!!.draw(batch, collected.toString() + " / " + totalTreasures, 75f, 458f)

        val barX = 330f
        val barY = 440f
        val barWidth = 260f
        val barHeight = 20f
        val oxygenPercent = oxygen / maxOxygen

        font!!.draw(batch, "Oxigeno", 210f, 458f)

        batch!!.setColor(0.2f, 0.2f, 0.2f, 1f)
        batch!!.draw(whitePixel, barX, barY, barWidth, barHeight)

        batch!!.setColor(0f, 0.6f, 1f, 1f)
        batch!!.draw(whitePixel, barX, barY, barWidth * oxygenPercent, barHeight)

        batch!!.setColor(1f, 1f, 1f, 1f)

        if (gameOver || win) {
            batch!!.setColor(0f, 0f, 0f, 0.75f)
            batch!!.draw(whitePixel, 0f, 0f, 800f, 480f)
            batch!!.setColor(1f, 1f, 1f, 1f)

            font!!.getData().setScale(4f)

            if (gameOver) {
                font!!.setColor(Color.RED)
                font!!.draw(batch, "GAME OVER", 250f, 310f)
            }

            if (win) {
                font!!.setColor(Color.GREEN)
                font!!.draw(batch, "YOU WIN!", 270f, 310f)
            }

            font!!.getData().setScale(2f)
            font!!.setColor(Color.WHITE)

            font!!.draw(
                batch,
                "Tesoros: " + collectedTreasures + " / " + totalTreasures,
                270f,
                250f
            )
            font!!.draw(batch, "Tiempo: " + survivalTime.toInt() + "s", 270f, 220f)
            font!!.draw(batch, "Puntuacion: " + score, 270f, 190f)
            font!!.draw(batch, "Pulsa R para reiniciar", 250f, 150f)
        }

        batch!!.end()
    }

    private fun handleInput(delta: Float) {
        var nextX = playerX

        val checkY1 = playerY + hitboxOffsetY + 4
        val checkY2 = playerY + hitboxOffsetY + hitboxHeight - 4

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            facingRight = false
            nextX -= horizontalSpeed * delta
            val leftCheckX = nextX + hitboxOffsetX

            if (!isSolid(leftCheckX, checkY1) && !isSolid(leftCheckX, checkY2)) {
                playerX = nextX
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            facingRight = true
            nextX += horizontalSpeed * delta
            val rightCheckX = nextX + hitboxOffsetX + hitboxWidth

            if (!isSolid(rightCheckX, checkY1) && !isSolid(rightCheckX, checkY2)) {
                playerX = nextX
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            velocityY = verticalSpeed
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            velocityY = -verticalSpeed
        } else {
            velocityY = -floatDownSpeed
        }

        clampPlayerToMap()
    }

    private fun updatePlayer(delta: Float) {
        val nextY = playerY + velocityY * delta

        val leftX = playerX + hitboxOffsetX
        val rightX = playerX + hitboxOffsetX + hitboxWidth
        val bottomY = nextY + hitboxOffsetY
        val topY = nextY + hitboxOffsetY + hitboxHeight

        if (velocityY > 0) {
            if (isSolid(leftX + 4, topY) || isSolid(rightX - 4, topY)) {
                velocityY = 0f
            } else {
                playerY = nextY
            }
        } else if (velocityY < 0) {
            if (isSolid(leftX + 4, bottomY) || isSolid(rightX - 4, bottomY)) {
                val tileY = (bottomY / TILE_SIZE).toInt()
                playerY = (tileY + 1) * TILE_SIZE - hitboxOffsetY
                velocityY = 0f
            } else {
                playerY = nextY
            }
        }

        clampPlayerToMap()
    }

    private fun clampPlayerToMap() {
        val mapWidth = (collisionLayer!!.getWidth() * TILE_SIZE).toFloat()
        val mapHeight = (collisionLayer!!.getHeight() * TILE_SIZE).toFloat()

        if (playerX < 0) {
            playerX = 0f
        }

        if (playerX > mapWidth - playerWidth) {
            playerX = mapWidth - playerWidth
        }

        if (playerY < 0) {
            playerY = 0f
            velocityY = 0f
        }

        if (playerY > mapHeight - playerHeight) {
            playerY = mapHeight - playerHeight
            velocityY = 0f
        }
    }

    private fun isSolid(x: Float, y: Float): Boolean {
        val tileX = (x / TILE_SIZE).toInt()
        val tileY = (y / TILE_SIZE).toInt()

        if (tileX < 0 || tileY < 0 || tileX >= collisionLayer!!.getWidth() || tileY >= collisionLayer!!.getHeight()) {
            return false
        }

        val cell = collisionLayer!!.getCell(tileX, tileY)
        return cell != null && cell.getTile() != null
    }

    private fun checkTreasureCollection() {
        val playerBounds = Rectangle(playerX, playerY, playerWidth, playerHeight)

        for (treasure in treasures!!) {
            if (!treasure.collected && playerBounds.overlaps(treasure.bounds)) {
                treasure.collected = true
                score += 10
                collectedTreasures++
            }
        }

        var allCollected = true
        for (treasure in treasures!!) {
            if (!treasure.collected) {
                allCollected = false
                break
            }
        }

        if (allCollected) {
            win = true
        }
    }

    private fun checkBubbleCollection() {
        val playerBounds = Rectangle(playerX, playerY, playerWidth, playerHeight)

        for (bubble in bubbles!!) {
            if (playerBounds.overlaps(bubble.bounds)) {
                oxygen += 20f

                if (oxygen > maxOxygen) {
                    oxygen = maxOxygen
                }

                resetBubbleToPlant(bubble)
            }
        }
    }

    private fun updateOxygen(delta: Float) {
        if (gameOver) return

        oxygen -= oxygenDrainRate * delta

        if (oxygen <= 0) {
            oxygen = 0f
            gameOver = true
        }
    }

    private fun updateBubbles(delta: Float) {
        for (bubble in bubbles!!) {
            if (!bubble.collected) {
                bubble.y += 25 * delta

                if (bubble.y > camera!!.position.y + 400) {
                    resetBubbleToPlant(bubble)
                }
            }
        }
    }

    private fun resetBubbleToPlant(bubble: Treasure) {
        if (bubble.x < 700) {
            bubble.x = 365f
            bubble.y = 375f
        } else if (bubble.x < 1300) {
            bubble.x = 925f
            bubble.y = 120f
        } else if (bubble.x < 2500) {
            bubble.x = 1885f
            bubble.y = 120f
        } else if (bubble.x < 5000) {
            bubble.x = 4225f
            bubble.y = 120f
        } else {
            bubble.x = 5595f
            bubble.y = 185f
        }
    }

    private fun updateMines(delta: Float) {
        for (mine in mines!!) {
            if (mine.exploded) continue

            val minY = min(mine.startY, mine.endY)
            val maxY = max(mine.startY, mine.endY)

            if (mine.movingUp) {
                mine.y += mine.speed * delta

                if (mine.y >= maxY) {
                    mine.y = maxY
                    mine.movingUp = false
                }
            } else {
                mine.y -= mine.speed * delta

                if (mine.y <= minY) {
                    mine.y = minY
                    mine.movingUp = true
                }
            }
        }
    }

    private fun checkMineProximity() {
        val playerCenterX = playerX + playerWidth / 2
        val playerCenterY = playerY + playerHeight / 2

        for (mine in mines!!) {
            if (mine.exploded) continue

            val mineCenterX = mine.x + mine.width / 2
            val mineCenterY = mine.y + mine.height / 2

            val dx = playerCenterX - mineCenterX
            val dy = playerCenterY - mineCenterY

            val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

            if (distance < mine.proximityRadius) {
                mine.active = true
            }
        }
    }

    private fun updateMinesExplosion(delta: Float) {
        val playerCenterX = playerX + playerWidth / 2
        val playerCenterY = playerY + playerHeight / 2

        for (mine in mines!!) {
            if (mine.exploded) {
                mine.explosionTimer += delta

                if (mine.explosionTimer > 2f) {
                    mine.exploded = false
                    mine.active = false
                    mine.activationTime = 0f
                    mine.explosionTimer = 0f
                    mine.y = mine.startY
                }

                continue
            }

            if (mine.active) {
                mine.activationTime += delta

                if (mine.activationTime > 1f) {
                    mine.exploded = true
                    mine.explosionTimer = 0f

                    val mineCenterX = mine.x + mine.width / 2
                    val mineCenterY = mine.y + mine.height / 2

                    val dx = playerCenterX - mineCenterX
                    val dy = playerCenterY - mineCenterY
                    val distance = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                    if (distance < mine.explosionRadius) {
                        oxygen -= 30f

                        if (oxygen < 0) {
                            oxygen = 0f
                        }

                        damageTimer = damageDuration
                        shakeTimer = shakeDuration
                    }
                }
            }
        }
    }

    private fun updateCamera() {
        val mapWidth = (collisionLayer!!.getWidth() * TILE_SIZE).toFloat()
        val mapHeight = (collisionLayer!!.getHeight() * TILE_SIZE).toFloat()

        camera!!.position.x = playerX + playerWidth / 2
        camera!!.position.y = playerY + playerHeight / 2

        if (camera!!.position.x < camera!!.viewportWidth / 2) {
            camera!!.position.x = camera!!.viewportWidth / 2
        }

        if (camera!!.position.x > mapWidth - camera!!.viewportWidth / 2) {
            camera!!.position.x = mapWidth - camera!!.viewportWidth / 2
        }

        if (camera!!.position.y < camera!!.viewportHeight / 2) {
            camera!!.position.y = camera!!.viewportHeight / 2
        }

        if (camera!!.position.y > mapHeight - camera!!.viewportHeight / 2) {
            camera!!.position.y = mapHeight - camera!!.viewportHeight / 2
        }
    }

    private fun resetGame() {
        playerX = 120f
        playerY = 180f
        velocityY = 0f

        score = 0
        oxygen = maxOxygen
        survivalTime = 0f
        collectedTreasures = 0

        gameOver = false
        win = false

        damageTimer = 0f
        shakeTimer = 0f

        for (treasure in treasures!!) {
            treasure.collected = false
        }

        for (bubble in bubbles!!) {
            resetBubbleToPlant(bubble)
        }

        for (mine in mines!!) {
            mine.active = false
            mine.exploded = false
            mine.activationTime = 0f
            mine.explosionTimer = 0f
            mine.y = mine.startY
        }
    }

    override fun dispose() {
        map!!.dispose()
        renderer!!.dispose()
        batch!!.dispose()

        fondo!!.dispose()
        plantTexture!!.dispose()
        treasureTexture!!.dispose()
        chestIcon!!.dispose()
        bubbleTexture!!.dispose()
        mineTexture!!.dispose()
        whitePixel!!.dispose()

        font!!.dispose()
    }

    private val currentPlayerFrame: TextureRegion?
        get() {
            val pressingLeft =
                Gdx.input.isKeyPressed(Input.Keys.LEFT)
            val pressingRight =
                Gdx.input.isKeyPressed(Input.Keys.RIGHT)
            val pressingUp = Gdx.input.isKeyPressed(Input.Keys.UP)
            val pressingDown =
                Gdx.input.isKeyPressed(Input.Keys.DOWN)

            if (pressingUp) {
                return upAnimation!!.getKeyFrame(stateTime, true)
            }

            if (pressingDown || velocityY < 0) {
                return downAnimation!!.getKeyFrame(stateTime, true)
            }

            if (pressingLeft || pressingRight) {
                return sideAnimation!!.getKeyFrame(stateTime, true)
            }

            return idleAnimation!!.getKeyFrame(stateTime, true)
        }
}
