package io.midnight_hills.map.rooms;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.midnight_hills.FrameBufferManager;
import io.midnight_hills.player.Player;
import io.midnight_hills.npc.NPC;
import io.midnight_hills.render.FuckenMapLayer;
import io.midnight_hills.render.FuckenMapRenderer;
import io.midnight_hills.render.Renderable;

import java.util.ArrayList;
import java.util.HashMap;

public class RoomManager {

    private HashMap<String, Room> rooms;
    private TiledMap map;
    private FuckenMapRenderer mapRenderer;
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
    private float time = 0f;
    private Camera camera;
    private RayHandler lights;
    private FrameBufferManager frameBufferManager;

    public RoomManager(Player player, SpriteBatch batch, Camera camera, RayHandler lights, FrameBufferManager frameBufferManager) {
        rooms = new HashMap<>();
        this.player = player;
        this.batch = batch;
        this.camera = camera;
        this.lights = lights;
        this.frameBufferManager = frameBufferManager;
    }

    public void init() {

        currentRoom = rooms.get("Town");
        mapRenderer = new FuckenMapRenderer(batch, camera, player, lights, frameBufferManager);


        player.teleport(new Vector2(100, 200));
        camera.position.x = player.getHitbox().x - player.getHitbox().getWidth() / 2.0f;
        camera.position.y = player.getHitbox().y - player.getHitbox().getHeight() / 2.0f;

        setupRoom(currentRoom);
    }

    public void add(String id, Room room) {
        rooms.put(id, room);
    }

    public Room get(String id) {
        return rooms.get(id);
    }

    public void update(float delta) {

        currentRoom.update(delta);
        if (transitionState != TransitionState.NONE) {
            player.lockInput();
            updateTransition(delta);
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
                    player.unlockInput();
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
        mapRenderer.render(delta);
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

    private void setupRoom(Room room) {

        ArrayList<Rectangle> collisionRects = new ArrayList<>(room.getColliders());
        for (NPC npc : room.getNpcs()) {
            collisionRects.add(npc.getHitbox());
        }

        player.setCollisionRects(collisionRects);

        map = room.getMap();
        mapRenderer.clearRenderables();
        for (MapLayer layer : map.getLayers()) {
            String type = Renderable.shaderTypes.contains(layer.getName()) ? layer.getName() : Renderable.shaderTypes.get(0);
            float depth = 0f;
            if (layer.getProperties().containsKey("depth")) {
                depth = (float) layer.getProperties().get("depth");
            }
//            TiledMapTileLayer tiledLayer = (TiledMapTileLayer) layer;
//            for (int x = 0; x < tiledLayer.getWidth(); x++) {
//                for (int y = 0; y < tiledLayer.getHeight(); y++) {
//                    TiledMapTileLayer.Cell cell = tiledLayer.getCell(x, y);
//                    if(cell != null){
//                        mapRenderer.getRenderables().add(new TileRenderable(x, y, depth ))
//                    }
//                }
//            }
            mapRenderer.addRenderable(new FuckenMapLayer(camera, depth, layer, type));
        }

//        mapRenderer.addRayHandler();

        player.registerSprites(mapRenderer);
        for (NPC npc : room.getNpcs()) {
            npc.registerSprites(mapRenderer);
        }


        mapRenderer.getRenderables().sort(
            (a, b) -> Float.compare(b.depth, a.depth)
        );
        for (Renderable renderable : mapRenderer.getRenderables()) {
            System.out.println(renderable + ": " + renderable.depth);
        }

    }


    private void switchRoom() {
        if (currentRoom != null) {
            currentRoom.onExit();
        }

        currentRoom = rooms.get(pendingDoor.getDestination());

        player.teleport(pendingDoor.getEntryLocation());
        player.faceDirection(pendingDoor.getEntryDirection());
        camera.position.x = player.getHitbox().x - player.getHitbox().getWidth() / 2.0f;
        camera.position.y = player.getHitbox().y - player.getHitbox().getHeight() / 2.0f;
        setupRoom(currentRoom);

        currentRoom.onEnter();
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

}
