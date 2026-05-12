package io.midnight_hills.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.*;
import io.midnight_hills.Main;

public class TitleScreen implements Screen {


    private SpriteBatch batch;
    private AssetManager assetManager;
    private Main game;
    private OrthographicCamera camera;
    private FitViewport port;
    private Texture background;
    private VfxManager vfxManager;
    private VignettingEffect vignettingEffect;
    private ChromaticAberrationEffect chromaticAberrationEffect;
    private BloomEffect bloomEffect;
    private LensFlareEffect lensFlareEffect;
    private FilmGrainEffect filmGrainEffect;
    private float time, bloomTimer, bloomMin = 0.05f, bloomMax = 0.1f, bloomSpeed = 0.3f, bloomAmount;
    private Interpolation easeIn = Interpolation.fastSlow;
    private Interpolation easeOut = Interpolation.fastSlow;

    public TitleScreen(SpriteBatch batch, AssetManager assetManager, Main game) {
        this.batch = batch;
        this.assetManager = assetManager;
        this.game = game;
        this.camera = new OrthographicCamera();

        background = assetManager.get("backgrounds/bg1.png", Texture.class);

        camera.update();
        port = new FitViewport(256, 144, camera);
        port.apply();
        vfxManager = new VfxManager(Pixmap.Format.RGBA8888, 256, 144);

        vignettingEffect = new VignettingEffect(false);
        vfxManager.addEffect(vignettingEffect);

        chromaticAberrationEffect = new ChromaticAberrationEffect(100);
        lensFlareEffect = new LensFlareEffect();
        lensFlareEffect.setIntensity(3f);
        vfxManager.addEffect(lensFlareEffect);
        vignettingEffect.setIntensity(0.7f);
//        vfxManager.addEffect(chromaticAberrationEffect);

        bloomEffect = new BloomEffect();
//        bloomEffect.setBaseIntensity(0.5f);
        vfxManager.addEffect(bloomEffect);

    }

    @Override
    public void show() {

    }

    public void update(float delta) {
        camera.update();
    }

    public void bloomPulse(float delta) {
        bloomTimer = (float) (0.5 + 0.5 * Math.sin(time * bloomSpeed));
        float dir = Math.signum((float) Math.cos(time * bloomSpeed));
        bloomAmount = dir == 1.0f ? easeIn.apply(bloomTimer) : easeOut.apply(bloomTimer);
        bloomAmount = bloomMin + (bloomAmount) * (bloomMax - bloomMin);
    }

    @Override
    public void render(float delta) {
        time += delta;

        bloomPulse(delta);
        update(delta);
        ScreenUtils.clear(0, 0, 0, 1, true);
        batch.setProjectionMatrix(camera.combined);
        vfxManager.cleanUpBuffers();
        vfxManager.beginInputCapture();
        batch.begin();
        batch.draw(background, 0, 0, 256, 144);
        batch.end();
        vfxManager.endInputCapture();


        bloomEffect.setThreshold(1 - bloomAmount);
        vfxManager.applyEffects();
        vfxManager.renderToScreen();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        port.update(width, height, true);

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
