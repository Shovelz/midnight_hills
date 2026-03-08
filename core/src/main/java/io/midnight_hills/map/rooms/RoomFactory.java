package io.midnight_hills.map.rooms;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.midnight_hills.player.Player;
import io.midnight_hills.npc.NPC;
import io.midnight_hills.npc.NPCFactory;
import org.lwjgl.Sys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RoomFactory {

    private final AssetManager assetManager;
    private final Map<String, Function<RoomContext, Room>> registry = new HashMap<>();
    private NPCFactory npcFactory;

    public RoomFactory(AssetManager assetManager, NPCFactory npcFactory) {
        this.assetManager = assetManager;
        this.npcFactory = npcFactory;

        register("House1", House1::new);
        register("House1_floor1", House1Floor1::new);
        register("Town", Town::new);
    }

    private void register(String id, Function<RoomContext, Room> constructor) {
        registry.put(id, constructor);
    }

    public Room create(String roomId) {

        TiledMap map = new TmxMapLoader().load("map/rooms/" + roomId + ".tmx");

        ArrayList<Door> doors = new ArrayList<>();
        for (MapObject object : map.getLayers().get("Doors").getObjects()) {
            MapProperties props = object.getProperties();

            int direction_integer = props.get("facing", Integer.class);
            if(direction_integer < 0 || direction_integer > 3){
                direction_integer = 0;
            }
            Player.Direction direction = Arrays.stream(Player.Direction.values()).toList().get(direction_integer);
            System.out.println("Door direction: " + direction);
            Vector2 entry = new Vector2(props.get("x", Integer.class), props.get("y", Integer.class));
            Rectangle hitbox = ((RectangleMapObject) object).getRectangle();

            Door door = new Door(direction, hitbox, roomId, props.get("room", String.class), entry);
            doors.add(door);
        }

        ArrayList<Rectangle> colliders = new ArrayList<>();

        for (MapObject collider : map.getLayers().get("Collisions").getObjects()) {
            Rectangle rect = ((RectangleMapObject) collider).getRectangle();
            colliders.add(rect);
        }

        ArrayList<NPC> npcs = new ArrayList<>();
        for (MapObject npc : map.getLayers().get("Npcs").getObjects()) {
            npcs.add(npcFactory.create(npc, map));
        }

        Function<RoomContext, Room> ctor = registry.get(roomId);
        if (ctor == null) {
            throw new RuntimeException("Unknown room id: " + roomId);
        }

        RoomContext ctx = new RoomContext(
            roomId, map, doors, colliders, npcs
        );

        return ctor.apply(ctx);

    }
}
