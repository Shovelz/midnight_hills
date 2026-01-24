package io.midnight_hills.map.rooms;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;


public class RoomContext {
    public final TiledMap map;
    public final String name;
    public final ArrayList<Door> doors;
    public final ArrayList<Rectangle> colliders;

    public RoomContext(String name, TiledMap map, ArrayList<Door> doors, ArrayList<Rectangle> colliders){
        this.name = name;
        this.map = map;
        this.doors = doors;
        this.colliders = colliders;
    }
}
