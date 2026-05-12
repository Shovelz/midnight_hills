package io.midnight_hills.screens;

import box2dLight.DirectionalLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.*;
import io.midnight_hills.FrameBufferManager;
import io.midnight_hills.Main;
import io.midnight_hills.dialogue.DialogueManager;
import io.midnight_hills.map.rooms.RoomFactory;
import io.midnight_hills.map.rooms.RoomManager;
import io.midnight_hills.npc.NPC;
import io.midnight_hills.npc.NPCFactory;
import io.midnight_hills.player.Player;
import io.midnight_hills.player.Torch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

public class GameScreen implements Screen {

    private SpriteBatch batch;
    private AssetManager assetManager;
    private Main game;

    private OrthographicCamera camera;

    private Player player;
    public final int TILE_SIZE = 16; // pixels per tile
    int VIEWPORT_WIDTH = 256;  // in pixels
    int VIEWPORT_HEIGHT = 144; // in pixels
    private ArrayList<Rectangle> collisionRects = new ArrayList<>();

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
    private FrameBuffer fbo, cloudFBO;
    private ShaderProgram cloudShader;
    private FrameBufferManager frameBufferManager;
    private Texture cloudTexture;
    private VfxManager vfxManager;
    private BloomEffect vfxEffect;
    private VignettingEffect vignettingEffect;
    private ChromaticAberrationEffect chromaticAberrationEffect;
    private DialogueManager dialogueManager;
    private Vector2 gameSize;

    private FitViewport screenSpacePort;
    private FitViewport port;


    //I don't have to win, you just have to lose
    // I will not let you destroy my world
    //LET THE PAIN RUN THOUGH YOU, LET IT MOLD YOU INTO AN UNSTOPPABLE FORCE, I BELIEVE IN YOU
    //Dementia is a scary problem that our grandparents used to have.
    //Soon, it'll be our parents.
    //Time marches on. You're in its way. Do everything you can and wish to, while you can.
    // “You don’t know the value of a moment, until it becomes a memory.”
    public GameScreen(SpriteBatch batch, AssetManager assetManager, Main game) {

        this.gameSize = new Vector2(256, 144);
        this.batch = batch;
        this.assetManager = assetManager;
        this.game = game;

        camera = new OrthographicCamera();
        port = new FitViewport(gameSize.x, gameSize.y, camera);

        port.apply(true);
        camera.update();

        screenCamera = new OrthographicCamera();
        screenCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        screenSpacePort = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), screenCamera);
        screenSpacePort.apply();
        screenCamera.update();



        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 1920, 1080, false);
        cloudFBO = new FrameBuffer(Pixmap.Format.RGBA8888, 1920, 1080, false);


        noiseTexture = new Texture("shaders/perlin.jpg");
        noiseTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

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

        this.dialogueManager = new DialogueManager(assetManager, player);
        Pixmap pixmap = new Pixmap(Gdx.files.internal("assets/ui/cursor.png"));
        int xHotspot = 31, yHotspot = 31;
        Cursor cursor = Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot);
        pixmap.dispose();
        Gdx.graphics.setCursor(cursor);

        frameBufferManager = new FrameBufferManager();

        roomManager = new RoomManager(player, batch, camera, lightsRayHandler, frameBufferManager);
        npcFactory = new NPCFactory(assetManager, dialogueManager);
        roomFactory = new RoomFactory(assetManager, npcFactory);

        FileHandle dir = Gdx.files.internal("assets/map/rooms");
        FileHandle[] files = dir.list();

        for (FileHandle file : files) {
            roomManager.add(file.nameWithoutExtension(), roomFactory.create(file.nameWithoutExtension()));
        }

        roomManager.init();

//        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/myfont.ttf"));
        font = assetManager.get("pixelFont1.ttf", BitmapFont.class);
        font.setColor(Color.WHITE);
//        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        System.out.println("Exists:");

        System.out.println(assetManager.getAssetNames());
//        System.out.println(Gdx.files.internal("fonts/PrStart.ttf").exists());


        midnightColor = new Color().set(new Color(0.24f, 0.22f, 0.35f, 1.0f), 0.5f);
        sunsetColor = new Color().set(new Color(0.78f, 0.76f, 0.55f, 1.0f), 0.5f);
        sunColor = new Color();

        torch = new Torch(player, lightsRayHandler);
        meridiem = Meridiem.AM;

        loadScreenShaders();


        vfxManager = new VfxManager(Pixmap.Format.RGBA8888, 256, 144);

        vignettingEffect = new VignettingEffect(false);
        vignettingEffect.setIntensity(0.7f);
        vfxManager.addEffect(vignettingEffect);

