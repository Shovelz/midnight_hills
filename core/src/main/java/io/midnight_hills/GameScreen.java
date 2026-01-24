package io.midnight_hills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.midnight_hills.map.OrthogonalTiledMapRendererWithSprites;
import io.midnight_hills.map.rooms.Door;
import io.midnight_hills.map.rooms.Room;
import io.midnight_hills.map.rooms.RoomFactory;
import io.midnight_hills.map.rooms.RoomManager;
import org.lwjgl.Sys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class GameScreen implements Screen {

    private SpriteBatch batch;
    private AssetManager assetManager;
    private Main game;

    private OrthographicCamera camera;
    private FitViewport port, screenSpacePort;

    private Player player;
    public final int TILE_SIZE = 16; // pixels per tile
    int VIEWPORT_WIDTH = 256;  // in pixels
    int VIEWPORT_HEIGHT = 144; // in pixels
    private ArrayList<Rectangle> collisionRects = new ArrayList<>();

    private MapLayer foregroundLayers, backgroundLayers, middleLayers, treeLayer;
    private ShaderProgram shaderProgram;
    private Texture noiseTexture;

    private FrameBuffer frameBuffer;
    private TextureRegion frameRegion;
    private OrthographicCamera screenCamera;
    private float time = 0f;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private ArrayList<Vector2> points = new ArrayList<>();
    private PerspectiveCamera persCamera;
    private ModelBatch modelBatch;
    private Model bottomModel;
    private Environment environment;
    private ModelInstance bottomLayerInstance;
    private FreeLookCameraController cameraController;

    private TextureAttribute bottomLayerAttr, topLayerAttr;
    private DirectionalShadowLight sun;
    private float lightTime = 0f;
    private final Matrix4 originalTopTransform = new Matrix4();

    private RoomManager roomManager;
    private RoomFactory roomFactory;


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

        persCamera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        persCamera.position.set(0f, 0f, 12f);
        persCamera.direction.set(0, 0, -1f);
        persCamera.near = 0.1f;
        persCamera.far = 50f;
        persCamera.update();
//
//        backgroundLayers = map.getLayers().get("Background");
//        middleLayers = map.getLayers().get("Foreground");
//        foregroundLayers = map.getLayers().get("OverlapPlayer");
//        treeLayer = map.getLayers().get("Trees");

        player = new Player(assetManager, camera, new Vector2(VIEWPORT_WIDTH / 2f, VIEWPORT_HEIGHT / 2f));

        String vert = Gdx.files.internal("shaders/test.vertex.glsl").readString();
        String frag = Gdx.files.internal("shaders/test.fragment.glsl").readString();

        noiseTexture = new Texture("shaders/perlin.jpg");
        noiseTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        // Cloud shader (not currently in use)
        shaderProgram = new ShaderProgram(vert, frag);

        if (shaderProgram.isCompiled()) {
            System.out.println("Compiled Successfully");
//            batch.setShader(shaderProgram);
        } else {
            throw new GdxRuntimeException(shaderProgram.getLog());
        }

        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            false);

        frameRegion = new TextureRegion(frameBuffer.getColorBufferTexture());

        String vertexSource = Gdx.files.internal("shaders/my.vertex.glsl").readString();
        String fragmentSource = Gdx.files.internal("shaders/my.fragment.glsl").readString();

        DefaultShader.Config config = new DefaultShader.Config(vertexSource, fragmentSource);

        modelBatch = new ModelBatch(new DefaultShaderProvider(config));

        ModelBuilder modelBuilder = new ModelBuilder();


        //Background layer
        Material backgroundMaterial = new Material("screen", TextureAttribute.createDiffuse(frameBuffer.getColorBufferTexture()), FloatAttribute.createAlphaTest(0.5f), IntAttribute.createCullFace(GL20.GL_NONE));
        backgroundMaterial.set(
            new BlendingAttribute(
                GL20.GL_SRC_ALPHA,
                GL20.GL_ONE_MINUS_SRC_ALPHA
            )
        );
        bottomModel = modelBuilder.createRect(
            -1f, -1f, 0f,
            1f, -1f, 0f,
            1f, 1f, 0f,
            -1f, 1f, 0f,
            0, 0, 1,
            backgroundMaterial,
            VertexAttributes.Usage.Position
                | VertexAttributes.Usage.TextureCoordinates
                | VertexAttributes.Usage.Normal
        );

        bottomLayerInstance = new ModelInstance(bottomModel);
        bottomLayerInstance.transform.idt();
        bottomLayerInstance.transform.translate(0f, 0f, 0f);



        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.2f, 0.2f, 0.2f, 1f));
        sun = new DirectionalShadowLight(4096, 4096, 200f, 200f, 1f, 150f);
        sun.set(2f, 2f, 2f, -1f, -1f, -1f);


        environment.shadowMap = sun;

        environment.add(sun);


