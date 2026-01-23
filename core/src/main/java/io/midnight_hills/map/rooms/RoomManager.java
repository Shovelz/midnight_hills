package io.midnight_hills.map.rooms;

import java.util.HashMap;

public class RoomManager {

    private HashMap<String, Room> rooms;

    public RoomManager(){
        rooms = new HashMap<String, Room>();
    }

    public void add(String id, Room room){
        rooms.put(id, room);
    }

    public Room get(String id){
        return rooms.get(id);
    }
}
