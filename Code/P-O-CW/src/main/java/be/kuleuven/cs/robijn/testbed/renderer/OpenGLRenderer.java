package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.*;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

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
import static org.lwjgl.opengl.GL32.GL_FIRST_VERTEX_CONVENTION;
import static org.lwjgl.opengl.GL32.glProvokingVertex;
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

        glProvokingVertex(GL_FIRST_VERTEX_CONVENTION);

        OpenGLRenderer renderer = new OpenGLRenderer(windowHandle);
        renderer.initializeModels();
        return renderer;
    }

    private long windowHandle;

    private Model droneModel = null;
    private Model boxModel = null;

    private OpenGLRenderer(long windowHandle){
        this.windowHandle = windowHandle;
    }

    private void initializeModels(){
        //Load shader for textured models
        Shader vertexShader = Shader.compileVertexShader(Resources.loadTextResource("/shaders/textured/vertex.glsl"));
        Shader fragmentShader = Shader.compileFragmentShader(Resources.loadTextResource("/shaders/textured/fragment.glsl"));
        ShaderProgram texturedProgram = ShaderProgram.link(vertexShader, fragmentShader);

        //Load drone
        Mesh droneMesh = OBJLoader.loadFromResources("/models/drone/drone.obj");
        Texture texture = Texture.load(Resources.loadImageResource("/models/drone/texture.jpg"));
        droneModel = new Model(droneMesh, texture, texturedProgram);

        //Load shader for box model
        vertexShader = Shader.compileVertexShader(Resources.loadTextResource("/shaders/box/vertex.glsl"));
        fragmentShader = Shader.compileFragmentShader(Resources.loadTextResource("/shaders/box/fragment.glsl"));
        ShaderProgram boxProgram = ShaderProgram.link(vertexShader, fragmentShader);

        //Load cube
        Mesh mesh = OBJLoader.loadFromResources("/models/cube/cube.obj");
        boxModel = new Model(mesh, null, boxProgram);
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
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        //Replace previous frame with a blank screen
        glClearColor(0.2f, 0.2f, 0.2f, 1f);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

        //Setup per-camera matrices
        OpenGLCamera openglCamera = (OpenGLCamera)camera;
        Matrix4f viewMatrix = createViewMatrix(openglCamera);
        Matrix4f projectionMatrix = createProjectionMatrix(openglCamera.getHorizontalFOV(), openglCamera.getVerticalFOV(), 0.1f, 1000f);
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

        Model model = null;
        if(obj instanceof Box){
            model = boxModel;
        }else if(obj instanceof Drone){
            model = droneModel;
        }else{
            return;
        }

        //Setup per-object matrices
        Matrix4f modelMatrix = createModelMatrix(obj.getWorldPosition(), obj.getRotation(), new ArrayRealVector(new double[]{1, 1, 1}, false));
        model.getShader().setUniformMatrix("viewProjectionTransformation", false, viewProjectionMatrix);
        model.getShader().setUniformMatrix("modelTransformation", false, modelMatrix);

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
        //Reading the pixels from an OpenGL framebuffer results in an array of bytes that is backwards.
        //For example:
        //000111222      888777666
        //333444555  =>  555444333
        //666777888      222111000
        //This means that RGB pixels become BGR pixels and that the image is flipped vertically and horizontally (rotate 180°)
        //The colorspace issue is fixed in OpenGLFrameBuffer, and the flipped image is fixed here by rendering the world
        //with the camera rotated 180° so that upon reading, the correct result is produced.
        viewMatrix.rotate((float)Math.PI, 0, 0 , 1);
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
