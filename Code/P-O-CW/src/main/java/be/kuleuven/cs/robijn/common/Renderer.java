package be.kuleuven.cs.robijn.common;

public interface Renderer {
    FrameBuffer createFrameBuffer(int width, int height);
    Camera createCamera();
    void render(FrameBuffer frameBuffer, Camera camera);
}
