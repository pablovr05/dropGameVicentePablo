package com.project;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.audio.Sound;

import java.util.Iterator;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture dropTexture;
    private Texture backgroundTexture;
    private Texture collectorTexture;
    private Array<Drop> drops;
    private long lastDropTime;
    private long lastDifficultyIncreaseTime;
    private float dropSpeedMultiplier = 1f;
    private long spawnInterval = 5_000_000_000L;
    private Collector collector;
    private int score = 0;
    private int lives = 3;
    private boolean gameOver = false;
    private Sound failSound;
    private Sound bubblesSound;
    private Texture gameOverBackground;


    private BitmapFont font;
    private GlyphLayout layout;

    private boolean isDragging = false;

    private OrthographicCamera camera;
    private Viewport viewport;

    private final float VIRTUAL_WIDTH = 480;
    private final float VIRTUAL_HEIGHT = 800;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply();
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();

        failSound = Gdx.audio.newSound(Gdx.files.internal("musicafallo.mp3"));
        bubblesSound = Gdx.audio.newSound(Gdx.files.internal("bubbles.mp3"));

        batch = new SpriteBatch();
        dropTexture = new Texture("drop.png");
        backgroundTexture = new Texture("fondo.jpg");
        collectorTexture = new Texture("cubo.png");
        gameOverBackground = new Texture("game_over_background.jpg");


        drops = new Array<>();
        spawnDrop();
        lastDifficultyIncreaseTime = TimeUtils.nanoTime();

        collector = new Collector(VIRTUAL_WIDTH / 2 - 65, 50, 130, 130);

        font = new BitmapFont();
        font.setColor(Color.BLACK);
        font.getData().setScale(2f);
        layout = new GlyphLayout();
    }

    private void resetGame() {
        drops.clear();
        spawnDrop();
        score = 0;
        lives = 3;
        dropSpeedMultiplier = 1f;
        spawnInterval = 5_000_000_000L;
        lastDifficultyIncreaseTime = TimeUtils.nanoTime();
        gameOver = false;
    }

    private void spawnDrop() {
        float width = (float) (20 + Math.random() * 40);
        float spawnHorizontal = (float) (Math.random() * (VIRTUAL_WIDTH - width));
        float height = width;
        float speed = 2 * dropSpeedMultiplier;
        drops.add(new Drop(spawnHorizontal, VIRTUAL_HEIGHT, speed, width, height));
        lastDropTime = TimeUtils.nanoTime();
    }

    private void incrementDifficulty() {
        if (TimeUtils.nanoTime() - lastDifficultyIncreaseTime > 2_500_000_000L) {
            dropSpeedMultiplier += 0.08f;
            spawnInterval = Math.max(300_000_000L, spawnInterval - 350_000_000L);
            lastDifficultyIncreaseTime = TimeUtils.nanoTime();
        }
    }

    private void handleInput() {
        if (gameOver) {
            if (Gdx.input.justTouched()) {
                resetGame();
            }
            return;
        }

        if (Gdx.input.isTouched()) {
            Vector3 worldCoords = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (!isDragging) {
                if (collector.contains(worldCoords.x, worldCoords.y)) {
                    isDragging = true;
                }
            }

            if (isDragging) {
                float newX = Math.max(0, Math.min(VIRTUAL_WIDTH - collector.getWidth(), worldCoords.x - collector.getWidth() / 2));
                collector.setX(newX);
            }
        } else {
            isDragging = false;
        }
    }

    @Override
    public void render() {
        handleInput();
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        if (gameOver) {
            batch.draw(gameOverBackground, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        } else {
            batch.draw(backgroundTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        }
        

        if (!gameOver) {
            if (TimeUtils.nanoTime() - lastDropTime > spawnInterval) {
                spawnDrop();
            }

            incrementDifficulty();

            batch.draw(collectorTexture, collector.getX(), collector.getY(), collector.getWidth(), collector.getHeight());

            Iterator<Drop> iter = drops.iterator();
            while (iter.hasNext()) {
                Drop drop = iter.next();
                drop.update();
                batch.draw(dropTexture, drop.getX(), drop.getY(), drop.getWidth(), drop.getHeight());

                if (drop.collidesWith(collector)) {
                    score++;
                    iter.remove();
                    bubblesSound.play();
                } else if (drop.getY() < 0) {
                    lives--;
                    iter.remove();
                    failSound.play();
                    if (lives <= 0) {
                        gameOver = true;
                        break;
                    }
                }
            }

            layout.setText(font, String.valueOf(score));
            font.draw(batch, layout, collector.getX() + collector.getWidth() / 2 - layout.width / 2,
                collector.getY() + collector.getHeight() / 2 + layout.height / 2);

            font.setColor(Color.WHITE);
            font.draw(batch, "Vidas: " + lives, 20, VIRTUAL_HEIGHT - 20);
            font.setColor(Color.BLACK);
        } else {
            font.setColor(Color.WHITE);
            layout.setText(font, "¡Has perdido!");
            font.draw(batch, layout, VIRTUAL_WIDTH / 2 - layout.width / 2, VIRTUAL_HEIGHT / 2 + 40);

            layout.setText(font, "Toca para volver a jugar");
            font.draw(batch, layout, VIRTUAL_WIDTH / 2 - layout.width / 2, VIRTUAL_HEIGHT / 2 - 10);
            font.setColor(Color.BLACK);
        }

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        dropTexture.dispose();
        backgroundTexture.dispose();
        collectorTexture.dispose();
        font.dispose();
        gameOverBackground.dispose();
        failSound.dispose();
    }
}
