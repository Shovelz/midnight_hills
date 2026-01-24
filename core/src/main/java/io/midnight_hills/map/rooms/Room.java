package io.midnight_hills.map.rooms;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;

public abstract class Room {

    protected String name;
    protected ArrayList<Rectangle> colliders;
    protected TiledMap map;
    protected ArrayList<Door> doors;

    public Room(String name, TiledMap map, ArrayList<Door> doors, ArrayList<Rectangle> colliders){
        this.name = name;
        this.map = map;
        this.doors = doors;
        this.colliders = colliders;
    }

    public ArrayList<Rectangle> getColliders(){
        return colliders;
    }

    public String getName() {
        return name;
    }

    public TiledMap getMap() {
        return map;
    }

    public void addDoor(Door door){
        doors.add(door);
    }

    public ArrayList<Door> getDoors(){
        return doors;
    }

    public void onExit() {
    }

    public void onEnter() {

    }

    public abstract void update(float delta);
}
