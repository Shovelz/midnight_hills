package io.midnight_hills.map.rooms;

public class House1 extends Room {

    public House1(RoomContext ctx) {
        super(ctx.name, ctx.map, ctx.doors, ctx.colliders);
    }

    @Override
    public void update(float delta) {

    }
}
