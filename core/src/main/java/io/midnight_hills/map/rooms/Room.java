package io.midnight_hills.map.rooms;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import io.midnight_hills.npc.NPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Room {

    protected String name;
    protected ArrayList<Rectangle> colliders;
    protected TiledMap map;
    protected ArrayList<Door> doors;
    protected ArrayList<NPC> npcs;
    protected ArrayList<AnimatedTiledMapTile> animatedTiles;

    public Room(String name, TiledMap map, ArrayList<Door> doors, ArrayList<Rectangle> colliders, ArrayList<NPC> npcs) {
        this.name = name;
        this.map = map;
        this.doors = doors;
        this.colliders = colliders;
        this.npcs = npcs;
        this.animatedTiles = new ArrayList<>();

        loadAnimatedTiles();
    }

    public ArrayList<NPC> getNpcs() {
        return npcs;
    }

    protected void loadAnimatedTiles() {
        Map<String, Array<StaticTiledMapTile>> frameTiles = new HashMap<>();

        for(TiledMapTileSet tileset : map.getTileSets()) {
            for (TiledMapTile tile : tileset) {
                if (tile.getProperties().containsKey("animation")) {
                    String animationName = tile.getProperties().get("animation", String.class);
                    if (!frameTiles.containsKey(animationName)) {
                        Array<StaticTiledMapTile> animationTilesSameName = new Array<>();
                        frameTiles.put(animationName, animationTilesSameName);
                    }
                    frameTiles.get(animationName).add((StaticTiledMapTile) tile);
                }
            }
        }


        //These look expensive to setup I think? So I guess I'll generate them here instead of the loop
        Map<String, AnimatedTiledMapTile> animatedTiles = new HashMap<>();

        for (Map.Entry<String, Array<StaticTiledMapTile>> entry : frameTiles.entrySet()) {
            animatedTiles.put(entry.getKey(), new AnimatedTiledMapTile(0.4f, entry.getValue()));
        }


        for (MapLayer mapLayer : map.getLayers().getByType(TiledMapTileLayer.class)) {
            TiledMapTileLayer layer = ((TiledMapTileLayer) mapLayer);
            for (int x = 0; x < layer.getWidth(); x++) {
                for (int y = 0; y < layer.getHeight(); y++) {
                    TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                    if (cell == null) {
                        break;
                    }
                    MapProperties properties = cell.getTile().getProperties();
                    if (properties.containsKey("animation")) {
                        String animationName = properties.get("animation", String.class);
                        //if equals animation pull animation from map
                        cell.setTile(animatedTiles.get(animationName));
                    }
                }
            }

        }

    }

    public ArrayList<AnimatedTiledMapTile> getAnimatedTiles() {
        return animatedTiles;
    }

    public ArrayList<Rectangle> getColliders() {
        return colliders;
    }

    public String getName() {
        return name;
    }

    public TiledMap getMap() {
        return map;
    }

    public void addDoor(Door door) {
        doors.add(door);
    }

    public ArrayList<Door> getDoors() {
        return doors;
    }

    public void onExit() {

    }

    public void onEnter() {

    }

    public abstract void update(float delta);
}
