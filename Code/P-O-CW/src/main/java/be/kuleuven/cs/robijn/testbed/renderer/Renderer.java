package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.scene.World;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.io.Closeable;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Renderer implements Closeable {
    public static Renderer create(){
        //Initialize LWJGL

        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        // Create the hidden window
        long windowHandle = glfwCreateWindow(1, 1, "", NULL, NULL);
        if (windowHandle == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        glfwMakeContextCurrent(windowHandle);
        GL.createCapabilities();
        return new Renderer(windowHandle);
    }

    private long windowHandle;
    Model triangle;
    private Renderer(long windowHandle){
        this.windowHandle = windowHandle;

        //Temporary code
        triangle = Model.loadModel();
        Shader vertexShader = Shader.loadVertexShader(Resources.loadTextResource("/shaders/vertex.glsl"));
        Shader fragmentShader = Shader.loadFragmentShader(Resources.loadTextResource("/shaders/fragment.glsl"));
        ShaderProgram program = ShaderProgram.createProgram(vertexShader, fragmentShader);
        triangle.setShaderProgram(program);
    }

    public void renderWorld(World world, Camera camera, FrameBuffer buffer){
        //Use own framebuffer instead of default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, buffer.getId());
        //Set the size and position of the image we want to render in the buffer
        glViewport(0, 0, buffer.getWidth(), buffer.getHeight());
        //We don't use depth for now, so disable
        glDisable(GL_DEPTH_TEST);
        glDepthMask(false);
        //Replace previous frame with a blank screen
        glClearColor(0, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT);

        //Enable shader program
        //TODO: here we want to iterate over the objects in the world,
        //load their geometry, shader and color and draw them.
        //For now, we just draw a triangle
        glUseProgram(triangle.getShaderProgram().getProgramId());

        //Draw triangle
        glBindVertexArray(triangle.getVertexArrayId());
        glDrawArrays(GL_TRIANGLES, 0, triangle.getVertexCount());
    }

    @Override
    public void close() throws IOException {
        //Destroy LWJGL resources
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
