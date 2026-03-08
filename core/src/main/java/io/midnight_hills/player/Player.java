package io.midnight_hills.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class Player {

    private Animation<TextureRegion> currentAnimation, idleAnimation,
        walkDownAnimation, walkUpAnimation, walkLeftAnimation, walkRightAnimation,
        idleUpAnimation, idleDownAnimation, idleLeftAnimation, idleRightAnimation;

    public enum Direction {LEFT, RIGHT, UP, DOWN}
                           //0    1     2   3
    private Direction direction;

    private enum State {IDLE, WALK, JUMP, ATTACK}

    private State state;
    private Vector2 velocity = new Vector2();
    private ArrayList<Rectangle> collisionRects;
    private OrthographicCamera camera;
    private boolean movementLocked = false;

    private float time = 0f;
    private boolean loopAnimation = true;
    private final float walkSpeed = 40f, runSpeed = 6f;
    //    private final float walkSpeed = 25f, runSpeed = 6f;
    private float speed = walkSpeed;
    private Rectangle hitbox = new Rectangle(100, 100, 14, 8);
    private Sprite sprite, shadow;
    private Texture shadowTexture;
    private Body body;

    public Player(AssetManager assetManager, OrthographicCamera camera, Vector2 startPosition, World world
    ) {
        this.collisionRects = new ArrayList<>();
        this.camera = camera;

        assetManager.load("packed/player.atlas", TextureAtlas.class);
        assetManager.load("player/playerShadow.png", Texture.class);
        assetManager.finishLoading();
        TextureAtlas atlas = assetManager.get("packed/player.atlas");

        walkUpAnimation = new Animation<>(0.1f, atlas.findRegions("walkUp"), Animation.PlayMode.LOOP);
        walkDownAnimation = new Animation<>(0.1f, atlas.findRegions("walkDown"), Animation.PlayMode.LOOP);
        walkLeftAnimation = new Animation<>(0.25f, atlas.findRegions("walkLeft"), Animation.PlayMode.LOOP);
        walkRightAnimation = new Animation<>(0.25f, atlas.findRegions("walkRight"), Animation.PlayMode.LOOP);

        idleUpAnimation = new Animation<>(0.25f, atlas.findRegions("idleUp"), Animation.PlayMode.LOOP);
        idleDownAnimation = new Animation<>(0.5f, atlas.findRegions("idleDown"), Animation.PlayMode.LOOP);
        idleLeftAnimation = new Animation<>(0.25f, atlas.findRegions("idleLeft"), Animation.PlayMode.LOOP);
        idleRightAnimation = new Animation<>(0.25f, atlas.findRegions("idleRight"), Animation.PlayMode.LOOP);

        currentAnimation = walkUpAnimation;
        direction = Direction.DOWN;

        hitbox.x = camera.position.x;
        hitbox.y = camera.position.y;
        sprite = new Sprite();
        shadow = new Sprite();
        shadowTexture = assetManager.get("player/playerShadow.png", Texture.class);
        shadow.setSize(12, 6);
        shadow.setRegion(shadowTexture);
        shadow.setOriginCenter();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
//        CircleShape circleShape = new CircleShape();
//        circleShape.setRadius(6);

        bodyDef.position.scl(hitbox.x, hitbox.y);
        body = world.createBody(bodyDef);


    }

    public void setCollisionRects(ArrayList<Rectangle> collisionRects) {
        this.collisionRects = collisionRects;
    }

    public void handleInput(float delta) {
        // Reset horizontal velocity each frame
        velocity.x = 0;
        velocity.y = 0;
        state = State.IDLE;

        if (movementLocked) {
            velocity = Vector2.Zero;
            return;
        }
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

    public Body getBody(){
        return body;
    }


    public void lockInput() {
        movementLocked = true;
    }

    public void unlockInput() {
        movementLocked = false;
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
        sprite.setPosition(hitbox.x - 1, hitbox.y);

        shadow.setPosition(hitbox.x + (sprite.getWidth() - shadow.getWidth()) / 2f - 1, hitbox.y - 2);

        body.getPosition().set(hitbox.x, hitbox.y);

    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public Sprite getShadow() {
        return shadow;
    }

    public void teleport(Vector2 entry) {
        hitbox.x = entry.x;
        hitbox.y = entry.y;
        velocity = Vector2.Zero;
    }

    public void faceDirection(Player.Direction face){
        this.direction = face;
    }

    public void render(SpriteBatch batch, float delta) {

    }
}
