package io.midnight_hills.map.rooms;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import io.midnight_hills.Player;


public class RoomContext {
    public final TiledMap map;
    protected Vector2 entry;
    protected String name;
    protected Player.Direction entryDirection;

    public RoomContext(Vector2 entry, String name, Player.Direction entryDirection, TiledMap map){
        this.entry = entry;
        this.name = name;
        this.entryDirection = entryDirection;
        this.map = map;
    }
}
