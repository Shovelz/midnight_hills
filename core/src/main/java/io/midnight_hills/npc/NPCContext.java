package io.midnight_hills.npc;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import io.midnight_hills.dialogue.DialogueManager;

public class NPCContext {
    public final TiledMap map;
    public final String name;
    public final Vector2 pos;
    public final AssetManager assetManager;
    public final DialogueManager dialogueManager;

    public NPCContext(String name, TiledMap map, Vector2 pos, AssetManager am, DialogueManager dm){
        this.name = name;
        this.map = map;
        this.pos = pos;
        this.assetManager = am;
        this.dialogueManager = dm;
    }
}
