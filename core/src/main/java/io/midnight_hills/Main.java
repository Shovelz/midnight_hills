package io.midnight_hills;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import io.midnight_hills.screens.GameScreen;
import io.midnight_hills.screens.TitleScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {

    private SpriteBatch batch;
    private AssetManager assetManager;
    private GameScreen gameScreen;
    private TitleScreen titleScreen;
    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        FileHandleResolver resolver = new InternalFileHandleResolver();

        assetManager.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(resolver));
        assetManager.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(resolver));

        assetManager.load("backgrounds/bg1.png", Texture.class);
        assetManager.load("ui/chatbox.png", Texture.class);

        FreetypeFontLoader.FreeTypeFontLoaderParameter size1Params = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        size1Params.fontFileName = "fonts/prstartk.ttf";
        size1Params.fontParameters.size = 48;
        size1Params.fontParameters.magFilter = Texture.TextureFilter.Nearest;
        size1Params.fontParameters.minFilter = Texture.TextureFilter.Nearest;
        size1Params.fontParameters.genMipMaps = false;
        size1Params.fontParameters.color = Color.BLACK;
        assetManager.load("pixelFont1.ttf", BitmapFont.class, size1Params);
        assetManager.finishLoading();

        gameScreen = new GameScreen(batch, assetManager, this);
        titleScreen = new TitleScreen(batch, assetManager, this);
//        setScreen(titleScreen);
        setScreen(gameScreen);
    }
}
