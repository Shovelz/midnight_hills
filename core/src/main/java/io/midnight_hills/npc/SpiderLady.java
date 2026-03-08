package io.midnight_hills.npc;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;

public class SpiderLady extends NPC {

    private ArrayList<Sprite> overlays, shadows, sprite;
    private Animation<TextureRegion> currentAnimationBottom, currentAnimationTop, wiggleAnimationTop, wiggleAnimationBottom, idleAnimationTop, idleAnimationBottom, bopAnimationBottom, bopAnimationTop;
    private float time = 0;
    private Sprite top, body;

    private enum State {IDLE, WIGGLE, BOP}

    private State state;

    public SpiderLady(NPCContext ctx) {
        super(ctx.name, new Rectangle(ctx.pos.x, ctx.pos.y, 14, 9), ctx.map, ctx.assetManager);
        overlays = new ArrayList<>();
        sprite = new ArrayList<>();
        shadows = new ArrayList<>();

        if (!assetManager.isLoaded("packed/rockBottom.atlas")) {
            assetManager.load("packed/rockBottom.atlas", TextureAtlas.class);
            assetManager.load("packed/rockTop.atlas", TextureAtlas.class);
            assetManager.finishLoading();
        }
        TextureAtlas atlasBottom = assetManager.get("packed/rockBottom.atlas");
        TextureAtlas atlasTop = assetManager.get("packed/rockTop.atlas");

        body = new Sprite();
        shadows.add(body);
        top = new Sprite();
        overlays.add(top);

        idleAnimationBottom = new Animation<>(0.1f, atlasBottom.findRegions("idle"), Animation.PlayMode.LOOP);
        idleAnimationTop = new Animation<>(0.1f, atlasTop.findRegions("idle"), Animation.PlayMode.LOOP);

        wiggleAnimationBottom = new Animation<>(0.1f, atlasBottom.findRegions("wiggle"), Animation.PlayMode.NORMAL);
        wiggleAnimationTop = new Animation<>(0.1f, atlasTop.findRegions("wiggle"), Animation.PlayMode.NORMAL);

        bopAnimationBottom =  new Animation<>(0.3f, atlasBottom.findRegions("bop"), Animation.PlayMode.NORMAL);
        bopAnimationTop =  new Animation<>(0.3f, atlasTop.findRegions("bop"), Animation.PlayMode.NORMAL);

        currentAnimationBottom = idleAnimationBottom;
        currentAnimationTop = idleAnimationTop;

        body.setSize(20, 13);
        body.setOriginCenter();

        top.setSize(20, 13);
        top.setOriginCenter();

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


        switch (state) {
            case IDLE:
                currentAnimationTop = idleAnimationTop;
                currentAnimationBottom = idleAnimationBottom;

                body.setRegion(currentAnimationBottom.getKeyFrame(time, true));
                top.setRegion(currentAnimationTop.getKeyFrame(time, true));
                body.setSize(20, 13);
                top.setSize(20, 13);
                break;
            case WIGGLE:
                if (currentAnimationBottom != wiggleAnimationBottom) {
                    currentAnimationBottom = wiggleAnimationBottom;
                    currentAnimationTop = wiggleAnimationTop;
                    time = 0;
                }

                body.setRegion(currentAnimationBottom.getKeyFrame(time, false));
                top.setRegion(currentAnimationTop.getKeyFrame(time, false));

                if (currentAnimationBottom.isAnimationFinished(time)) {
                    state = State.IDLE;
                    time = 0;
                }
                body.setSize(20, 13);
                top.setSize(20, 13);
                break;
            case BOP:

                if (currentAnimationBottom != bopAnimationBottom) {
                    currentAnimationBottom = bopAnimationBottom;
                    currentAnimationTop = bopAnimationTop;
                    time = 0;
                }

                body.setRegion(currentAnimationBottom.getKeyFrame(time, false));
                top.setRegion(currentAnimationTop.getKeyFrame(time, false));
                if (currentAnimationBottom.isAnimationFinished(time)) {
                    state = State.IDLE;
                    time = 0;
                }
                body.setSize(20, 19);
                top.setSize(20, 19);
                break;
        }

        body.setPosition(hitbox.x - 2, hitbox.y + 1);
        top.setPosition(hitbox.x - 2, hitbox.y + 1);

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void clicked(float delta) {
        if (state == State.IDLE) {
            state = State.BOP;
//            state = State.WIGGLE;
        }
    }
}
