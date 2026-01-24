package io.midnight_hills.map.rooms;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.midnight_hills.Player;

public class Door {

    private Player.Direction entryDirection;
    private Rectangle hitbox;
    private String belongsTo, destination;
    private Vector2 entryLocation;

    public Door(Player.Direction direction, Rectangle hitbox, String belongs, String dest, Vector2 loc){
        this.entryDirection = direction;
        this.hitbox = hitbox;
        this.belongsTo = belongs;
        this.destination = dest;
        this.entryLocation = loc;
    }

    public Player.Direction getEntryDirection() {
        return entryDirection;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public String getBelongsTo() {
        return belongsTo;
    }

    public String getDestination() {
        return destination;
    }

    public Vector2 getEntryLocation() {
        return entryLocation;
    }
}
