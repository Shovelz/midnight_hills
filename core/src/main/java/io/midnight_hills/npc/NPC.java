package io.midnight_hills.npc;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import io.midnight_hills.dialogue.DialogueManager;
import io.midnight_hills.render.FuckenMapRenderer;
import io.midnight_hills.render.SpriteRenderable;

import java.util.ArrayList;

public abstract class NPC {

    protected String name;
    protected Rectangle hitbox;
    protected TiledMap map;
    protected AssetManager assetManager;
    protected DialogueManager dialogueManager;

    public NPC(String name, Rectangle hitbox, TiledMap map, AssetManager assetManager, DialogueManager dialogueManager) {
        this.name = name;
        this.hitbox = hitbox;
        this.map = map;
        this.assetManager = assetManager;
        this.dialogueManager = dialogueManager;
    }

    public abstract ArrayList<SpriteRenderable> getShadows();
    public abstract ArrayList<SpriteRenderable> getOverlays();
    public abstract ArrayList<SpriteRenderable> getSprites();
    public abstract void update(float delta);
    public abstract void render(float delta);
    public abstract void clicked(float delta);

    public void registerSprites(FuckenMapRenderer renderer) {
        for (SpriteRenderable s : getSprites()) renderer.addRenderable(s);
        for (SpriteRenderable o : getOverlays()) renderer.addRenderable(o);
        for (SpriteRenderable sh : getShadows()) renderer.addRenderable(sh);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }
}
