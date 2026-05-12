package io.midnight_hills.render;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;


public class SpriteRenderable extends Renderable{
    private Sprite sprite;
    private Rectangle hitbox;
    private String name;

    public SpriteRenderable(Sprite sprite, float depth, Rectangle hitbox, String name){
        this.sprite = sprite;
        this.depth = depth;
        this.hitbox = hitbox;
        this.name = name;
    }

    @Override
    protected void render(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public float getY(){
        return hitbox.y;
    }

    public String getName() {
        return name;
    }
}
