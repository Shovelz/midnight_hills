package io.midnight_hills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.*;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalShadowLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.ShadowMap;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
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

    private FrameBuffer backgroundBuffer, foregroundBuffer;
    private TextureRegion backgroundRegion, foregroundRegion;
    private OrthographicCamera screenCamera;
    private float time = 0f;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private ArrayList<Vector2> points = new ArrayList<>();
    private PerspectiveCamera persCamera;
    private ModelBatch modelBatch, shadowBatch;
    private Model bottomModel, topModel;
    private Environment environment;
    private ModelInstance bottomLayerInstance, topLayerInstance;
    private FreeLookCameraController cameraController;

    private TextureAttribute bottomLayerAttr, topLayerAttr;
    private DirectionalShadowLight sun;
    private PointLight rotatingLight;
    private float lightTime = 0f;
    private Model debugBallModel;
    private ModelInstance debugBallInstance;


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

        backgroundBuffer = new FrameBuffer(Pixmap.Format.RGBA8888,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            false);

        backgroundRegion = new TextureRegion(backgroundBuffer.getColorBufferTexture());

        foregroundBuffer = new FrameBuffer(Pixmap.Format.RGBA8888,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            false);

        foregroundRegion = new TextureRegion(backgroundBuffer.getColorBufferTexture());


        modelBatch = new ModelBatch();
        shadowBatch = new ModelBatch(new DepthShaderProvider());

        ModelBuilder modelBuilder = new ModelBuilder();


        //Background layer
        Material backgroundMaterial = new Material("screen", TextureAttribute.createDiffuse(backgroundBuffer.getColorBufferTexture()), FloatAttribute.createAlphaTest(0.5f), IntAttribute.createCullFace(GL20.GL_NONE));
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


        Material topMaterial = new Material("foreground", TextureAttribute.createDiffuse(foregroundBuffer.getColorBufferTexture()), FloatAttribute.createAlphaTest(0.5f), IntAttribute.createCullFace(GL20.GL_NONE));
        topMaterial.set(
            new BlendingAttribute(
                GL20.GL_SRC_ALPHA,
                GL20.GL_ONE_MINUS_SRC_ALPHA
            )
        );

        //Top Layer with shadows
        topModel = modelBuilder.createRect(
            -1f, -1f, 0f,
            1f, -1f, 0f,
            1f, 1f, 0f,
            -1f, 1f, 0f,
            0, 0, 1,
            topMaterial,
            VertexAttributes.Usage.Position
                | VertexAttributes.Usage.TextureCoordinates
                | VertexAttributes.Usage.Normal
        );

        topLayerInstance = new ModelInstance(topModel);
        topLayerInstance.transform.idt();
        topLayerInstance.transform.translate(0f, 0f, 0f);

        fitQuadToCamera();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.3f, 0.3f, 0.3f, 1f));
        sun = new DirectionalShadowLight(4096,4096, 35f, 35f, 1f, 50f);
        sun.set(2f, 2f, 2f, -1f, -1f, -1f);

        environment.shadowMap = sun;
        rotatingLight = new PointLight().set(Color.GREEN, new Vector3(0f, 6f, 0f), 200f); // initial position above the quads


        debugBallModel = modelBuilder.createSphere(
            0.2f, 0.2f, 0.2f, 16, 16,  // width, height, depth, divisions
            new Material(ColorAttribute.createDiffuse(Color.RED)),
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal
        );

        debugBallInstance = new ModelInstance(debugBallModel);
        //Just commented out the point light
