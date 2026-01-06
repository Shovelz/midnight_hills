package io.midnight_hills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class FreeLookCameraController {

    private final PerspectiveCamera camera;

    private float yaw = 90f;
    private float pitch = 0f;
    public float mouseSensitivity = 0.15f;

    public float moveSpeed = 8f;
    public float acceleration = 20f;
    public float damping = 10f;

    private final Vector3 velocity = new Vector3();
    private final Vector3 wishDir = new Vector3();

    private final Vector3 forward = new Vector3();
    private final Vector3 right = new Vector3();

    public FreeLookCameraController(PerspectiveCamera camera) {
        this.camera = camera;
        updateDirection();
    }


    public void update(float delta) {
        handleMouseLook();
        handleMovement(delta);
        camera.update();
    }

    public void setPosition(float x, float y, float z) {
        camera.position.set(x, y, z);
        camera.update();
    }

    public void setYawPitch(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = MathUtils.clamp(pitch, -89f, 89f);
        updateDirection();
    }

    private void handleMouseLook() {
        float dx = Gdx.input.getDeltaX();
        float dy = -Gdx.input.getDeltaY();

        yaw   += dx * mouseSensitivity;
        pitch += dy * mouseSensitivity;
        pitch = MathUtils.clamp(pitch, -89f, 89f);

        updateDirection();
    }

    private void updateDirection() {
        float yawRad = MathUtils.degreesToRadians * yaw;
        float pitchRad = MathUtils.degreesToRadians * pitch;

        Vector3 dir = camera.direction;
        dir.x = MathUtils.cos(pitchRad) * MathUtils.cos(yawRad);
        dir.y = MathUtils.sin(pitchRad);
        dir.z = MathUtils.cos(pitchRad) * MathUtils.sin(yawRad);
        dir.nor();

        camera.up.set(Vector3.Y);
    }

    private void handleMovement(float delta) {
        forward.set(camera.direction.x, 0f, camera.direction.z).nor();
        right.set(forward).crs(Vector3.Y).nor();

        wishDir.setZero();

        if (Gdx.input.isKeyPressed(Input.Keys.W)) wishDir.add(forward);
        if (Gdx.input.isKeyPressed(Input.Keys.S)) wishDir.sub(forward);
        if (Gdx.input.isKeyPressed(Input.Keys.D)) wishDir.add(right);
        if (Gdx.input.isKeyPressed(Input.Keys.A)) wishDir.sub(right);

        // Vertical free-fly
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) wishDir.y += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) wishDir.y -= 1f;

        if (wishDir.len2() > 0f) {
            wishDir.nor().scl(moveSpeed);
        }

        float accel = acceleration * delta;
        velocity.x += MathUtils.clamp(wishDir.x - velocity.x, -accel, accel);
        velocity.y += MathUtils.clamp(wishDir.y - velocity.y, -accel, accel);
        velocity.z += MathUtils.clamp(wishDir.z - velocity.z, -accel, accel);

        if (wishDir.len2() == 0f) {
            velocity.scl(1f - Math.min(1f, damping * delta));
        }

        camera.position.mulAdd(velocity, delta);
    }
}
