package io.midnight_hills;

// Source - https://stackoverflow.com/a/25472228
// Posted by kajacx, modified by community. See post 'Timeline' for change history
// Retrieved 2026-04-21, License - CC BY-SA 3.0

import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import java.util.Stack;

public class FrameBufferManager {
    private Stack<FrameBuffer> stack = new Stack<FrameBuffer>();

    public void begin(FrameBuffer buffer) {
        if (!stack.isEmpty()) {
            stack.peek().end();
        }
        stack.push(buffer).begin();
    }

    public FrameBuffer getCurrent(){
        return stack.peek();
    }

    public void end() {
        stack.pop().end();
        if (!stack.isEmpty()) {
            stack.peek().begin();
        }
    }
}
