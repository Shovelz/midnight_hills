package io.midnight_hills;

import box2dLight.ConeLight;
import box2dLight.DirectionalLight;
import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.midnight_hills.map.rooms.RoomFactory;
import io.midnight_hills.map.rooms.RoomManager;
import io.midnight_hills.npc.NPC;
import io.midnight_hills.npc.NPCFactory;
import io.midnight_hills.player.Player;
import io.midnight_hills.player.Torch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;


public class GameScreen implements Screen {

    private SpriteBatch batch;
    private AssetManager assetManager;
    private Main game;

    private OrthographicCamera camera;
    private FitViewport screenSpacePort;
    private FitViewport port;

    private Player player;
    public final int TILE_SIZE = 16; // pixels per tile
    int VIEWPORT_WIDTH = 256;  // in pixels
    int VIEWPORT_HEIGHT = 144; // in pixels
    private ArrayList<Rectangle> collisionRects = new ArrayList<>();

    private ShaderProgram shaderProgram;
    private Texture noiseTexture;

    private OrthographicCamera screenCamera;
    private float time = 0f;
    private BitmapFont font;

    private DirectionalLight sun;
    private float lightTime = 0f;

    private RoomManager roomManager;
    private RoomFactory roomFactory;
    private NPCFactory npcFactory;
    private Vector3 mousePosition;
    private TiledMapTile hoveredTile;

    private RayHandler sunRayHandler, lightsRayHandler;
    private World world;
    private float sunAngle = -90.1f;
    private Color sunColor, sunsetColor, midnightColor;
    private int lightingMin = 0, lightingMax = 1, sunLevelMin = -40, sunLevelMax = 40;
    private Torch torch;
    private float clockTime;

    private enum Meridiem {AM, PM}

    private Meridiem meridiem;
    private float previousLerp = 0f, lerp = 0f;

    public GameScreen(SpriteBatch batch, AssetManager assetManager, Main game) {

        this.batch = batch;
        this.assetManager = assetManager;
        this.game = game;
        this.camera = new OrthographicCamera();

        screenCamera = new OrthographicCamera();
        screenCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        screenCamera = new OrthographicCamera();
        screenSpacePort = new FitViewport(1920, 1080, screenCamera);
        screenSpacePort.apply();

        screenCamera.update();
        port = new FitViewport(256, 144, camera);
        camera.update();


        String vert = Gdx.files.internal("shaders/test.vertex.glsl").readString();
        String frag = Gdx.files.internal("shaders/test.fragment.glsl").readString();

        noiseTexture = new Texture("shaders/perlin.jpg");
        noiseTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        // Cloud shader (not currently in use)
        shaderProgram = new ShaderProgram(vert, frag);

        if (shaderProgram.isCompiled()) {
            System.out.println("Compiled Successfully");
            batch.setShader(shaderProgram);
        } else {
            throw new GdxRuntimeException(shaderProgram.getLog());
        }


        world = new World(Vector2.Zero, true);
        sunRayHandler = new RayHandler(world);
        sunRayHandler.setShadows(true);
        sunRayHandler.setCombinedMatrix(camera);
        sunRayHandler.setAmbientLight(0f, 0f, 0f, 1f);
        sunRayHandler.setBlurNum(8);

        lightsRayHandler = new RayHandler(world);
        lightsRayHandler.setShadows(true);
        lightsRayHandler.setCombinedMatrix(camera);
        lightsRayHandler.setAmbientLight(0f, 0f, 0f, 1f);
        lightsRayHandler.setBlurNum(8);

        sun = new DirectionalLight(sunRayHandler, 128, new Color(255, 255, 153, 0.3f), sunAngle);

        player = new Player(assetManager, camera, new Vector2(VIEWPORT_WIDTH / 2f, VIEWPORT_HEIGHT / 2f), world);

        Pixmap pixmap = new Pixmap(Gdx.files.internal("assets/ui/cursor.png"));
        int xHotspot = 31, yHotspot = 31;
        Cursor cursor = Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot);
        pixmap.dispose();
        Gdx.graphics.setCursor(cursor);

        roomManager = new RoomManager(player, batch);
        npcFactory = new NPCFactory(assetManager);
        roomFactory = new RoomFactory(assetManager, npcFactory);

        FileHandle dir = Gdx.files.internal("assets/map/rooms");
        FileHandle[] files = dir.list();

        for (FileHandle file : files) {
            roomManager.add(file.nameWithoutExtension(), roomFactory.create(file.nameWithoutExtension()));
        }

        roomManager.init();

        font = new BitmapFont();
        font.setColor(Color.WHITE);


        midnightColor = new Color().set(new Color(0.24f, 0.22f, 0.35f, 1.0f), 0.5f);
        sunsetColor = new Color().set(new Color(0.78f, 0.76f, 0.55f, 1.0f), 0.5f);
        sunColor = new Color();

