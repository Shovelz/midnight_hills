package io.midnight_hills.render;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import io.midnight_hills.FrameBufferManager;
import io.midnight_hills.player.Player;

import java.util.*;

public class FuckenMapRenderer {


    private List<Renderable> renderables;
    private Map<String, ShaderProgram> renderableToShader;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Player player;
    private FrameBufferManager frameBufferManager;
    private RayHandler lights;
    private float time;
    private FrameBuffer reflectionFBO, waterFBO;


    public FuckenMapRenderer(SpriteBatch batch, Camera gameCamera, Player player, RayHandler lights, FrameBufferManager frameBufferManager) {
        this.batch = batch;
        this.camera = (OrthographicCamera) gameCamera;
        this.player = player;
        this.frameBufferManager = frameBufferManager;
        this.lights = lights;
        this.renderableToShader = new HashMap<>();
        this.renderables = new ArrayList<>();

        loadShaders();
        reflectionFBO = new FrameBuffer(Pixmap.Format.RGBA8888, 1920, 1080, false);
        waterFBO = new FrameBuffer(Pixmap.Format.RGBA8888, 1920, 1080, false);
    }

    private void loadShaders() {

        String vert = Gdx.files.internal("shaders/water.vert.glsl").readString();
        String frag = Gdx.files.internal("shaders/water.frag.glsl").readString();
        ShaderProgram shader = new ShaderProgram(vert, frag);
        if (!shader.isCompiled()) {
            throw new GdxRuntimeException(shader.getLog());
        }

        renderableToShader.put("Water", shader);
    }

    public void clearRenderables() {
        renderables.clear();
    }

    public void addRenderable(Renderable renderable) {
        renderables.add(renderable);
    }

    public void addSpriteRenderable(Renderable renderable) {
        renderables.add(renderable);
    }

    public List<Renderable> getRenderables() {
        return renderables;
    }

    public void render(float delta) {
        time += 0.4f * delta;
        if (time > 20f) {
            time = 0f;
        }

        lights.setCombinedMatrix(camera);
        lights.update();
        batch.setProjectionMatrix(camera.combined);

        renderables.sort((a, b) -> {
            int depthCompare = Float.compare(a.depth, b.depth);
            if (depthCompare != 0) {
                return depthCompare;
            }

            //Same depth sprite renderables, sort by y
            if (a instanceof SpriteRenderable sa && b instanceof SpriteRenderable sb) {
                return Float.compare(sb.getY(), sa.getY());
            }

            return 0;
        });


//        renderable.getClass().getSimpleName()

        System.out.println("NEW RENDERables: \n\n");
        for (Renderable renderable : renderables) {
            System.out.println(renderable + ": " + renderable.depth);
        }
        ScreenUtils.clear(0, 0, 0, 1, true);
        batch.begin();

        for (Renderable renderable : renderables) {

//            if(renderable instanceof SpriteRenderable sr && sr.getName().equals("Player")){
//                lights.updateAndRender();
//                renderable.render(batch);
//                continue;
//            }
            if (renderable.shaderType.equals("Normal")) {
                renderable.render(batch);
            }

            if (renderable.shaderType.equals("Water")) {
                batch.end();
                frameBufferManager.begin(waterFBO);
                batch.begin();
                ScreenUtils.clear(0, 0, 0, 0, true);
                renderable.render(batch);
                batch.end();
                frameBufferManager.end();

                frameBufferManager.begin(reflectionFBO);

                ScreenUtils.clear(0, 0, 0, 0, true);
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                batch.begin();

                batch.draw(
                    player.getSprite(),
                    player.getHitbox().x - 1,
                    player.getHitbox().y,
                    player.getSprite().getWidth(),
                    -player.getSprite().getHeight() // flipped
                );

                batch.end();
                frameBufferManager.end();

                Texture tex = waterFBO.getColorBufferTexture();

                tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                tex.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);

                batch.flush();
                batch.begin();

//                sceneFBO.getColorBufferTexture().bind(1);
//                reflectionFBO.getColorBufferTexture().bind(2);


                TextureRegion reflection = new TextureRegion(reflectionFBO.getColorBufferTexture());
                reflection.flip(false, true);
                // bind screen texture to unit 1
                ShaderProgram shader = renderableToShader.get(renderable.shaderType);
                waterFBO.getColorBufferTexture().bind(1);
                reflection.getTexture().bind(2);

                //Bind textures and reset
                shader.bind();
                shader.setUniformi("u_screenTexture", 1);
                shader.setUniformi("u_reflectionTex", 2);
                Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

                //Shader uniforms
                shader.setUniformf("u_time", time);
                shader.setUniformf("u_intensity", 0.3f);
                shader.setUniformf("u_clarity", 0.3f);
                shader.setUniformf("u_highlightScale", 0.8f);

                shader.setUniformf("u_resolution",
                    Gdx.graphics.getWidth(),
                    Gdx.graphics.getHeight()
                );

                TextureRegion water = new TextureRegion(waterFBO.getColorBufferTexture());
                water.flip(false, true);

                batch.setShader(shader);
                batch.draw(water,
                    camera.position.x - camera.viewportWidth / 2f,
                    camera.position.y - camera.viewportHeight / 2f,
                    camera.viewportWidth,
                    camera.viewportHeight
                );

                batch.setShader(null);
            }
        }

        batch.end();
//        lights.render();
    }
}
