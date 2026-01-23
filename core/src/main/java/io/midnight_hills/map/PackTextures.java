package io.midnight_hills.map;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;

public class PackTextures {
    public static void main(String[] args) {
        TexturePacker.Settings settings = new TexturePacker.Settings();
        settings.paddingX = 2;             // horizontal padding in pixels
        settings.paddingY = 2;             // vertical padding
        settings.duplicatePadding = true;  // duplicate border pixels
        settings.maxWidth = 1024;
        settings.maxHeight = 1024;
        settings.edgePadding = true;       // add padding at texture edges
        settings.filterMin = Texture.TextureFilter.Nearest;
        settings.filterMag = Texture.TextureFilter.Nearest;
        settings.useIndexes = true;

        TexturePacker.process(
            settings,
            "assets/player",   // input folder containing all PNG frames
            "assets/packed",               // output folder
            "player"                // atlas name: player.atlas
        );
    }
}
