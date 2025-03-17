package com.project;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import java.util.Iterator;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture dropTexture;
    private Texture backgroundTexture;
    private Texture collectorTexture; // Nueva textura para el recolector
    private Array<Drop> drops;
    private long lastDropTime;
    private long lastDifficultyIncreaseTime;
    private float dropSpeedMultiplier = 1f;
    private long spawnInterval = 5_000_000_000L;
    private Collector collector;
    private int score = 0;
    private BitmapFont font;

    private boolean isDragging = false; // Indica si el recolector está siendo arrastrado

    @Override
    public void create() {
        batch = new SpriteBatch();
        dropTexture = new Texture("drop.png");
        backgroundTexture = new Texture("fondo.jpg");
        collectorTexture = new Texture("cubo.png"); // Cargar la textura del recolector

        drops = new Array<>();
        spawnDrop();
        lastDifficultyIncreaseTime = TimeUtils.nanoTime();

        // Crear el recolector en el centro
        collector = new Collector(260, 50, 130, 130);

        // Inicializar fuente para puntaje
        font = new BitmapFont();
        font.setColor(Color.BLACK);
    }

    private void spawnDrop() {
        float width = (float) (20 + Math.random() * 40);
        float spawnHorizontal = (float) (Math.random() * (720 - width));
        float height = width;
        float speed = 2 * dropSpeedMultiplier;
        drops.add(new Drop(spawnHorizontal, 800, speed, width, height));
        lastDropTime = TimeUtils.nanoTime();
    }

    private void incrementDifficulty() {
        if (TimeUtils.nanoTime() - lastDifficultyIncreaseTime > 10_000_000_000L) {
            dropSpeedMultiplier += 0.08f;
            spawnInterval = Math.max(300_000_000L, spawnInterval - 350_000_000L);
            lastDifficultyIncreaseTime = TimeUtils.nanoTime();
            System.out.println("Dificultad aumentada: Velocidad = " + dropSpeedMultiplier + ", Spawn Interval = " + spawnInterval);
        }
    }

    private void handleInput() {
        if (Gdx.input.isTouched()) {
            float touchX = Gdx.input.getX();

            if (!isDragging) {
                // Iniciar arrastre si el usuario tocó el recolector
                if (collector.contains(touchX, Gdx.graphics.getHeight() - Gdx.input.getY())) {
                    isDragging = true;
                }
            }

            // Si está arrastrando, mover el recolector solo en X
            if (isDragging) {
                collector.setX(Math.max(-25, Math.min(746 - collector.getWidth(), touchX - collector.getWidth() / 2)));
            }
        } else {
            // Soltar el recolector cuando el usuario deja de tocar
            isDragging = false;
        }
    }

    @Override
    public void render() {
        handleInput(); // Manejar el movimiento del recolector

        ScreenUtils.clear(0f, 0f, 0f, 0.5f); // Fondo negro con algo de opacidad

        if (TimeUtils.nanoTime() - lastDropTime > spawnInterval) {
            spawnDrop();
        }

        incrementDifficulty();

        batch.begin();
        batch.draw(backgroundTexture, 0, 0, 720, 870); // Fondo de la escena
        // Usar la textura del recolector en lugar de la capa blanca semitransparente
        batch.draw(collectorTexture, collector.getX(), collector.getY(), collector.getWidth(), collector.getHeight());

        // Dibujar las gotas
        Iterator<Drop> iter = drops.iterator();
        while (iter.hasNext()) {
            Drop drop = iter.next();
            drop.update();
            batch.draw(dropTexture, drop.getX(), drop.getY(), drop.getWidth(), drop.getHeight());

            // Verificar colisión con el recolector
            if (drop.collidesWith(collector)) {
                score++;  
                iter.remove(); 
            } else if (drop.getY() < 0) {
                iter.remove();
            }
        }

        // Dibujar el puntaje dentro del recolector
        GlyphLayout layout = new GlyphLayout(font, String.valueOf(score));
        font.draw(batch, layout, collector.getX() + collector.getWidth() / 2 - layout.width / 2,
                  collector.getY() + collector.getHeight() / 2 + layout.height / 2);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        dropTexture.dispose();
        backgroundTexture.dispose();
        collectorTexture.dispose(); // Asegúrate de liberar la textura del recolector
        font.dispose();
    }
}
