package io.midnight_hills;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {

    private SpriteBatch batch;
    private AssetManager assetManager;
    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        setScreen(new GameScreen(batch, assetManager, this));
    }
}
