package io.midnight_hills.map;

import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import java.util.ArrayList;
import java.util.List;

public class OrthogonalTiledMapRendererWithSprites extends OrthogonalTiledMapRenderer {
    private List<Sprite> sprites;
    private List<Sprite> overlaps;
    private List<Sprite> shadows;
    private final int drawSpritesAfterLayer = 3;
    private final int drawShadowAfterLayer = 2;
    private final int drawOverlapAfterLayer = 4;
    private RayHandler rayHandler;

    public OrthogonalTiledMapRendererWithSprites(TiledMap map, SpriteBatch batch) {
        super(map, batch);
        sprites = new ArrayList<>();
        shadows = new ArrayList<>();
        overlaps = new ArrayList<>();
    }

    public void addSprite(Sprite sprite){
        sprites.add(sprite);
    }

    public void addShadow(Sprite sprite){
        shadows.add(sprite);
    }

    public void addOverlap(Sprite sprite){
        overlaps.add(sprite);
    }

    public void addRayHandler(RayHandler rayHandler) { this.rayHandler = rayHandler;}
    @Override
    public void render() {
        beginRender();
        int currentLayer = 0;
        for (MapLayer layer : map.getLayers()) {
            if (layer.isVisible()) {
                if (layer instanceof TiledMapTileLayer) {
                    renderTileLayer((TiledMapTileLayer)layer);
                    currentLayer++;
                    if(currentLayer == drawSpritesAfterLayer){
                        for(Sprite sprite : sprites)
                            if(sprite.getTexture() != null)
                                sprite.draw(batch);
                    }
                    if(currentLayer == drawShadowAfterLayer){
                        for(Sprite shadow: shadows)
                            if(shadow.getTexture() != null)
                                shadow.draw(batch);
                    }
                    if(currentLayer == drawOverlapAfterLayer){
                        for(Sprite overlap : overlaps)
                            if(overlap.getTexture() != null)
                                overlap.draw(batch);
                    }
                } else {
                    for (MapObject object : layer.getObjects()) {
                        renderObject(object);
                    }
                }
            }
        }
        endRender();
    }

}
