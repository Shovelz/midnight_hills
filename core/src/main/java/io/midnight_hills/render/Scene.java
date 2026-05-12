package io.midnight_hills.render;

import java.util.List;

public class Scene {
    private List<Renderable> renderables;
}

/**
 Renderable contains:
 draw method,
 depth (where they are ordered in tiled map class + sprites set their own),
 post-processing type (used to determine fbo and shader to be used on that renderable layer
 can be normal, tree transparency, water, etc

 Renderables:
 sprites (entities)
 overlaps
 shadows
 map layers


 Renderables -> render themselves, send to -> Maprenderer -> orders by Y
 and loops through renderables and calls their render method

**/
