package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.Camera;
import be.kuleuven.cs.robijn.common.FrameBuffer;
import be.kuleuven.cs.robijn.common.Renderer;
import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.WorldObject;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.io.Closeable;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.system.MemoryUtil.NULL;

public class OpenGLRenderer implements Renderer {
    public static OpenGLRenderer create(){
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
        return new OpenGLRenderer(windowHandle);
    }

    private long windowHandle;

    //Temp testing code
    private Model model;

    private OpenGLRenderer(long windowHandle){
        this.windowHandle = windowHandle;

        //Temp testing code
        Mesh mesh = OBJLoader.loadFromResources("/models/drone/FRE.obj");
        Shader vertexShader = Shader.compileVertexShader(Resources.loadTextResource("/shaders/vertex.glsl"));
        Shader fragmentShader = Shader.compileFragmentShader(Resources.loadTextResource("/shaders/fragment.glsl"));
        ShaderProgram program = ShaderProgram.link(vertexShader, fragmentShader);
        Texture texture = Texture.load(Resources.loadImageResource("/models/drone/CIRRUSTS.JPG"));
        model = new Model(mesh, texture, program);
    }

    @Override
    public void render(WorldObject worldRoot, FrameBuffer buffer, Camera camera){
        if(!(buffer instanceof OpenGLFrameBuffer)){
            throw new IllegalArgumentException("Incompatible framebuffer");
        }
        if(!(camera instanceof OpenGLCamera)){
            throw new IllegalArgumentException("Incompatible camera");
        }

        //Use own framebuffer instead of default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, ((OpenGLFrameBuffer)buffer).getId());
        //Set the size and position of the image we want to render in the buffer
        glViewport(0, 0, buffer.getWidth(), buffer.getHeight());
        //Enable depth testing so we only see the faces oriented towards the camera.
        glEnable(GL_DEPTH_TEST);
        //Replace previous frame with a blank screen
        glClearColor(0.2f, 0.2f, 0.2f, 1f);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

        //Setup per-camera matrices
        //TODO: mirror image through transformation matrix?
        OpenGLCamera openglCamera = (OpenGLCamera)camera;
        Matrix4f viewMatrix = createViewMatrix(openglCamera);
        Matrix4f projectionMatrix = createProjectionMatrix(openglCamera.getHorizontalFOV(), openglCamera.getVerticalFOV(), 1f, 10000f);
        Matrix4f viewProjectionMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);

        //Render objects
        for (WorldObject child : worldRoot.getChildren()){
            renderChildren(child, viewProjectionMatrix);
        }
    }

    private void renderChildren(WorldObject obj, Matrix4f viewProjectionMatrix){
        //Recursively render children
        for (WorldObject child : obj.getChildren()){
            renderChildren(child, viewProjectionMatrix);
        }

        //Setup per-object matrices
        Matrix4f modelMatrix = createModelMatrix(obj.getPosition(), obj.getRotation(), new ArrayRealVector(new double[]{1, 1, 1}, false));
        Matrix4f mvp = new Matrix4f(viewProjectionMatrix).mul(modelMatrix);
        model.getShader().setUniformMatrix("mvp", false, mvp); //TODO: is this the best way to do this?

        //Bind object model mesh, texture, shader, ...
        glBindVertexArray(model.getMesh().getVertexArrayObjectId());
        glActiveTexture(GL_TEXTURE0);
        if(model.getTexture() != null){
            glBindTexture(GL_TEXTURE_2D, model.getTexture().getTextureId());
        }else{
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        glUseProgram(model.getShader().getProgramId());

        //Draw object
        glDrawElements(GL_TRIANGLES, model.getMesh().getIndexCount(), GL_UNSIGNED_INT, 0);
    }

    /**
     * Returns a linear transformation matrix for transforming vertices from object space to world space.
     * @return a non-null matrix
     */
    private Matrix4f createModelMatrix(RealVector position, RealVector rotation, RealVector scale){
        Matrix4f matrix = new Matrix4f();
        matrix.identity();

        float posX = (float)position.getEntry(0);
        float posY = (float)position.getEntry(1);
        float posZ = (float)position.getEntry(2);
        matrix.translate(posX, posY, posZ);

        matrix.rotate((float)rotation.getEntry(0), 1, 0 , 0);
        matrix.rotate((float)rotation.getEntry(1), 0, 1 , 0);
        matrix.rotate((float)rotation.getEntry(2), 0, 0 , 1);

        float scaleX = (float)scale.getEntry(0);
        float scaleY = (float)scale.getEntry(1);
        float scaleZ = (float)scale.getEntry(2);
        matrix.scale(scaleX, scaleY, scaleZ);
        return matrix;
    }

    /**
     * Returns a linear transformation matrix for transforming vertices from world space to camera space.
     * @return a non-null matrix
     */
    private Matrix4f createViewMatrix(OpenGLCamera camera){
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();
        viewMatrix.rotate(camera.getRotation().getX(), 1, 0 , 0);
        viewMatrix.rotate(camera.getRotation().getY(), 0, 1 , 0);
        viewMatrix.rotate(camera.getRotation().getZ(), 0, 0 , 1);
        viewMatrix.translate(-camera.getPosition().getX(), -camera.getPosition().getY(), -camera.getPosition().getZ());
        return viewMatrix;
    }

    /**
     * Returns a linear transformation matrix for transforming vertices from camera space to screen space.
     * @return a non-null matrix
     */
    private Matrix4f createProjectionMatrix(float xFOV, float yFOV, float zNear, float zFar){
        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.identity();
        projectionMatrix.perspective(yFOV, xFOV/yFOV, zNear, zFar);
        return projectionMatrix;
    }

    @Override
    public FrameBuffer createFrameBuffer(int width, int height) {
        return OpenGLFrameBuffer.create(width, height);
    }

    @Override
    public OpenGLCamera createCamera() {
        return new OpenGLCamera();
    }

    @Override
    public void close() {
        //TODO: destroy model resources?
        //Destroy LWJGL resources
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
