package io.midnight_hills.render;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

public abstract class Renderable {
    public float depth = 0f;
    protected String shaderType = "Normal";
    protected abstract void render(SpriteBatch batch);

    public static List<String> shaderTypes = List.of("Normal", "Water");
}
