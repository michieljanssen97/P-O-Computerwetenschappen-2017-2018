package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.common.math.Vector3f;
import be.kuleuven.cs.robijn.common.scene.World;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Renderer implements AutoCloseable {
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

    private Model model;

    private Renderer(long windowHandle){
        this.windowHandle = windowHandle;

        //Temporary code
        Mesh mesh = OBJLoader.loadFromResources("/models/cube/cube.obj");
        Shader vertexShader = Shader.compileVertexShader(Resources.loadTextResource("/shaders/vertex.glsl"));
        Shader fragmentShader = Shader.compileFragmentShader(Resources.loadTextResource("/shaders/fragment.glsl"));
        ShaderProgram program = ShaderProgram.link(vertexShader, fragmentShader);
        model = new Model(mesh, null, program);
    }

    //Temp testing data
    float fovX = (float)Math.PI/2f;
    float fovY = (float)Math.PI/2f;

    public void renderWorld(World world, Camera camera, FrameBuffer buffer){
        //Use own framebuffer instead of default framebuffer
        glBindFramebuffer(GL_FRAMEBUFFER, buffer.getId());
        //Set the size and position of the image we want to render in the buffer
        glViewport(0, 0, buffer.getWidth(), buffer.getHeight());
        //Enable depth testing so we only see the faces oriented towards the camera.
        glEnable(GL_DEPTH_TEST);
        //Replace previous frame with a blank screen
        glClearColor(0, 0f, 0f, 1f);
        glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

        //Setup per-camera matrices
        //TODO: mirror image through transformation matrix?
        Matrix4f viewMatrix = createViewMatrix(camera);
        Matrix4f projectionMatrix = createProjectionMatrix(fovX, fovY, 1f, 1000f);

        //Setup per-object matrices
        Vector3f objPos = new Vector3f(0, 0, -6);
        Vector3f objRotation = new Vector3f(0, 0, 0);
        Vector3f objScale = new Vector3f(1, 1, 1);

        Matrix4f modelMatrix = createModelMatrix(objPos, objRotation, objScale);

        Matrix4f mvp = new Matrix4f(projectionMatrix).mul(viewMatrix).mul(modelMatrix);

        model.getShader().setUniformMatrix("mvp", false, mvp); //TODO: is this the best way to do this?

        //Draw object
        glBindVertexArray(model.getMesh().getVertexArrayObjectId());
        glActiveTexture(GL_TEXTURE0);
        if(model.getTexture() != null){
            glBindTexture(GL_TEXTURE_2D, model.getTexture().getTextureId());
        }else{
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        glDrawElements(GL_TRIANGLES, model.getMesh().getVertexCount(), GL_UNSIGNED_INT, 0);
    }

    /**
     * Returns a linear transformation matrix for transforming vertices from object space to world space.
     * @return a non-null matrix
     */
    private Matrix4f createModelMatrix(Vector3f position, Vector3f rotation, Vector3f scale){
        Matrix4f matrix = new Matrix4f();
        matrix.identity();
        matrix.translate(position.getX(), position.getY(), position.getZ());
        matrix.rotate(rotation.getX(), 1, 0 , 0);
        matrix.rotate(rotation.getY(), 0, 1 , 0);
        matrix.rotate(rotation.getZ(), 0, 0 , 1);
        matrix.scale(scale.getX(), scale.getY(), scale.getZ());
        return matrix;
    }

    /**
     * Returns a linear transformation matrix for transforming vertices from world space to camera space.
     * @return a non-null matrix
     */
    private Matrix4f createViewMatrix(Camera camera){
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
    public void close() {
        //TODO: destroy model resources?

        //Destroy LWJGL resources
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
