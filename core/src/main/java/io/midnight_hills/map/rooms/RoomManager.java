package io.midnight_hills.map.rooms;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.midnight_hills.Player;
import io.midnight_hills.map.OrthogonalTiledMapRendererWithSprites;

import java.util.HashMap;

public class RoomManager {

    private HashMap<String, Room> rooms;
    private TiledMap map;
    private OrthogonalTiledMapRendererWithSprites mapRenderer;
    private Room currentRoom;
    private Player player;
    private SpriteBatch batch;

    private enum TransitionState {
        NONE,
        FADE_OUT,
        SWITCH,
        FADE_IN
    }

    private TransitionState transitionState = TransitionState.NONE;
    private float transitionTime = 0f;
    private float transitionDuration = 0.4f;
    private ShapeRenderer fadeRenderer = new ShapeRenderer();

    private Door pendingDoor;

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

        if (transitionState != TransitionState.NONE) {
            updateTransition(delta);
            player.lockInput();
            player.unlockInput();
            return;
        }
        for (Door door : currentRoom.getDoors()) {
            if (door.getHitbox().overlaps(player.getHitbox())) {
                beginTransition(door);
                break;
            }
        }
    }

    private void updateTransition(float delta) {
        transitionTime += delta;

        switch (transitionState) {
            case FADE_OUT:
                if (transitionTime >= transitionDuration) {
                    switchRoom();
                    transitionState = TransitionState.FADE_IN;
                    transitionTime = 0f;
                }
                break;

            case FADE_IN:
                if (transitionTime >= transitionDuration) {
                    transitionState = TransitionState.NONE;
                }
                break;
        }
    }

    private void beginTransition(Door door) {
        if (transitionState != TransitionState.NONE) return;

        pendingDoor = door;
        transitionTime = 0f;
        transitionState = TransitionState.FADE_OUT;
    }


    public void render(SpriteBatch batch, float delta, OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.render();
    }

    public void renderFade(OrthographicCamera camera) {
        float alpha = getFadeToBlackAlpha();
        if (alpha <= 0f) return;

        fadeRenderer.setProjectionMatrix(camera.combined);
        fadeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        fadeRenderer.setColor(0f, 0f, 0f, alpha);
        fadeRenderer.rect(
            camera.position.x - camera.viewportWidth / 2f,
            camera.position.y - camera.viewportHeight / 2f,
            camera.viewportWidth,
            camera.viewportHeight
        );
        fadeRenderer.end();
    }

    private float getFadeToBlackAlpha() {
        if (transitionState == TransitionState.NONE) return 0f;

        float t = transitionTime / transitionDuration;

        if (transitionState == TransitionState.FADE_OUT) {
            return MathUtils.clamp(t, 0f, 1f);
        }

        if (transitionState == TransitionState.FADE_IN) {
            return MathUtils.clamp(1f - t, 0f, 1f);
        }

        return 0f;
    }


    private void switchRoom() {
        if (currentRoom != null) {
            currentRoom.onExit();
        }

        currentRoom = rooms.get(pendingDoor.getDestination());

        player.teleport(pendingDoor.getEntryLocation());
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
