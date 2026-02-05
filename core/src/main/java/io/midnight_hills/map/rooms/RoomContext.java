package io.midnight_hills.map.rooms;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import io.midnight_hills.npc.NPC;

import java.util.ArrayList;


public class RoomContext {
    public final TiledMap map;
    public final String name;
    public final ArrayList<Door> doors;
    public final ArrayList<Rectangle> colliders;
    public final ArrayList<NPC> npcs;

    public RoomContext(String name, TiledMap map, ArrayList<Door> doors, ArrayList<Rectangle> colliders, ArrayList<NPC> npcs){
        this.name = name;
        this.map = map;
        this.doors = doors;
        this.colliders = colliders;
        this.npcs = npcs;
    }
}
