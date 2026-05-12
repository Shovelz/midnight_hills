package io.midnight_hills.dialogue;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import io.midnight_hills.dialogue.ui.DialogueBox;
import io.midnight_hills.player.Player;
import org.lwjgl.Sys;

import java.util.ArrayList;
import java.util.Stack;

public class DialogueManager {
    private ArrayList<DialogueBox> dialogues;
    private AssetManager assetManager;

    public final Rectangle dim;
    private Texture boxTexture;
    private Stack<String> messages;
    private String current;
    private BitmapFont dialogueFont;
    private Player player;
    private GlyphLayout layout;

    public DialogueManager(AssetManager assetManager, Player player) {
        this.player = player;
        dialogues = new ArrayList<>();
        boxTexture = assetManager.get("ui/chatbox.png");
        messages = new Stack<>();
        dialogueFont = assetManager.get("pixelFont1.ttf", BitmapFont.class);
        dialogueFont.setColor(Color.BLACK);
        dim = new Rectangle((1920 - (boxTexture.getWidth() * 7.5f)) /
            2f, 32, boxTexture.getWidth() * 7.5f, boxTexture.getHeight() * 7.5f);
        layout = new GlyphLayout();
    }

    public void addDialogue(String message) {
        messages.add(message);
        pop();
    }

    public void pop(){
        if (!messages.isEmpty()) {
            current = messages.pop();
            layout.setText(dialogueFont, current, dialogueFont.getColor(), dim.getWidth() - 9f*7.5f, 1, true );
        }
    }

    public void clicked() {
        pop();
        if(messages.isEmpty()){
            current = null;
            player.unlockInput();
        }
    }

    public void render(SpriteBatch batch, float delta) {
        if (current != null) {
            batch.draw(boxTexture, dim.x, dim.y, dim.width, dim.height);
            dialogueFont.draw(batch, layout, dim.x + (5 * 7.5f), dim.y + dim.height - (5* 7.5f));
            player.lockInput();
//            dialogueFont.draw(batch, current, 100, 100);
        }
    }


}
