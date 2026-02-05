package io.midnight_hills.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.DistanceFieldFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;


public class ChatBox {

    private Rectangle pos;
    private DistanceFieldFont font;
    private Texture texture;

    public ChatBox(DistanceFieldFont font, AssetManager assetManager){
        this.font = font;
        this.texture = assetManager.get("ui/chatbox.png", Texture.class);

    }

    public void handleInput(float delta){
//        next();
    }

    public void render(SpriteBatch batch, float delta){

    }
}
