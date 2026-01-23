package io.midnight_hills.map.rooms;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import io.midnight_hills.Player;


public class House1 extends Room {

    public House1(Vector2 entry, String name, Player.Direction entryDirection, TiledMap map) {
        super(entry, name, entryDirection, map);
    }

    public House1(RoomContext ctx) {
        super(ctx.entry, ctx.name, ctx.entryDirection, ctx.map);
    }
}
