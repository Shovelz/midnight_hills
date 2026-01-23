package io.midnight_hills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class Player {

    private Animation<TextureRegion> currentAnimation, idleAnimation,
        walkDownAnimation, walkUpAnimation, walkLeftAnimation, walkRightAnimation,
        idleUpAnimation, idleDownAnimation, idleLeftAnimation, idleRightAnimation;

    public enum Direction {LEFT, RIGHT, UP, DOWN}

    private Direction direction;

    private enum State {IDLE, WALK, JUMP, ATTACK}

    private State state;
    private Vector2 velocity = new Vector2();
    private ArrayList<Rectangle> collisionRects;
    private GameScreen gameScreen;

    private float time = 0f;
    private boolean loopAnimation = true;
    private final float walkSpeed = 50f, runSpeed = 6f;
    //    private final float walkSpeed = 25f, runSpeed = 6f;
    private float speed = walkSpeed;
    private Rectangle hitbox = new Rectangle(100, 100, 14, 8);
    private Sprite sprite;

    public Player(AssetManager assetManager, ArrayList<Rectangle> collisionRects, GameScreen gameScreen, Vector2 startPosition) {
        this.collisionRects = new ArrayList<>(collisionRects);
        this.gameScreen = gameScreen;

        assetManager.load("packed/player.atlas", TextureAtlas.class);
        assetManager.finishLoading();
        TextureAtlas atlas = assetManager.get("packed/player.atlas");

        walkUpAnimation = new Animation<>(0.1f, atlas.findRegions("walkUp"), Animation.PlayMode.LOOP);
        walkDownAnimation = new Animation<>(0.1f, atlas.findRegions("walkDown"), Animation.PlayMode.LOOP);
        walkLeftAnimation = new Animation<>(0.25f, atlas.findRegions("walkLeft"), Animation.PlayMode.LOOP);
        walkRightAnimation = new Animation<>(0.25f, atlas.findRegions("walkRight"), Animation.PlayMode.LOOP);

        idleUpAnimation = new Animation<>(0.25f, atlas.findRegions("idleUp"), Animation.PlayMode.LOOP);
        idleDownAnimation = new Animation<>(0.25f, atlas.findRegions("idleDown"), Animation.PlayMode.LOOP);
        idleLeftAnimation = new Animation<>(0.25f, atlas.findRegions("idleLeft"), Animation.PlayMode.LOOP);
        idleRightAnimation = new Animation<>(0.25f, atlas.findRegions("idleRight"), Animation.PlayMode.LOOP);

        currentAnimation = walkUpAnimation;
        direction = Direction.DOWN;

        hitbox.x = gameScreen.getCamera().position.x;
        hitbox.y = gameScreen.getCamera().position.y;
        sprite = new Sprite();

    }

    public void handleInput(float delta) {
        // Reset horizontal velocity each frame
        velocity.x = 0;
        velocity.y = 0;
        state = State.IDLE;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocity.x = -1;
            direction = Direction.LEFT;
            state = State.WALK;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocity.x = 1;
            direction = Direction.RIGHT;
            state = State.WALK;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            velocity.y = 1;
            direction = Direction.UP;
            state = State.WALK;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            velocity.y = -1;
            direction = Direction.DOWN;
            state = State.WALK;
        }

    }

    public void update(float delta) {
        handleInput(delta);
        time += delta;

        //Normalize and scale to speed
        velocity.nor().scl(speed);

        //Vertical then Horizontal collision
        float newY = hitbox.y + velocity.y * delta;
        Rectangle verticalBounds = new Rectangle(hitbox.x, newY, hitbox.width, hitbox.height);

        for (Rectangle rect : collisionRects) {
            if (verticalBounds.overlaps(rect)) {
                System.out.println("Vertical Overlap");
                if (velocity.y > 0) {
                    newY = rect.y - hitbox.height;
                } else if (velocity.y < 0) {
                    newY = rect.y + rect.height;
                }
                velocity.y = 0;
            }
        }

        hitbox.y = newY;

        float newX = hitbox.x + velocity.x * delta;
        Rectangle horizontalBounds = new Rectangle(newX, hitbox.y, hitbox.width, hitbox.height);

        for (Rectangle rect : collisionRects) {
            if (horizontalBounds.overlaps(rect)) {
                System.out.println("Horizontal Overlap");
                if (velocity.x > 0) {
                    newX = rect.x - hitbox.width;
                } else if (velocity.x < 0) {
                    newX = rect.x + rect.width;
                }
                velocity.x = 0;
            }
        }
        hitbox.x = newX;

        if (state == State.WALK) {
            switch (direction) {
                case UP:
                    currentAnimation = walkUpAnimation;
                    break;
                case DOWN:
                    currentAnimation = walkDownAnimation;
                    break;
                case LEFT:
                    currentAnimation = walkLeftAnimation;
                    break;
                case RIGHT:
                    currentAnimation = walkRightAnimation;
                    break;
            }
        }

        if (state == State.IDLE) {
            switch (direction) {
                case UP:
                    currentAnimation = idleUpAnimation;
                    break;
                case DOWN:
                    currentAnimation = idleDownAnimation;
                    break;
                case LEFT:
                    currentAnimation = idleLeftAnimation;
                    break;
                case RIGHT:
                    currentAnimation = idleRightAnimation;
                    break;
            }
        }


        TextureRegion frame = currentAnimation.getKeyFrame(time, true);
        sprite.setRegion(frame);
        sprite.setSize(16, 17);
        sprite.setOriginCenter();
        sprite.setPosition(hitbox.x, hitbox.y);


    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void render(SpriteBatch batch, float delta) {

//        TextureRegion frame = currentAnimation.getKeyFrame(time, loopAnimation);
//        if (direction == Direction.LEFT && !frame.isFlipX()) {
//            frame.flip(true, false);
//        } else if (direction == Direction.RIGHT && frame.isFlipX()) {
//            frame.flip(true, false);
//        }
//        sprite.draw(batch);
    }
}