//        cameraController = new FreeLookCameraController(persCamera);
//        Gdx.input.setCursorCatched(true);


        bottomLayerAttr = new TextureAttribute(1, frameRegion);

        Pixmap pixmap = new Pixmap(Gdx.files.internal("assets/ui/cursor.png"));
        int xHotspot = 31, yHotspot = 31;
        Cursor cursor = Gdx.graphics.newCursor(pixmap, xHotspot, yHotspot);
        pixmap.dispose();
        Gdx.graphics.setCursor(cursor);

        roomManager = new RoomManager(player, batch);
        roomFactory = new RoomFactory(assetManager);

        FileHandle dir = Gdx.files.internal("assets/map/rooms");
        FileHandle[] files = dir.list();

        for (FileHandle file : files) {
            System.out.println(file.name());        // farm.tmx
            System.out.println(file.nameWithoutExtension()); // farm
            System.out.println(file.path());        // maps/farm.tmx
            roomManager.add(file.nameWithoutExtension(), roomFactory.create(file.nameWithoutExtension()));
        }

        roomManager.init();

        fitQuadToCamera();
    }


    private void fitQuadToCamera() {
        fitInstanceToCamera(bottomLayerInstance);
    }

    private void fitInstanceToCamera(ModelInstance instance) {
        Vector3 pos = instance.transform.getTranslation(new Vector3());

        float distance = persCamera.position.z - pos.z;

        float fovRad = persCamera.fieldOfView * MathUtils.degreesToRadians;
        float height = 2f * distance * MathUtils.tan(fovRad / 2f);
        float width = height * persCamera.viewportWidth / persCamera.viewportHeight;

        instance.transform.idt();
        instance.transform.translate(pos.x, pos.y, pos.z);
        instance.transform.scale(width / 2f, -height / 2f, 1f);
    }


    private void update(float delta) {
        float lerp = 5f * delta;
        camera.position.x += (player.getHitbox().x + player.getHitbox().width / 2f - camera.position.x) * lerp;
        camera.position.y += (player.getHitbox().y + player.getHitbox().height / 2f - camera.position.y) * lerp;

        // Clamp camera to map bounds
        float halfViewportWidth = port.getWorldWidth() / 2f;
        float halfViewportHeight = port.getWorldHeight() / 2f;

        int mapWidthInPixels = roomManager.getCurrentRoom().getMap().getProperties().get("width", Integer.class) * TILE_SIZE;
        int mapHeightInPixels = roomManager.getCurrentRoom().getMap().getProperties().get("width", Integer.class) * TILE_SIZE;

        camera.position.x = Math.max(halfViewportWidth, camera.position.x);
        camera.position.x = Math.min(mapWidthInPixels - halfViewportWidth, camera.position.x);

        camera.position.y = Math.max(halfViewportHeight, camera.position.y);
        camera.position.y = Math.min(mapHeightInPixels - halfViewportHeight, camera.position.y);

        camera.update();
        screenCamera.update();
        persCamera.update();
        player.update(delta);

        roomManager.update(delta);

    }

    private void handleInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
            camera.zoom += 0.2f;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.E)) {
            camera.zoom -= 0.2f;
        }
        camera.update();

    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    @Override
    public void render(float delta) {

        time += delta;

        //Idk if these change anything, open gl is confusing lol
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        update(delta);
        handleInput(delta);
//        cameraController.update(delta);


        lightTime += delta;
        float angle = lightTime * 0.9f; // adjust speed here
        float elevation = 0.5f;

        Vector3 sunDir = new Vector3(1f, elevation, 0f);

        sunDir.rotate(Vector3.Y, angle).nor();
        sun.setDirection(sunDir);

        //Frame buffer with player and tiles on it
        frameBuffer.begin();

        ScreenUtils.clear(1, 0, 0, 1, true);
        batch.setShader(null);

        //Render the world
        roomManager.render(batch, delta, camera);


        frameBuffer.end();

        //3d render to quad
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Vector3 scale = new Vector3();
        originalTopTransform.getScale(scale);

        // Normal scene pass
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Set the instance textures to backgroundBuffer and foregroundBuffer
        bottomLayerAttr.textureDescription.set(frameRegion.getTexture(), Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        bottomLayerInstance.materials.get(0).set(bottomLayerAttr);

        //Render in 3d space for lighting
        modelBatch.begin(persCamera);
        modelBatch.render(bottomLayerInstance, environment);
        modelBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        port.update(width, height, true);

        screenSpacePort.update(width, height, true);
        fitQuadToCamera();

        //Recreate the buffers because we resized the screen
        if (frameBuffer != null) frameBuffer.dispose();
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        frameRegion = new TextureRegion(frameBuffer.getColorBufferTexture());

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
//Shader for clouds, currently not in use
//        batch.setShader(null);
//        Gdx.gl.glUseProgram(0);
/// /        batch.setShader(shaderProgram);
//        batch.setProjectionMatrix(screenCamera.combined);
//        batch.begin();

//        fbRegion.getTexture().bind(0);
//        shaderProgram.setUniformi("u_texture", 0);
//        noiseTexture.bind(1);
//        shaderProgram.setUniformi("u_noiseTexture", 1);
//        shaderProgram.setUniformf("u_time", time);
//        shaderProgram.setUniformf("u_resolution", screenSpacePort.getWorldWidth(), screenSpacePort.getWorldHeight());

//        batch.draw(fbRegion, 0, 0, screenSpacePort.getWorldWidth(), screenSpacePort.getWorldHeight());
//
//        batch.end();
//        batch.setShader(null);
