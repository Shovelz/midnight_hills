package io.midnight_hills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.ArrayList;


public class GameScreen implements Screen {

    private SpriteBatch batch;
    private AssetManager assetManager;
    private Main game;
    private Animation<TextureRegion> currentAnimation, firstAnimation;

    private OrthographicCamera camera;
    private FitViewport port, screenSpacePort;

    private Player player;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    int TILE_SIZE = 16; // pixels per tile
    int VIEWPORT_WIDTH = 256;  // in pixels
    int VIEWPORT_HEIGHT = 144; // in pixels
    private ArrayList<Rectangle> collisionRects = new ArrayList<>();

    private int[] foregroundLayers, backgroundLayers, middleLayers;
    private int mapWidthInPixels, mapHeightInPixels;
    private ShaderProgram shaderProgram;
    private Texture noiseTexture, whitePixel;

    private FrameBuffer frameBuffer;
    private TextureRegion fbRegion;
    private OrthographicCamera screenCamera;
    private float time = 0f;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private ArrayList<Vector2> points = new ArrayList<>();
    private PerspectiveCamera persCamera;
    private ModelBatch modelBatch;
    private Model screenModel;
    private Environment environment;
    private ModelInstance screenInstance;
    private FreeLookCameraController cameraController;


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
        persCamera.position.set(0f, -3f, 2f);
        persCamera.lookAt(0f, 0f, 0f);
        persCamera.near = 0.1f;
        persCamera.far = 100f;
        persCamera.update();

        map = new TmxMapLoader().load("map/map.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map, batch); // reuse your SpriteBatch

        backgroundLayers = new int[]{0};
        middleLayers = new int[]{1};
        foregroundLayers = new int[]{2};
        // Grab collisions
        for (MapObject object : map.getLayers().get("Collisions").getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                collisionRects.add(rect);
            }
        }

        int mapWidthInTiles = map.getProperties().get("width", Integer.class);
        int mapHeightInTiles = map.getProperties().get("height", Integer.class);
        mapWidthInPixels = mapWidthInTiles * TILE_SIZE;
        mapHeightInPixels = mapHeightInTiles * TILE_SIZE;

        player = new Player(assetManager, collisionRects, this, new Vector2(VIEWPORT_WIDTH / 2f, VIEWPORT_HEIGHT / 2f));


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

        fbRegion = new TextureRegion(frameBuffer.getColorBufferTexture());
        fbRegion.flip(false, true);


        //Get polygons from tilemap and use for shadows, need to get framebuffer working before making this 3d
//        for (MapObject object : map.getLayers().get("Shadows").getObjects()) {
//            if (object instanceof PolygonMapObject) {
//
//
//                Polygon polygon = ((PolygonMapObject) object).getPolygon();
//
//                BodyDef bodyDef = new BodyDef();
//                bodyDef.type = BodyDef.BodyType.StaticBody;
//                bodyDef.position.set(polygon.getX(), polygon.getY());
//                points.add(new Vector2(polygon.getX(), polygon.getY()));
//
//                Body boxBody = shadowWorld.createBody(bodyDef);
//
//                float[] vertices = polygon.getVertices();
//                FloatArray verts = new FloatArray(polygon.getVertices());
//                ShortArray indices = new EarClippingTriangulator().computeTriangles(verts);
//
//                for (int i = 0; i < indices.size; i += 3) {
//                    float[] tri = new float[6];
//                    for (int j = 0; j < 3; j++) {
//                        int idx = indices.get(i + j) * 2;
//                        tri[j*2]     = verts.get(idx);
//                        tri[j*2 + 1] = verts.get(idx + 1);
//                    }
//
//                    PolygonShape shape = new PolygonShape();
//                    shape.set(tri);
//                Fixture fixture = boxBody.createFixture(shape, 0f);
//
//                LightData data = new LightData(0.5f);
//                fixture.setUserData(data);
//                    shape.dispose();
//                }
//
//            }
//        }

        font = new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);

        font.getData().setScale(0.4f);

        modelBatch = new ModelBatch();
        ModelBuilder modelBuilder = new ModelBuilder();

