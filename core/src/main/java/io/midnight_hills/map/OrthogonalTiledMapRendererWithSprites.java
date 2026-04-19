package io.midnight_hills.map;

import box2dLight.RayHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import io.midnight_hills.player.Player;
import org.lwjgl.Sys;

import java.util.*;

public class OrthogonalTiledMapRendererWithSprites extends OrthogonalTiledMapRenderer {
    private List<Sprite> sprites;
    private List<Sprite> overlaps;
    private List<Sprite> shadows;
    private final String drawSpritesAfterLayer = "Foreground"; //3
    private final String drawShadowAfterLayer = "Paths"; //2
    private final String drawOverlapAfterLayer = "OverlapPlayer"; //4
    private Map<String, ShaderProgram> tileLayerToShader;
    private RayHandler rayHandler;
    private ShaderProgram waterShader;
    private float time = 0f;
    private FrameBuffer fbo, reflectionFbo;
    private Camera camera;
    private float viewportWidth = 256, viewportHeight = 144;
    private Player player;

    public OrthogonalTiledMapRendererWithSprites(TiledMap map, SpriteBatch batch, Camera gameCamera, Player player) {
        super(map, batch);
        sprites = new ArrayList<>();
        shadows = new ArrayList<>();
        overlaps = new ArrayList<>();
        tileLayerToShader = new HashMap<>();
        this.camera = gameCamera;
        this.player = player;

        loadShaders();

        fbo = new FrameBuffer(Pixmap.Format.RGBA8888, 1920, 1080, false);
        reflectionFbo = new FrameBuffer(Pixmap.Format.RGBA8888, 1920, 1080, false);
    }

    private void loadShaders() {

        String vert = Gdx.files.internal("shaders/test.vertex.glsl").readString();
        String frag = Gdx.files.internal("shaders/test.frag.glsl").readString();
        waterShader = new ShaderProgram(vert, frag);
        if (waterShader.isCompiled()) {
            System.out.println("Water Shader Compiled Successfully");
        } else {
            throw new GdxRuntimeException(waterShader.getLog());
        }

        tileLayerToShader.put("Water", waterShader);

    }

    public Map<String, ShaderProgram> getShaders() {
        return tileLayerToShader;
    }

    public void addSprite(Sprite sprite) {
        sprites.add(sprite);
    }

    public void addShadow(Sprite sprite) {
        shadows.add(sprite);
    }

    public void addOverlap(Sprite sprite) {
        overlaps.add(sprite);
    }

    public void addRayHandler(RayHandler rayHandler) {
        this.rayHandler = rayHandler;
    }

    public void render(float delta) {
        time += (0.4f * delta);
        beginRender();
        for (MapLayer layer : map.getLayers()) {

            if (!layer.isVisible()) continue;

            if (!(layer instanceof TiledMapTileLayer)) {
                for (MapObject object : layer.getObjects()) {
                    renderObject(object);
                }
                continue;
            }


            //If the tile layer has a shader
            if (tileLayerToShader.containsKey(layer.getName())) {
                String layerName = layer.getName();
                ShaderProgram shader = tileLayerToShader.get(layerName);

                batch.end();

                fbo.begin();
                batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                ScreenUtils.clear(132 / 255f, 115 / 255f, 156 / 255f, 0, true);
                batch.begin();

                ScreenUtils.clear(0, 0, 0, 0, true);

                renderTileLayer((TiledMapTileLayer) layer);

                batch.end();
                fbo.end();


                if (layerName.equals("Water")) {
                    reflectionFbo.begin();
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
                    reflectionFbo.end();
                }

                batch.begin();
                TextureRegion water = new TextureRegion(fbo.getColorBufferTexture());
                water.flip(false, true);

                batch.setShader(shader);
                if (layerName.equals("Water")) {
                    Texture tex = fbo.getColorBufferTexture();

                    tex.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
                    tex.setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
                    Texture fboTex = fbo.getColorBufferTexture();

                    // bind screen texture to unit 1
                    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
                    fbo.getColorBufferTexture().bind();
                    shader.setUniformi("u_screenTexture", 1);

                    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE2);
                    TextureRegion reflection = new TextureRegion(reflectionFbo.getColorBufferTexture());
                    reflection.flip(false, true);
                    reflection.getTexture().bind();
                    shader.setUniformi("u_reflectionTex", 2);

                    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

                    shader.setUniformf("u_time", time);
                    shader.setUniformf("u_intensity", 0.3f);
                    shader.setUniformf("u_clarity", 0.3f);
                    shader.setUniformf("u_highlightScale", 0.8f);

                    shader.setUniformf("u_resolution",
                        Gdx.graphics.getWidth(),
                        Gdx.graphics.getHeight()
                    );
                }

                batch.draw(water,
                    camera.position.x - viewportWidth / 2f,
                    camera.position.y - viewportHeight / 2f,
                    viewportWidth,
                    viewportHeight
                );


                batch.end();
                batch.begin();

                batch.setShader(null);
            } else {
                renderTileLayer((TiledMapTileLayer) layer);
            }

            if (layer.getName().equals(drawSpritesAfterLayer)) {
                sprites.sort((Sprite a, Sprite b) -> (int) (b.getY() - a.getY()));

                for (Sprite sprite : sprites) {
                    if (sprite.getTexture() != null) {
                        sprite.draw(batch);
                    }
                }
            }

            if (layer.getName().equals(drawShadowAfterLayer)) {
                for (Sprite shadow : shadows)
                    if (shadow.getTexture() != null)
                        shadow.draw(batch);
            }

            if (layer.getName().equals(drawOverlapAfterLayer)) {
                for (Sprite overlap : overlaps)
                    if (overlap.getTexture() != null)
                        overlap.draw(batch);
            }
        }
        endRender();


    }

}