        torch = new Torch(player, lightsRayHandler);
        meridiem = Meridiem.AM;
    }


    private void update(float delta) {

        float cameraDelta = Math.min(delta, 1f / 60f);
        float smoothness = 5f;
        float alpha = 1f - (float) Math.exp(-smoothness * cameraDelta);
//        float lerp = 5f * delta;
        float targetX = player.getHitbox().x + player.getHitbox().width / 2f;
        float targetY = player.getHitbox().y + player.getHitbox().height / 2f;

        camera.position.x += (targetX - camera.position.x) * alpha;
        camera.position.y += (targetY - camera.position.y) * alpha;

        // Clamp camera to map bounds
        float halfViewportWidth = port.getWorldWidth() / 2f;
        float halfViewportHeight = port.getWorldHeight() / 2f;

        int mapWidthInPixels = roomManager.getCurrentRoom().getMap().getProperties().get("width", Integer.class) * TILE_SIZE;
        int mapHeightInPixels = roomManager.getCurrentRoom().getMap().getProperties().get("height", Integer.class) * TILE_SIZE;

        camera.position.x = Math.max(halfViewportWidth, camera.position.x);
        camera.position.x = Math.min(mapWidthInPixels - halfViewportWidth, camera.position.x);

        camera.position.y = Math.max(halfViewportHeight, camera.position.y);
        camera.position.y = Math.min(mapHeightInPixels - halfViewportHeight, camera.position.y);

        player.update(delta);
        roomManager.update(delta);

        camera.update();
        screenCamera.update();

    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom += 0.2f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.zoom -= 0.2f;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            for (NPC npc : roomManager.getCurrentRoom().getNpcs()) {
                if (npc.getHitbox().contains(getMousePosInGameWorld())) {
                    npc.clicked(delta);
                }
            }
        }
    }

    public Vector2 getMousePosInGameWorld() {
        Vector3 pos = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        return new Vector2(pos.x, pos.y);
    }


    public OrthographicCamera getCamera() {
        return camera;
    }

    @Override
    public void render(float delta) {

        time += delta;
        lightTime += delta;

        if (delta > 0.025f) {
            System.out.println("DELTA SPIKE: " + delta);
        }

        //Idk if these change anything, open gl is confusing lol
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        update(delta);
        handleInput(delta);
        torch.move();
//        System.out.println(new Color().set(sunsetColor));
//        System.out.println(sunsetColor.lerp(midnightColor, 0.25f));
//        System.out.println(sunsetColor);

        lerp = oscillate(0f, 1f, 0.3f, lightTime);
        lerp(midnightColor, sunsetColor, lerp);
        sun.setColor(sunColor);

        //lerp 1 = 12pm, 0 = 12 am
        //24 hour system, 0 to 24

        if (lerp > 0f && previousLerp < lerp) {
            meridiem = Meridiem.AM;
            clockTime = lerp * 11;
        }
        if (lerp < 1f && previousLerp > lerp) {
            meridiem = Meridiem.PM;
            clockTime = (1 - lerp) * 11;
        }
        if(clockTime > 6 && meridiem == Meridiem.PM){
            torch.hide();
        }
        if(clockTime >= 4 && meridiem == Meridiem.AM){
            torch.show();
        }

//        System.out.println((int)clockTime + " " + meridiem);
        previousLerp = lerp;
        //Frame buffer with player and tiles on it
        ScreenUtils.clear(0, 0, 0, 1, true);
        batch.setShader(null);
        //Render the world

        roomManager.render(batch, delta, camera);
        sunRayHandler.setAmbientLight(0, 0, 0, (float) Math.min(lightingMax, Math.max(0.1, lerp)));
        sunRayHandler.setCombinedMatrix(camera);
        sunRayHandler.update();
        sunRayHandler.render();
//
//        lightsRayHandler.setAmbientLight(0, 0, 0, (float) Math.min(lightingMax, Math.max(0.1, lerp)));
        lightsRayHandler.setCombinedMatrix(camera);
        lightsRayHandler.update();
        lightsRayHandler.render();

        //Render in 3d space for lighting
        batch.begin();
        batch.setProjectionMatrix(screenCamera.combined);
        font.draw(batch, "FPS " + Gdx.graphics.getFramesPerSecond(), 10, 1070);
        batch.end();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        roomManager.renderFade(screenCamera);
    }

    private float oscillate(float min, float max, float speed, float time) {
        float l = min + (max - min) * (0.5f + 0.5f * (float) Math.sin(time * speed));
        return new BigDecimal(Float.toString(l)).setScale(2, RoundingMode.HALF_UP).floatValue();

    }

    private void lerp(Color from, Color to, float l) {
        sunColor.r = l * (to.r - from.r) + from.r;
        sunColor.g = l * (to.g - from.g) + from.g;
        sunColor.b = l * (to.b - from.b) + from.b;
        sunColor.a = l * (to.a - from.a) + from.a;
    }


    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        port.update(width, height, true);

        sunRayHandler.useCustomViewport(port.getScreenX(), port.getScreenY(), port.getScreenWidth(), port.getScreenHeight());
        lightsRayHandler.useCustomViewport(port.getScreenX(), port.getScreenY(), port.getScreenWidth(), port.getScreenHeight());
        screenSpacePort.update(width, height, true);
    }

    @Override
    public void show() {
        // Prepare your screen here.
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
    }
}
