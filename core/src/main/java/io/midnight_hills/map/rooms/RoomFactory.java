package io.midnight_hills.map.rooms;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Vector2;
import com.sun.source.tree.CompoundAssignmentTree;
import io.midnight_hills.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RoomFactory {

    private final AssetManager assetManager;
    private final Map<String, Function<RoomContext, Room>> registry = new HashMap<>();

    public RoomFactory(AssetManager assetManager) {
        this.assetManager = assetManager;

        register("House1", House1::new);
    }

    private void register(String id, Function<RoomContext, Room> constructor) {
        registry.put(id, constructor);
    }

    public Room create(MapProperties props) {

        String roomId = props.get("room", String.class);
        TiledMap map = new TmxMapLoader().load("map/" + roomId + ".tmx");
        Player.Direction direction = Arrays.stream(Player.Direction.values()).toList().get(props.get("facing", Integer.class));
        Vector2 entry = new Vector2(props.get("x", Integer.class), props.get("y", Integer.class));

        Function<RoomContext, Room> ctor = registry.get(roomId);
        if (ctor == null) {
            throw new RuntimeException("Unknown room id: " + roomId);
        }

        RoomContext ctx = new RoomContext(
            entry, roomId, direction, map
        );

        return ctor.apply(ctx);

    }
}