//        chromaticAberrationEffect = new ChromaticAberrationEffect(100);
//        vfxManager.addEffect(chromaticAberrationEffect);
    }

    private void loadScreenShaders() {

        String vert = Gdx.files.internal("shaders/clouds.vert.glsl").readString();
        String frag = Gdx.files.internal("shaders/clouds.frag.glsl").readString();
        cloudShader = new ShaderProgram(vert, frag);
        cloudTexture = new Texture(Gdx.files.internal("cloud.png"));
//        cloudTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        if (!cloudShader.isCompiled()) {
            throw new GdxRuntimeException(cloudShader.getLog());
        }
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
        //OUTPUT THESE

        camera.position.x = Math.max(halfViewportWidth, camera.position.x);
        camera.position.x = Math.min(mapWidthInPixels - halfViewportWidth, camera.position.x);

        camera.position.y = Math.max(halfViewportHeight, camera.position.y);
        camera.position.y = Math.min(mapHeightInPixels - halfViewportHeight, camera.position.y);

        player.update(delta);
        roomManager.update(delta);

        port.apply();
        screenSpacePort.apply();
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
        Vector2 mousePositionRaw = new Vector2(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            System.out.println(dialogueManager.dim);
            System.out.println(mousePositionRaw);

            if (dialogueManager.dim.contains(mousePositionRaw)) {
                dialogueManager.clicked();
                return;
            }
            for (NPC npc : roomManager.getCurrentRoom().getNpcs()) {
                if (npc.getHitbox().contains(getMousePosInGameWorld())) {
                    npc.clicked(delta);
                    return;
                }
            }
        }
    }

    public Vector2 getMousePosInGameWorld() {
        //TODO make this temp, not made on every G frame
        System.out.println("Mouse Y " + Gdx.input.getY());
        Vector3 pos = port.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        return new Vector2(pos.x, pos.y);
    }


    public OrthographicCamera getCamera() {
        return camera;
    }

    @Override
    public void render(float delta) {

        time += delta;

        lightTime += delta;

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

        lerp = 1 - oscillate(0f, 1f, 0.3f, lightTime);
        lerp(midnightColor, sunsetColor, lerp);
        sun.setColor(sunColor);


        clockTime += delta;
        if(clockTime >= 12){
            clockTime = 0;
            meridiem = meridiem == Meridiem.AM ? Meridiem.PM : Meridiem.AM;
        }



        if (clockTime > 6 && meridiem == Meridiem.PM) {
            torch.hide();
        }
        if (clockTime >= 4 && meridiem == Meridiem.AM) {
            torch.show();
        }
//
//        System.out.println((int)clockTime + " " + meridiem);
        previousLerp = lerp;
        //Frame buffer with player and tiles on it
        ScreenUtils.clear(0, 0, 0, 1, true);
        //Render the world
        port.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.setShader(null);
        frameBufferManager.begin(cloudFBO);
        roomManager.render(batch, delta, camera);
        frameBufferManager.end();

//        cloudTexture.bind(1);
        cloudFBO.getColorBufferTexture().bind(1);

        cloudShader.bind();
//        cloudShader.setUniformf("u_time", time);
//        cloudShader.setUniformi("u_screenTexture", 1);
//        cloudShader.setUniformf("u_resolution", Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        cloudShader.setUniformf("u_scale", 40f);
//        cloudShader.setUniformf("u_speed", 20f);
//        cloudShader.setUniformf("u_darkness", 0.6f);
//        cloudShader.setUniformf("u_cameraPos",
//            camera.position.x - camera.viewportWidth / 2f,
//            camera.position.y - camera.viewportHeight / 2f
//        );


        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

        vfxManager.cleanUpBuffers();
        vfxManager.beginInputCapture();

//        batch.setBlendFunction(GL20.GL_DST_COLOR, GL20.GL_ZERO);
//        batch.setShader(cloudShader);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(
            cloudFBO.getColorBufferTexture(),
            camera.position.x - port.getWorldWidth() / 2f,
            camera.position.y - port.getWorldHeight() / 2f,
            port.getWorldWidth(),
            port.getWorldHeight(),
            0, 0, 1, 1
        );
        batch.end();


        batch.setProjectionMatrix(screenCamera.combined);
        batch.begin();
        dialogueManager.render(batch, delta);
        batch.end();
        batch.setShader(null);

        vfxManager.endInputCapture();
        vfxManager.applyEffects();

        // Render result to the screen.
        vfxManager.renderToScreen();
        if (roomManager.getCurrentRoom().isIndoors()) {
            lerp = 0f;
        }
//        lightsRayHandler.render();
        sunRayHandler.setAmbientLight(0, 0, 0, (float) Math.min(lightingMax, Math.max(0.1, lerp)));
        sunRayHandler.setCombinedMatrix(camera);
        sunRayHandler.update();
        sunRayHandler.render();
//        lightsRayHandler.setAmbientLight(0, 0, 0, (float) Math.min(lightingMax, Math.max(0.6, lerp)));
//        lightsRayHandler.update();


        batch.setShader(null);
        batch.begin();
        batch.setProjectionMatrix(screenCamera.combined);
        font.draw(batch, "FPS " + Gdx.graphics.getFramesPerSecond(), 10, screenSpacePort.getWorldHeight() - 5);
        font.draw(batch, "Time " + Math.floor(clockTime * 100) / 100 + " " + meridiem, 10, screenSpacePort.getWorldHeight() - 45);
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
        port.apply();

//        screenCamera.setToOrtho(false, width, height);
//        screenCamera.update();

        screenSpacePort.update(width, height, true);
        screenSpacePort.apply();

        sunRayHandler.useCustomViewport(port.getScreenX(), port.getScreenY(), port.getScreenWidth(), port.getScreenHeight());
        lightsRayHandler.useCustomViewport(port.getScreenX(), port.getScreenY(), port.getScreenWidth(), port.getScreenHeight());

        vfxManager.resize(width, height);

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
        // Since VfxManager has internal frame buffers,
        // it implements Disposable interface and thus should be utilized properly.
        vfxManager.dispose();

        // *** PLEASE NOTE ***
        // VfxManager doesn't dispose attached VfxEffects.
        // This is your responsibility to manage their lifecycle.
        vfxEffect.dispose();

    }
}
