package io.midnight_hills;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class AnimationHelper {
    public static Animation<TextureRegion> loadAnimation(int width, int height, Texture sheet, float speed) {

        TextureRegion[][] tmpFrames = TextureRegion.split(sheet, width, height);

        TextureRegion[] animationFrames = new TextureRegion[tmpFrames.length * tmpFrames[0].length];
        int index = 0;
        for (int row = 0; row < tmpFrames.length; row++) {
            for (int col = 0; col < tmpFrames[tmpFrames.length - 1].length; col++) {
                animationFrames[index++] = tmpFrames[row][col];
            }

        }
        return new Animation<TextureRegion>(speed, animationFrames);
    }


}
