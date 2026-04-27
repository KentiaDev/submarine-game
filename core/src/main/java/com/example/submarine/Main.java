package com.example.submarine;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Main extends ApplicationAdapter {

    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private TiledMapTileLayer collisionLayer;

    private SpriteBatch batch;

    private Texture fondo;
    private Texture treasureTexture;
    private Texture bubbleTexture;
    private Texture mineTexture;
    private Texture plantTexture;
    private Texture chestIcon;
    private Texture whitePixel;

    private Animation<TextureRegion> swimAnimation;
    private float stateTime;

    private float playerX;
    private float playerY;
    private float velocityY;

    private Array<Treasure> treasures;
    private Array<Treasure> bubbles;
    private Array<Mine> mines;
    private Array<Treasure> plants;

    private int score;
    private float oxygen;

    private boolean gameOver = false;
    private boolean win = false;

    private float survivalTime = 0f;
    private int collectedTreasures = 0;

    private final float maxOxygen = 100f;
    private final float oxygenDrainRate = 1.5f;

    private BitmapFont font;
    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> upAnimation;
    private Animation<TextureRegion> downAnimation;
    private Animation<TextureRegion> sideAnimation;

    private boolean facingRight = true;

    private enum PlayerState {
        IDLE,
        UP,
        DOWN,
        SIDE
    }

    private final float playerWidth = 120f;
    private final float playerHeight = 60f;

    private final float hitboxWidth = 60f;
    private final float hitboxHeight = 28f;
    private final float hitboxOffsetX = 30f;
    private final float hitboxOffsetY = 16f;

    private final float horizontalSpeed = 220f;
    private final float verticalSpeed = 170f;
    private final float floatDownSpeed = 25f;

    private final int TILE_SIZE = 64;

    private float damageTimer = 0f;
    private final float damageDuration = 0.5f;

    private float shakeTimer = 0f;
    private final float shakeDuration = 0.5f;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, 800, 480);

        map = new TmxMapLoader().load("Mapa.tmx");
        renderer = new OrthogonalTiledMapRenderer(map);
        collisionLayer = (TiledMapTileLayer) map.getLayers().get(0);

        batch = new SpriteBatch();

        fondo = new Texture("Fondo5.png");
        treasureTexture = new Texture("treasure.png");
        bubbleTexture = new Texture("bubble.png");
        mineTexture = new Texture("mine.png");
        plantTexture = new Texture("plant.png");
        chestIcon = new Texture("treasure.png");

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();

        Texture sheet = new Texture("player.png");
        TextureRegion[][] tmp = TextureRegion.split(sheet, 80, 80);

        TextureRegion[] idleFrames = new TextureRegion[1];
        idleFrames[0] = tmp[0][0];

        TextureRegion[] upFrames = new TextureRegion[2];
        upFrames[0] = tmp[0][1];
        upFrames[1] = tmp[0][2];

        TextureRegion[] downFrames = new TextureRegion[2];
        downFrames[0] = tmp[0][3];
        downFrames[1] = tmp[0][4];

        TextureRegion[] sideFrames = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            sideFrames[i] = tmp[0][i];
        }

        idleAnimation = new Animation<>(0.3f, idleFrames);
        upAnimation = new Animation<>(0.15f, upFrames);
        downAnimation = new Animation<>(0.15f, downFrames);
        sideAnimation = new Animation<>(0.12f, sideFrames);
        stateTime = 0f;

        playerX = 120f;
        playerY = 180f;
        velocityY = 0f;

        treasures = new Array<>();
        treasures.add(new Treasure(350, 375, 40, 40));
        treasures.add(new Treasure(910, 1397, 40, 40));
        treasures.add(new Treasure(2825, 758, 40, 40));
        treasures.add(new Treasure(3000, 1782, 40, 40));
        treasures.add(new Treasure(4940, 820, 40, 40));
        treasures.add(new Treasure(5775, 375, 40, 40));
        treasures.add(new Treasure(5700, 1463, 40, 40));
        treasures.add(new Treasure(222, 1718, 40, 40));

        bubbles = new Array<>();
        bubbles.add(new Treasure(365, 375, 40, 40));
        bubbles.add(new Treasure(925, 120, 40, 40));
        bubbles.add(new Treasure(1885, 120, 40, 40));
        bubbles.add(new Treasure(4225, 120, 40, 40));
        bubbles.add(new Treasure(5595, 185, 40, 40));

        plants = new Array<>();
        plants.add(new Treasure(340, 375, 80, 120));
        plants.add(new Treasure(900, 120, 70, 110));
        plants.add(new Treasure(1850, 120, 90, 130));
        plants.add(new Treasure(4200, 120, 80, 120));
        plants.add(new Treasure(5570, 185, 80, 120));

        mines = new Array<>();

        float mitadPantalla = 300;

        Mine mine1 = new Mine(1125, 1725, 50, 50, mitadPantalla, 70, 120, 80);
        mine1.movingUp = false;

        Mine mine2 = new Mine(1750, 1450, 50, 50, mitadPantalla - 20, 65, 120, 120);
        mine2.movingUp = false;

        Mine mine3 = new Mine(1500, 800, 50, 50, mitadPantalla + 20, 75, 120, 120);
        mine3.movingUp = false;

        Mine mine4 = new Mine(3500, 700, 50, 50, mitadPantalla - 10, 68, 120, 120);
        mine4.movingUp = false;

        Mine mine5 = new Mine(3800, 800, 50, 50, mitadPantalla + 10, 72, 120, 120);
        mine5.movingUp = false;

        Mine mine6 = new Mine(5200, 500, 50, 50, mitadPantalla, 70, 120, 120);
        mine6.movingUp = false;

        Mine mine7 = new Mine(3075, 200, 50, 50, mitadPantalla, 65, 120, 120);
        Mine mine8 = new Mine(6125, 130, 50, 50, mitadPantalla, 70, 120, 120);

        mines.add(mine1);
        mines.add(mine2);
        mines.add(mine3);
        mines.add(mine4);
        mines.add(mine5);
        mines.add(mine6);
        mines.add(mine7);
        mines.add(mine8);

        score = 0;
        oxygen = maxOxygen;
        survivalTime = 0f;
        collectedTreasures = 0;
        gameOver = false;
        win = false;

        font = new BitmapFont();
        font.getData().setScale(2f);
        font.setColor(Color.WHITE);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        if ((gameOver || win) && Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            resetGame();
        }

        if (damageTimer > 0) damageTimer -= delta;
        if (shakeTimer > 0) shakeTimer -= delta;

        if (!gameOver && !win) {
            survivalTime += delta;

            handleInput(delta);
            updatePlayer(delta);
            updateMines(delta);
            checkMineProximity();
            updateMinesExplosion(delta);
            updateOxygen(delta);
            updateBubbles(delta);
            checkTreasureCollection();
            checkBubbleCollection();
        }

        updateCamera();

        stateTime += delta;
        TextureRegion currentFrame = getCurrentPlayerFrame();

        camera.update();
        hudCamera.update();

        // FONDO
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(
            fondo,
            camera.position.x - camera.viewportWidth / 2,
            camera.position.y - camera.viewportHeight / 2,
            camera.viewportWidth,
            camera.viewportHeight
        );
        batch.end();

        // MAPA / PLATAFORMAS
        renderer.setView(camera);
        renderer.render();

        // OBJETOS DEL MUNDO
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Treasure plant : plants) {
            float angle = (float) Math.sin(stateTime * 1 + plant.x * 0.01f) * 3f;

            batch.draw(
                plantTexture,
                plant.x,
                plant.y,
                plant.width / 2,
                0,
                plant.width,
                plant.height,
                1,
                1,
                angle,
                0,
                0,
                plantTexture.getWidth(),
                plantTexture.getHeight(),
                false,
                false
            );
        }

        for (Treasure treasure : treasures) {
            if (!treasure.collected) {
                batch.draw(treasureTexture, treasure.x, treasure.y, treasure.width, treasure.height);
            }
        }

        for (Treasure bubble : bubbles) {
            if (!bubble.collected) {
                batch.draw(bubbleTexture, bubble.x, bubble.y, bubble.width, bubble.height);
            }
        }

        for (Mine mine : mines) {
            if (!mine.exploded) {
                if (mine.active) {
                    float blink = (float) Math.sin(mine.activationTime * 20);
                    if (blink > 0) {
                        batch.setColor(1, 0.2f, 0.2f, 1);
                    } else {
                        batch.setColor(1, 1, 1, 1);
                    }
                } else {
                    batch.setColor(1, 1, 1, 1);
                }

                batch.draw(mineTexture, mine.x, mine.y, mine.width, mine.height);
            }

            if (mine.exploded && mine.explosionTimer < 0.5f) {
                float size = mine.width + mine.explosionTimer * 200;
                batch.setColor(1, 0.5f, 0.2f, 0.7f);

                batch.draw(
                    mineTexture,
                    mine.x + mine.width / 2 - size / 2,
                    mine.y + mine.height / 2 - size / 2,
                    size,
                    size
                );
            }
        }

        batch.setColor(1, 1, 1, 1);

        float drawX = playerX;
        float drawY = playerY;

        if (shakeTimer > 0) {
            drawX += (Math.random() - 0.5f) * 20;
            drawY += (Math.random() - 0.5f) * 10;
        }

        if (damageTimer > 0) {
            batch.setColor(1, 0.3f, 0.3f, 1);
        }

        if (facingRight) {
            batch.draw(currentFrame, drawX, drawY, playerWidth, playerHeight);
        } else {
            batch.draw(currentFrame, drawX + playerWidth, drawY, -playerWidth, playerHeight);
        }

        batch.setColor(1, 1, 1, 1);
        batch.end();

        // HUD FIJO EN PANTALLA
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        batch.setColor(0, 0, 0, 0.65f);
        batch.draw(whitePixel, 0, 420, 800, 60);
        batch.setColor(1, 1, 1, 1);

        int totalTreasures = treasures.size;
        int collected = 0;

        for (Treasure t : treasures) {
            if (t.collected) {
                collected++;
            }
        }

        batch.draw(chestIcon, 25, 430, 40, 40);
        font.draw(batch, collected + " / " + totalTreasures, 75, 458);

        float barX = 330;
        float barY = 440;
        float barWidth = 260;
        float barHeight = 20;
        float oxygenPercent = oxygen / maxOxygen;

        font.draw(batch, "Oxigeno", 210, 458);

        batch.setColor(0.2f, 0.2f, 0.2f, 1);
        batch.draw(whitePixel, barX, barY, barWidth, barHeight);

        batch.setColor(0, 0.6f, 1, 1);
        batch.draw(whitePixel, barX, barY, barWidth * oxygenPercent, barHeight);

        batch.setColor(1, 1, 1, 1);

        if (gameOver || win) {
            batch.setColor(0, 0, 0, 0.75f);
            batch.draw(whitePixel, 0, 0, 800, 480);
            batch.setColor(1, 1, 1, 1);

            font.getData().setScale(4f);

            if (gameOver) {
                font.setColor(Color.RED);
                font.draw(batch, "GAME OVER", 250, 310);
            }

            if (win) {
                font.setColor(Color.GREEN);
                font.draw(batch, "YOU WIN!", 270, 310);
            }

            font.getData().setScale(2f);
            font.setColor(Color.WHITE);

            font.draw(batch, "Tesoros: " + collectedTreasures + " / " + totalTreasures, 270, 250);
            font.draw(batch, "Tiempo: " + (int) survivalTime + "s", 270, 220);
            font.draw(batch, "Puntuacion: " + score, 270, 190);
            font.draw(batch, "Pulsa R para reiniciar", 250, 150);
        }

        batch.end();
    }

    private void handleInput(float delta) {
        float nextX = playerX;

        float checkY1 = playerY + hitboxOffsetY + 4;
        float checkY2 = playerY + hitboxOffsetY + hitboxHeight - 4;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            facingRight = false;
            nextX -= horizontalSpeed * delta;
            float leftCheckX = nextX + hitboxOffsetX;

            if (!isSolid(leftCheckX, checkY1) && !isSolid(leftCheckX, checkY2)) {
                playerX = nextX;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            facingRight = true;
            nextX += horizontalSpeed * delta;
            float rightCheckX = nextX + hitboxOffsetX + hitboxWidth;

            if (!isSolid(rightCheckX, checkY1) && !isSolid(rightCheckX, checkY2)) {
                playerX = nextX;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            velocityY = verticalSpeed;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            velocityY = -verticalSpeed;
        } else {
            velocityY = -floatDownSpeed;
        }

        clampPlayerToMap();
    }

    private void updatePlayer(float delta) {
        float nextY = playerY + velocityY * delta;

        float leftX = playerX + hitboxOffsetX;
        float rightX = playerX + hitboxOffsetX + hitboxWidth;
        float bottomY = nextY + hitboxOffsetY;
        float topY = nextY + hitboxOffsetY + hitboxHeight;

        if (velocityY > 0) {
            if (isSolid(leftX + 4, topY) || isSolid(rightX - 4, topY)) {
                velocityY = 0;
            } else {
                playerY = nextY;
            }
        } else if (velocityY < 0) {
            if (isSolid(leftX + 4, bottomY) || isSolid(rightX - 4, bottomY)) {
                int tileY = (int) (bottomY / TILE_SIZE);
                playerY = (tileY + 1) * TILE_SIZE - hitboxOffsetY;
                velocityY = 0;
            } else {
                playerY = nextY;
            }
        }

        clampPlayerToMap();
    }

    private void clampPlayerToMap() {
        float mapWidth = collisionLayer.getWidth() * TILE_SIZE;
        float mapHeight = collisionLayer.getHeight() * TILE_SIZE;

        if (playerX < 0) {
            playerX = 0;
        }

        if (playerX > mapWidth - playerWidth) {
            playerX = mapWidth - playerWidth;
        }

        if (playerY < 0) {
            playerY = 0;
            velocityY = 0;
        }

        if (playerY > mapHeight - playerHeight) {
            playerY = mapHeight - playerHeight;
            velocityY = 0;
        }
    }

    private boolean isSolid(float x, float y) {
        int tileX = (int) (x / TILE_SIZE);
        int tileY = (int) (y / TILE_SIZE);

        if (tileX < 0 || tileY < 0 ||
            tileX >= collisionLayer.getWidth() ||
            tileY >= collisionLayer.getHeight()) {
            return false;
        }

        TiledMapTileLayer.Cell cell = collisionLayer.getCell(tileX, tileY);
        return cell != null && cell.getTile() != null;
    }

    private void checkTreasureCollection() {
        Rectangle playerBounds = new Rectangle(playerX, playerY, playerWidth, playerHeight);

        for (Treasure treasure : treasures) {
            if (!treasure.collected && playerBounds.overlaps(treasure.getBounds())) {
                treasure.collected = true;
                score += 10;
                collectedTreasures++;
            }
        }

        boolean allCollected = true;
        for (Treasure treasure : treasures) {
            if (!treasure.collected) {
                allCollected = false;
                break;
            }
        }

        if (allCollected) {
            win = true;
        }
    }

    private void checkBubbleCollection() {
        Rectangle playerBounds = new Rectangle(playerX, playerY, playerWidth, playerHeight);

        for (Treasure bubble : bubbles) {
            if (playerBounds.overlaps(bubble.getBounds())) {
                oxygen += 20;

                if (oxygen > maxOxygen) {
                    oxygen = maxOxygen;
                }

                resetBubbleToPlant(bubble);
            }
        }
    }

    private void updateOxygen(float delta) {
        if (gameOver) return;

        oxygen -= oxygenDrainRate * delta;

        if (oxygen <= 0) {
            oxygen = 0;
            gameOver = true;
        }
    }

    private void updateBubbles(float delta) {
        for (Treasure bubble : bubbles) {
            if (!bubble.collected) {
                bubble.y += 25 * delta;

                if (bubble.y > camera.position.y + 400) {
                    resetBubbleToPlant(bubble);
                }
            }
        }
    }

    private void resetBubbleToPlant(Treasure bubble) {
        if (bubble.x < 700) {
            bubble.x = 365;
            bubble.y = 375;
        } else if (bubble.x < 1300) {
            bubble.x = 925;
            bubble.y = 120;
        } else if (bubble.x < 2500) {
            bubble.x = 1885;
            bubble.y = 120;
        } else if (bubble.x < 5000) {
            bubble.x = 4225;
            bubble.y = 120;
        } else {
            bubble.x = 5595;
            bubble.y = 185;
        }
    }

    private void updateMines(float delta) {
        for (Mine mine : mines) {
            if (mine.exploded) continue;

            float minY = Math.min(mine.startY, mine.endY);
            float maxY = Math.max(mine.startY, mine.endY);

            if (mine.movingUp) {
                mine.y += mine.speed * delta;

                if (mine.y >= maxY) {
                    mine.y = maxY;
                    mine.movingUp = false;
                }
            } else {
                mine.y -= mine.speed * delta;

                if (mine.y <= minY) {
                    mine.y = minY;
                    mine.movingUp = true;
                }
            }
        }
    }

    private void checkMineProximity() {
        float playerCenterX = playerX + playerWidth / 2;
        float playerCenterY = playerY + playerHeight / 2;

        for (Mine mine : mines) {
            if (mine.exploded) continue;

            float mineCenterX = mine.x + mine.width / 2;
            float mineCenterY = mine.y + mine.height / 2;

            float dx = playerCenterX - mineCenterX;
            float dy = playerCenterY - mineCenterY;

            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < mine.proximityRadius) {
                mine.active = true;
            }
        }
    }

    private void updateMinesExplosion(float delta) {
        float playerCenterX = playerX + playerWidth / 2;
        float playerCenterY = playerY + playerHeight / 2;

        for (Mine mine : mines) {
            if (mine.exploded) {
                mine.explosionTimer += delta;

                if (mine.explosionTimer > 2f) {
                    mine.exploded = false;
                    mine.active = false;
                    mine.activationTime = 0f;
                    mine.explosionTimer = 0f;
                    mine.y = mine.startY;
                }

                continue;
            }

            if (mine.active) {
                mine.activationTime += delta;

                if (mine.activationTime > 1f) {
                    mine.exploded = true;
                    mine.explosionTimer = 0f;

                    float mineCenterX = mine.x + mine.width / 2;
                    float mineCenterY = mine.y + mine.height / 2;

                    float dx = playerCenterX - mineCenterX;
                    float dy = playerCenterY - mineCenterY;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);

                    if (distance < mine.explosionRadius) {
                        oxygen -= 30;

                        if (oxygen < 0) {
                            oxygen = 0;
                        }

                        damageTimer = damageDuration;
                        shakeTimer = shakeDuration;
                    }
                }
            }
        }
    }

    private void updateCamera() {
        float mapWidth = collisionLayer.getWidth() * TILE_SIZE;
        float mapHeight = collisionLayer.getHeight() * TILE_SIZE;

        camera.position.x = playerX + playerWidth / 2;
        camera.position.y = playerY + playerHeight / 2;

        if (camera.position.x < camera.viewportWidth / 2) {
            camera.position.x = camera.viewportWidth / 2;
        }

        if (camera.position.x > mapWidth - camera.viewportWidth / 2) {
            camera.position.x = mapWidth - camera.viewportWidth / 2;
        }

        if (camera.position.y < camera.viewportHeight / 2) {
            camera.position.y = camera.viewportHeight / 2;
        }

        if (camera.position.y > mapHeight - camera.viewportHeight / 2) {
            camera.position.y = mapHeight - camera.viewportHeight / 2;
        }
    }

    private void resetGame() {
        playerX = 120f;
        playerY = 180f;
        velocityY = 0f;

        score = 0;
        oxygen = maxOxygen;
        survivalTime = 0f;
        collectedTreasures = 0;

        gameOver = false;
        win = false;

        damageTimer = 0f;
        shakeTimer = 0f;

        for (Treasure treasure : treasures) {
            treasure.collected = false;
        }

        for (Treasure bubble : bubbles) {
            resetBubbleToPlant(bubble);
        }

        for (Mine mine : mines) {
            mine.active = false;
            mine.exploded = false;
            mine.activationTime = 0f;
            mine.explosionTimer = 0f;
            mine.y = mine.startY;
        }
    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        batch.dispose();

        fondo.dispose();
        plantTexture.dispose();
        treasureTexture.dispose();
        chestIcon.dispose();
        bubbleTexture.dispose();
        mineTexture.dispose();
        whitePixel.dispose();

        font.dispose();
    }
    private TextureRegion getCurrentPlayerFrame() {
        boolean pressingLeft = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean pressingRight = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean pressingUp = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean pressingDown = Gdx.input.isKeyPressed(Input.Keys.DOWN);

        if (pressingUp) {
            return upAnimation.getKeyFrame(stateTime, true);
        }

        if (pressingDown || velocityY < 0) {
            return downAnimation.getKeyFrame(stateTime, true);
        }

        if (pressingLeft || pressingRight) {
            return sideAnimation.getKeyFrame(stateTime, true);
        }

        return idleAnimation.getKeyFrame(stateTime, true);
    }
}
