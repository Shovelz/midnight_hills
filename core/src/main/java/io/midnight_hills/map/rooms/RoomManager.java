package io.midnight_hills.map.rooms;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import io.midnight_hills.Player;
import io.midnight_hills.map.OrthogonalTiledMapRendererWithSprites;
import org.lwjgl.Sys;

import java.util.HashMap;

public class RoomManager {

    private HashMap<String, Room> rooms;
    private TiledMap map;
    private OrthogonalTiledMapRendererWithSprites mapRenderer;
    private Room currentRoom;
    private Player player;
    private SpriteBatch batch;

    public RoomManager(Player player, SpriteBatch batch) {
        rooms = new HashMap<>();
        this.player = player;
        this.batch = batch;
    }

    public void init() {
        System.out.println(rooms);
        currentRoom = rooms.get("Town");
        player.teleport(new Vector2(100, 100));
        player.setCollisionRects(currentRoom.getColliders());

        map = currentRoom.getMap();
        mapRenderer = new OrthogonalTiledMapRendererWithSprites(map, batch);
        mapRenderer.addSprite(player.getSprite());
    }

    public void add(String id, Room room) {
        rooms.put(id, room);
    }

    public Room get(String id) {
        return rooms.get(id);
    }

    public void update(float delta) {

        for (Door door : currentRoom.getDoors()) {
            if (door.getHitbox().overlaps(player.getHitbox())) {
                enterRoom(door);
            }
        }
    }

    public void render(SpriteBatch batch, float delta, OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    public void enterRoom(Door door) {
        if (currentRoom != null) {
            currentRoom.onExit();
        }

        currentRoom = rooms.get(door.getDestination());
        player.teleport(door.getEntryLocation());
        player.setCollisionRects(currentRoom.getColliders());

        map = currentRoom.getMap();
        mapRenderer = new OrthogonalTiledMapRendererWithSprites(map, batch);
        mapRenderer.addSprite(player.getSprite());

        currentRoom.onEnter();
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }
}