//        environment.add(rotatingLight);

        environment.add(sun);


        cameraController = new FreeLookCameraController(persCamera);
        Gdx.input.setCursorCatched(true);

        persCamera.position.set(0f, 0f, 12f);
        persCamera.direction.set(0, 0, -1f);

        bottomLayerAttr = new TextureAttribute(1, backgroundRegion);
        topLayerAttr = new TextureAttribute(1, foregroundRegion);
    }


    private void fitQuadToCamera() {
        float distance = persCamera.position.z - bottomLayerInstance.transform.getTranslation(new Vector3()).z;

        float fovRad = persCamera.fieldOfView * MathUtils.degreesToRadians;
        float height = 2f * distance * MathUtils.tan(fovRad / 2f);
        float width = height * persCamera.viewportWidth / persCamera.viewportHeight;

        bottomLayerInstance.transform.idt();
        bottomLayerInstance.transform.translate(0f, 0f, 0f);
        bottomLayerInstance.transform.scale(width / 2f, -height / 2f, 1f);

        topLayerInstance.transform.idt();
        topLayerInstance.transform.translate(0f, 0f, 1f);
        topLayerInstance.transform.scale(width / 2f, -height / 2f, 1f);

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

        //Idk if these change anything, open gl is confusing lol
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        update(delta);
        handleInput(delta);
        cameraController.update(delta);


        //DirectionShadowLight (sun) position
        float angle2 = lightTime * 0.3f * MathUtils.PI2;
        float x2 = MathUtils.cos(angle2);
        float y2 = 0.5f; // slightly above
        float z2 = MathUtils.sin(angle2);
        sun.setDirection(x2, y2, z2);

        //Point light and debug ball position
        lightTime += delta;
        float radius = 5f;
        float speed = 0.2f;  // rotations per 10 seconds

        float angle = lightTime * speed * MathUtils.PI2;

        float x = MathUtils.cos(angle) * radius;
        float z = MathUtils.sin(angle) * radius;
        float y = 3f;
        //Dont worry about the point light, Im not rendering it
//        rotatingLight.position.set(x, y, z);
        debugBallInstance.transform.setTranslation(rotatingLight.position);


        //Frame buffer with player and tiles on it
        backgroundBuffer.begin();

        ScreenUtils.clear(1, 0, 0, 1, true);
        batch.setShader(null);

        mapRenderer.setView(camera);
        mapRenderer.render(backgroundLayers);
        mapRenderer.render(middleLayers);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        player.render(batch, delta);
        batch.end();

        batch.flush();
        backgroundBuffer.end();

        //Frame buffer with the top layer (the one that should be casting shadows_
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        foregroundBuffer.begin();
        ScreenUtils.clear(0, 0, 0, 0, true);

        mapRenderer.setView(camera);
        mapRenderer.render(foregroundLayers);

        foregroundBuffer.end();


        //3d render to quad
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Shadow map pass of top layer
        sun.begin(Vector3.Zero, persCamera.direction);
        shadowBatch.begin(sun.getCamera());
        shadowBatch.render(topLayerInstance); // only shadow casters
        shadowBatch.end();
        sun.end();

        // Normal scene pass
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        //Set the instance textures to backgroundBuffer and foregroundBuffer
        bottomLayerAttr.textureDescription.set(backgroundRegion.getTexture(), Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        bottomLayerInstance.materials.get(0).set(bottomLayerAttr);

        topLayerAttr.textureDescription.set(foregroundRegion.getTexture(), Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        topLayerInstance.materials.get(0).set(topLayerAttr);

        modelBatch.begin(persCamera);
        modelBatch.render(bottomLayerInstance, environment);
        // disable shadow receiving (this doesnt work for some reason)
        ShadowMap shadowBackup = environment.shadowMap;
        environment.shadowMap = null;
        modelBatch.render(topLayerInstance, environment);
        environment.shadowMap = shadowBackup;
//        bottomLayerInstance.transform.idt();
//        bottomLayerInstance.transform.translate(0f, 0f, 4f);
        modelBatch.render(debugBallInstance);
        modelBatch.end();

        persCamera.update();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        port.update(width, height, true);

        screenSpacePort.update(width, height, true);
        fitQuadToCamera();

        //Recreate the buffers because we resized the screen
        if (backgroundBuffer != null) backgroundBuffer.dispose();
        backgroundBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        backgroundRegion = new TextureRegion(backgroundBuffer.getColorBufferTexture());

        if (foregroundBuffer != null) foregroundBuffer.dispose();
        foregroundBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        foregroundRegion = new TextureRegion(foregroundBuffer.getColorBufferTexture());

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
