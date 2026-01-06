package io.midnight_hills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class Player {

    private Animation<TextureRegion> currentAnimation, idleAnimation,
        walkDownAnimation, walkUpAnimation, walkLeftAnimation, walkRightAnimation,
        idleUp, idleDown, idleLeft, idleRight;
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

    public Player(AssetManager assetManager, ArrayList<Rectangle> collisionRects, GameScreen gameScreen, Vector2 startPosition) {
        this.collisionRects = new ArrayList<>(collisionRects);
        this.gameScreen = gameScreen;
        assetManager.load("player/walk_down.png", Texture.class);
        assetManager.load("player/walk_up.png", Texture.class);
        assetManager.load("player/walk_left.png", Texture.class);
        assetManager.load("player/walk_right.png", Texture.class);
        assetManager.load("player/idle_up.png", Texture.class);
        assetManager.load("player/idle_down.png", Texture.class);
        assetManager.load("player/idle_left.png", Texture.class);
        assetManager.load("player/idle_right.png", Texture.class);
        assetManager.finishLoading();

        walkDownAnimation = AnimationHelper.loadAnimation(16, 17, assetManager.get("player/walk_down.png"), 0.1f);
        walkUpAnimation = AnimationHelper.loadAnimation(16, 17, assetManager.get("player/walk_up.png"), 0.1f);
        walkLeftAnimation = AnimationHelper.loadAnimation(16, 17, assetManager.get("player/walk_left.png"), 0.25f);
        walkRightAnimation = AnimationHelper.loadAnimation(16, 17, assetManager.get("player/walk_right.png"), 0.25f);
        idleUp = AnimationHelper.loadAnimation(16, 17, assetManager.get("player/idle_up.png"), 0.25f);
        idleDown = AnimationHelper.loadAnimation(16, 17, assetManager.get("player/idle_down.png"), 0.25f);
        idleLeft = AnimationHelper.loadAnimation(16, 17, assetManager.get("player/idle_left.png"), 0.25f);
        idleRight = AnimationHelper.loadAnimation(16, 17, assetManager.get("player/idle_right.png"), 0.25f);

        currentAnimation = walkUpAnimation;
        direction = Direction.DOWN;

        hitbox.x = gameScreen.getCamera().position.x;
        hitbox.y = gameScreen.getCamera().position.y;

    }
    public void handleInput(float delta) {
        // Reset horizontal velocity each frame
        velocity.x = 0;
        velocity.y = 0;
        state = State.IDLE;

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            velocity.x = -1;
            direction = Direction.LEFT;
            state = State.WALK;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            velocity.x = 1;
            direction = Direction.RIGHT;
            state = State.WALK;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            velocity.y = 1;
            direction = Direction.UP;
            state = State.WALK;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            velocity.y = -1;
            direction = Direction.DOWN;
            state = State.WALK;
        }

    }

    public void update(float delta){
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

        if(state == State.WALK){
            switch (direction){
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

        if(state == State.IDLE){
            switch (direction){
                case UP:
                    currentAnimation = idleUp;
                    break;
                case DOWN:
                    currentAnimation = idleDown;
                    break;
                case LEFT:
                    currentAnimation = idleLeft;
                    break;
                case RIGHT:
                    currentAnimation = idleRight;
                    break;
            }
        }


    }

    public Rectangle getHitbox(){
        return hitbox;
    }

    public void render(SpriteBatch batch, float delta){

        TextureRegion frame = currentAnimation.getKeyFrame(time, loopAnimation);
//        if (direction == Direction.LEFT && !frame.isFlipX()) {
//            frame.flip(true, false);
//        } else if (direction == Direction.RIGHT && frame.isFlipX()) {
//            frame.flip(true, false);
//        }
        batch.draw(frame, hitbox.x, hitbox.y);
    }
}
