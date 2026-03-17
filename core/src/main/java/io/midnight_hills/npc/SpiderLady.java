package io.midnight_hills.npc;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;

public class SpiderLady extends NPC {

    private ArrayList<Sprite> overlays, shadows, sprite;
    private Animation<TextureRegion> currentAnimation, idleAnimation;
    private float time = 0;
    private Sprite body;

    private enum State {IDLE,}

    private State state;

    public SpiderLady(NPCContext ctx) {
        super(ctx.name, new Rectangle(ctx.pos.x, ctx.pos.y, 14,9), ctx.map, ctx.assetManager);
        overlays = new ArrayList<>();
        sprite = new ArrayList<>();
        shadows = new ArrayList<>();

        if (!assetManager.isLoaded("packed/spiderLady.atlas")) {
            assetManager.load("packed/spiderLady.atlas", TextureAtlas.class);
            assetManager.finishLoading();
        }
        TextureAtlas atlas = assetManager.get("packed/spiderLady.atlas");

        body = new Sprite();
        sprite.add(body);

        idleAnimation = new Animation<>(0.1f, atlas.findRegions("idle"), Animation.PlayMode.LOOP);

        currentAnimation = idleAnimation;

        body.setSize(39, 30);
        body.setOriginCenter();

        state = State.IDLE;
    }

    @Override
    public ArrayList<Sprite> getShadows() {
        return shadows;
    }

    @Override
    public ArrayList<Sprite> getOverlays() {
        return overlays;
    }

    @Override
    public ArrayList<Sprite> getSprites() {
        return sprite;
    }

    @Override
    public void update(float delta) {
        time += delta;

        currentAnimation = idleAnimation;


        body.setRegion(currentAnimation.getKeyFrame(time, true));
        body.setPosition(hitbox.x - 2, hitbox.y + 1);

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void clicked(float delta) {
    }
}
