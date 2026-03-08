package io.midnight_hills.npc;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.*;
import java.util.function.Function;

public class NPCFactory {

    private final AssetManager assetManager;
    private final Map<String, Function<NPCContext, NPC>> registry = new HashMap<>();

    public NPCFactory(AssetManager assetManager) {
        this.assetManager = assetManager;

        register("Rock", Rock::new);
        register("SpiderLady", SpiderLady::new);
    }

    private void register(String id, Function<NPCContext, NPC> constructor) {
        registry.put(id, constructor);
    }

    public NPC create(MapObject object, TiledMap map) {

        MapProperties props = object.getProperties();
        String npcId = props.get("name", String.class);

        Rectangle hitbox = ((RectangleMapObject) object).getRectangle();

        Function<NPCContext, NPC> ctor = registry.get(npcId);
        if (ctor == null) {
            throw new RuntimeException("Unknown npc id: " + npcId);
        }


        NPCContext ctx = new NPCContext(npcId, map, new Vector2(hitbox.x, hitbox.y), assetManager);

        return ctor.apply(ctx);

    }
}