//        Material material = new Material(
//            TextureAttribute.createDiffuse(frameBuffer.getColorBufferTexture())
//        );

        Material material = new Material(
            TextureAttribute.createEmissive(frameBuffer.getColorBufferTexture())
        );

        screenModel = modelBuilder.createRect(
            -1f, -1f, 0f,
            1f, -1f, 0f,
            1f, 1f, 0f,
            -1f, 1f, 0f,
            0, 0, 1,
            material,
            VertexAttributes.Usage.Position
                | VertexAttributes.Usage.TextureCoordinates
                | VertexAttributes.Usage.Normal
        );

        screenInstance = new ModelInstance(screenModel);
        screenInstance.transform.idt();
        screenInstance.transform.translate(0f, 0f, 0f);
        screenInstance.transform.scale(8f, 4.5f, 1f);

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 0.6f, 1f));
        environment.add(
            new DirectionalLight().set(
                1f, 0.6f, 0.8f,   // light color
                -0.2f, -1f, -0.3f // direction (POINTING FROM light → surface)
            )
        );


        cameraController = new FreeLookCameraController(persCamera);
        Gdx.input.setCursorCatched(true);

        persCamera.position.set(0.04528238f, 0.07076593f, 1.9752901f);
        persCamera.direction.set(-0.018214911f, -0.0021088764f, -0.999832f);
    }

    @Override
    public void show() {
        // Prepare your screen here.
    }

    private void update(float delta) {
        float lerp = 5f * delta;
        camera.position.x += (player.getHitbox().x + player.getHitbox().width / 2f - camera.position.x) * lerp;
        camera.position.y += (player.getHitbox().y + player.getHitbox().height / 2f - camera.position.y) * lerp;

        // Clamp camera to map bounds
        float halfViewportWidth = port.getWorldWidth() / 2f;
        float halfViewportHeight = port.getWorldHeight() / 2f;

        camera.position.x = Math.max(halfViewportWidth, camera.position.x);
        camera.position.x = Math.min(mapWidthInPixels - halfViewportWidth, camera.position.x);

        camera.position.y = Math.max(halfViewportHeight, camera.position.y);
        camera.position.y = Math.min(mapHeightInPixels - halfViewportHeight, camera.position.y);

        camera.update();
        screenCamera.update();
        player.update(delta);

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

        update(delta);
        handleInput(delta);
        cameraController.update(delta);


//        float dayLength = 10f;
//        float dayProgress = (time % dayLength) / dayLength;
//        float angle = MathUtils.lerp(0f, 180f, dayProgress);
//        sunLight.setDirection(angle);
//        sunShadow.setDirection(angle);


        //Frame buffer with player and tiles on it, currently work and renders
        frameBuffer.begin();
        ScreenUtils.clear(1, 0, 0, 1, true);
        batch.setShader(null);
        mapRenderer.setView(camera);

        mapRenderer.render(backgroundLayers);

        batch.setProjectionMatrix(camera.combined);
        mapRenderer.render(middleLayers);
        batch.begin();
        player.render(batch, delta);
        batch.end();

        mapRenderer.render(foregroundLayers);

        frameBuffer.end();


//        Uncomment this code and comment 3d render code below to see frame buffer drawing to screen space
//        batch.setProjectionMatrix(screenCamera.combined);
//        batch.begin();
//        batch.draw(fbRegion, 0, 0, screenSpacePort.getWorldWidth(), screenSpacePort.getWorldHeight());
//        batch.end();


        //3d render to quad
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        modelBatch.begin(persCamera);
        modelBatch.render(screenInstance, environment);
        modelBatch.end();

        persCamera.update();

        //Shader for clouds, currently not in use
//        batch.setShader(null);
//        Gdx.gl.glUseProgram(0);
////        batch.setShader(shaderProgram);
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
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        port.update(width, height, true);

        screenSpacePort.update(width, height, true);

        if (frameBuffer != null) frameBuffer.dispose();
        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        fbRegion = new TextureRegion(frameBuffer.getColorBufferTexture());
        fbRegion.flip(false, true);

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
