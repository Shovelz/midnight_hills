package io.midnight_hills.map.rooms;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.midnight_hills.Player;
import java.util.ArrayList;

public abstract class Room {

    protected Vector2 entry;
    protected String name;
    protected Player.Direction entryDirection;
    protected ArrayList<Rectangle> colliders;
    protected TiledMap map;

    public Room(Vector2 entry, String name, Player.Direction entryDirection, TiledMap map){
        this.entry = entry;
        this.name = name;
        this.entryDirection = entryDirection;
        this.map = map;
    }

    public ArrayList<Rectangle> getColliders(){
        return colliders;
    }

    public Player.Direction getEntryDirection() {
        return entryDirection;
    }

    public String getName() {
        return name;
    }

    public Vector2 getEntry() {
        return entry;
    }

    public TiledMap getMap() {
        return map;
    }
}
