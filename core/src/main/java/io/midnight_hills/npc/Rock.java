package io.midnight_hills.npc;

import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import io.midnight_hills.dialogue.DialogueManager;
import io.midnight_hills.render.SpriteRenderable;

import java.util.ArrayList;

public class Rock extends NPC {

    private ArrayList<SpriteRenderable> overlays, shadows, sprite;
    private Animation<TextureRegion> currentAnimation, wiggleAnimation, idleAnimation, bopAnimation;
    private float time = 0;
    private Sprite body;

    private enum State {IDLE, WIGGLE, BOP}

    private State state;

    public Rock(NPCContext ctx) {
        super(ctx.name, new Rectangle(ctx.pos.x, ctx.pos.y, 14, 9), ctx.map, ctx.assetManager, ctx.dialogueManager);
        overlays = new ArrayList<>();
        sprite = new ArrayList<>();
        shadows = new ArrayList<>();

        if (!assetManager.isLoaded("packed/rock.atlas")) {
            assetManager.load("packed/rock.atlas", TextureAtlas.class);
            assetManager.finishLoading();
        }

        TextureAtlas atlas = assetManager.get("packed/rock.atlas");

        body = new Sprite();
        sprite.add(new SpriteRenderable(body, 2.0f, hitbox, "Rock"));

        idleAnimation = new Animation<>(0.1f, atlas.findRegions("idle"), Animation.PlayMode.LOOP);

        wiggleAnimation = new Animation<>(0.1f, atlas.findRegions("wiggle"), Animation.PlayMode.NORMAL);

        bopAnimation = new Animation<>(0.3f, atlas.findRegions("bop"), Animation.PlayMode.NORMAL);

        currentAnimation = idleAnimation;

        body.setSize(20, 13);
        body.setOriginCenter();

        state = State.IDLE;
    }

    @Override
    public ArrayList<SpriteRenderable> getShadows() {
        return shadows;
    }

    @Override
    public ArrayList<SpriteRenderable> getOverlays() {
        return overlays;
    }

    @Override
    public ArrayList<SpriteRenderable> getSprites() {
        return sprite;
    }

    @Override
    public void update(float delta) {
        time += delta;

        switch (state) {
            case IDLE:
                currentAnimation = idleAnimation;

                body.setRegion(currentAnimation.getKeyFrame(time, true));
                body.setSize(20, 13);
                break;
            case WIGGLE:
                if (currentAnimation != wiggleAnimation) {
                    currentAnimation = wiggleAnimation;
                    time = 0;
                }


                body.setRegion(currentAnimation.getKeyFrame(time, false));

                if (currentAnimation.isAnimationFinished(time)) {
                    state = State.IDLE;
                    time = 0;
                }
                body.setSize(20, 13);
                break;
            case BOP:

                if (currentAnimation != bopAnimation) {
                    currentAnimation = bopAnimation;
                    time = 0;
                }

                body.setRegion(currentAnimation.getKeyFrame(time, false));
                if (currentAnimation.isAnimationFinished(time)) {
                    state = State.IDLE;
                    time = 0;
                }
                body.setSize(20, 19);
                break;
        }

        body.setPosition(hitbox.x - 2, hitbox.y + 1);

    }

    @Override
    public void render(float delta) {

    }

    @Override
    public void clicked(float delta) {
        if (state == State.IDLE) {
            state = State.BOP;
            dialogueManager.addDialogue("I'm rockin it sooo hard rn");
//            state = State.WIGGLE;
        }
    }
}
