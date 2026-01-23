package io.midnight_hills.map.rooms;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.midnight_hills.Player;
import java.util.ArrayList;

public abstract class Room {

    protected Vector2 entry;
    protected String name;
    protected Player.Direction entryDirection;
    protected ArrayList<Rectangle> colliders;

    public ArrayList<Rectangle> getColliders(){
        return colliders;
    }

    public Player.Direction getEntryDirection() {
        return entryDirection;
    }

    public String getName() {
        return name;
    }

    public Vector2 getEntry() {
        return entry;
    }
}
