package io.midnight_hills.player;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;

public class Torch {

    private Vector2 position;
    private PointLight light;
    private boolean isOn;
    private Player player;

    public Torch(Player player, RayHandler rayHandler){
        this.player = player;

        light = new PointLight(rayHandler, 128, new Color(0.96f, 1, 0.53f, 0.6f), 30, player.getHitbox().x, player.getHitbox().y);
//        light.attachToBody(player.getBody());
    }

    public PointLight getLight(){
        return light;
    }

    public void show(){
        isOn = false;
        light.setActive(false);
    }
    public void hide(){
        isOn = true;
        light.setActive(true);
    }
    public void move(){
        light.setPosition(player.getHitbox().x + player.getHitbox().width/2f,
            player.getHitbox().y + player.getHitbox().getHeight()/2f);
    }
}
