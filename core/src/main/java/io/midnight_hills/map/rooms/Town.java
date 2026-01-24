package io.midnight_hills.map.rooms;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Map;


public class Town extends Room {
    private Map<Vector2, TiledMap> loadedChunks;
    public Town(RoomContext ctx) {
        super(ctx.name, ctx.map, ctx.doors, ctx.colliders);
    }

    @Override
    public void update(float delta) {
//        loadChunksNearPlayer();
//        unloadFarChunks();
    }
}
