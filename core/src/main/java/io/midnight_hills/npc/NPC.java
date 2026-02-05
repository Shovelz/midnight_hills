package io.midnight_hills.npc;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import io.midnight_hills.map.OrthogonalTiledMapRendererWithSprites;

import java.util.ArrayList;

public abstract class NPC {

    protected String name;
    protected Rectangle hitbox;
    protected TiledMap map;
    protected AssetManager assetManager;

    public NPC(String name, Rectangle hitbox, TiledMap map, AssetManager assetManager) {
        this.name = name;
        this.hitbox = hitbox;
        this.map = map;
        this.assetManager = assetManager;
    }


    public abstract ArrayList<Sprite> getShadows();
    public abstract ArrayList<Sprite> getOverlays();
    public abstract ArrayList<Sprite> getSprites();
    public abstract void update(float delta);
    public abstract void render(float delta);
    public abstract void clicked(float delta);

    public void registerSprites(OrthogonalTiledMapRendererWithSprites renderer) {
        for (Sprite s : getSprites()) renderer.addSprite(s);
        for (Sprite s : getOverlays()) renderer.addOverlap(s);
        for (Sprite s : getShadows()) renderer.addShadow(s);
    }
    public Rectangle getHitbox() {
        return hitbox;
    }
}
