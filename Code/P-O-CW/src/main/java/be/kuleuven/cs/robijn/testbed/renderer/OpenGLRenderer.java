package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.common.Font;
import be.kuleuven.cs.robijn.common.airports.Airport;
import be.kuleuven.cs.robijn.common.airports.Gate;
import be.kuleuven.cs.robijn.common.airports.Runway;
import be.kuleuven.cs.robijn.testbed.renderer.bmfont.BMFont;
import be.kuleuven.cs.robijn.worldObjects.Label3D;
import be.kuleuven.cs.robijn.common.math.VectorMath;
import be.kuleuven.cs.robijn.worldObjects.Box;
import be.kuleuven.cs.robijn.worldObjects.Camera;
import be.kuleuven.cs.robijn.worldObjects.Drone;
import be.kuleuven.cs.robijn.worldObjects.OrthographicCamera;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFWNativeWGL.glfwGetWGLContext;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14.glBlendEquation;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL32.*;
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

        //Enable texture transparancy
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBlendEquation(GL_FUNC_ADD);

        OpenGLRenderer renderer = new OpenGLRenderer(windowHandle);
        renderer.initializeModels();
        renderer.initializeIcons();
        renderer.initializeLineModel();

        RenderDebug.setRenderer(renderer);

        return renderer;
    }

    private long windowHandle;

    private Model droneModel = null;
    private Model boxModel = null;
    private Model groundModel = null;

    private Model lineModel = null;

    private Model runwayModel = null;
    private Model gateModel = null;
    private Model withPackageGateModel = null;

    private Billboard droneIcon;
    private Billboard boxIcon;

    private ArrayList<Line> linesToDraw = new ArrayList<>();
    private ArrayList<OpenGLFrameBuffer> frameBuffers = new ArrayList<>();

    private final LabelRenderer labelRenderer = new LabelRenderer(this);

    private OpenGLRenderer(long windowHandle){
        this.windowHandle = windowHandle;
    }

    private void initializeModels(){
        //Load shader for textured models (the shaders will be closed after linking, but the reference in the program will keep them from being deleted)
        ShaderProgram texturedProgram;
        try(Shader vertexShader = Shader.compileVertexShader(Resources.loadTextResource("/shaders/textured/vertex.glsl"))){
            try(Shader fragmentShader = Shader.compileFragmentShader(Resources.loadTextResource("/shaders/textured/fragment.glsl"))){
                texturedProgram = ShaderProgram.link(vertexShader, fragmentShader);
            }
        }

        //Load drone
        //Mesh droneMesh = OBJLoader.loadFromResources("/models/drone/drone.obj");
        //Texture droneTexture = Texture.load(Resources.loadImageResource("/models/drone/texture.jpg"));
        Mesh droneMesh = OBJLoader.loadFromResources("/models/drone/predator.obj");
        Texture droneTexture = Texture.load(Resources.loadImageResource("/models/drone/predator.png"));
        droneModel = new Model(droneMesh, droneTexture, texturedProgram);

        //Load shader for box model
        ShaderProgram boxProgram;
        try(Shader vertexShader = Shader.compileVertexShader(Resources.loadTextResource("/shaders/box/vertex.glsl"))){
            try(Shader fragmentShader = Shader.compileFragmentShader(Resources.loadTextResource("/shaders/box/fragment.glsl"))){
                boxProgram = ShaderProgram.link(vertexShader, fragmentShader);
            }
        }

        //Load cube
        Mesh boxMesh = OBJLoader.loadFromResources("/models/cube/cube.obj");
        boxModel = new Model(boxMesh, null, boxProgram);

        //Load ground model
        Mesh groundMesh = OBJLoader.loadFromResources("/models/plane/plane.obj");
        Texture groundTexture = Texture.load(Resources.loadImageResource("/models/ground/texture.png"));
        groundTexture.setTextureScale(new Vector2D(0.0001, 0.0001));
        groundModel = new Model(groundMesh, groundTexture, texturedProgram);

        //Load gate model
        Mesh gateMesh = OBJLoader.loadFromResources("/models/plane/plane.obj");
        gateMesh.setRenderOffset(new Vector3D(0, .006, 0));
        Texture gateTexture = Texture.load(Resources.loadImageResource("/models/gate/texture.png"));
        Texture withPackageGateTexture = Texture.load(Resources.loadImageResource("/models/gate/with_package_texture.png"));
        gateModel = new Model(gateMesh, gateTexture, texturedProgram);
        withPackageGateModel = new Model(gateMesh, withPackageGateTexture, texturedProgram);

        //Load runway model
        Mesh runwayMesh = OBJLoader.loadFromResources("/models/plane/plane.obj");
        runwayMesh.setRenderOffset(new Vector3D(0, .006, 0));
        Texture runwayTexture = Texture.load(Resources.loadImageResource("/models/runway/texture.png"));
        runwayModel = new Model(runwayMesh, runwayTexture, texturedProgram);
    }

    private void initializeIcons(){
        Texture boxIconTexture = Texture.load(Resources.loadImageResource("/icons/cube.png"));
        boxIcon = Billboard.create(boxIconTexture);

        Texture droneIconTexture = Texture.load(Resources.loadImageResource("/icons/drone.png"));
        droneIcon = Billboard.create(droneIconTexture);
    }

    private void initializeLineModel(){
        float[] vertices = new float[]{
                0, 0, 0,
                0, 0, -1,
        };

        int[] indices = new int[]{
                0, 1, 0
        };

        Mesh lineMesh = Mesh.loadMesh(vertices, null, null, indices);

        ShaderProgram lineProgram;
        try(Shader vertexShader = Shader.compileVertexShader(Resources.loadTextResource("/shaders/line/vertex.glsl"))){
            try(Shader fragmentShader = Shader.compileFragmentShader(Resources.loadTextResource("/shaders/line/fragment.glsl"))){
                lineProgram = ShaderProgram.link(vertexShader, fragmentShader);
            }
        }

        lineModel = new Model(lineMesh, null, lineProgram);
    }

    @Override
    public RenderTask startRender(WorldObject worldRoot, FrameBuffer buffer, Camera camera, Semaphore worldStateLock){
        if(!(buffer instanceof OpenGLFrameBuffer)){
            throw new IllegalArgumentException("Incompatible framebuffer");
        }
        if(!(camera instanceof OpenGLPerspectiveCamera) && !(camera instanceof OpenGLOrthographicCamera)){
            throw new IllegalArgumentException("Incompatible camera");
        }

        if(!((OpenGLFrameBuffer) buffer).isReady()){
            throw new IllegalStateException("The framebuffer is unavailable.");
        }

        try {
            worldStateLock.acquire();

            labelRenderer.updateLabelCache(worldRoot);

            //Use own framebuffer instead of default framebuffer
            glBindFramebuffer(GL_FRAMEBUFFER, ((OpenGLFrameBuffer)buffer).getId());
            //Set the size and position of the image we want to render in the buffer
            glViewport(0, 0, buffer.getWidth(), buffer.getHeight());
            //Enable depth testing so we only see the faces oriented towards the camera.
            glEnable(GL_DEPTH_TEST);
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
            //The projection matrix that is used mirrors the y-axis. (see createProjectionMatrix())
            //This causes the winding order of the vertices to flip:
            //   B
            //  / \      Winding order is ABC: counter-clock-wise
            // C - A
            //-------
            // C - A
            //  \ /      Winding order is ABC: clock-wise
            //   B
            //We need to flip the OpenGL winding order as well to compensate and keep the face normals in the same direction.
            //Otherwise face culling will cut away the front parts and leave behind only the back parts
            glFrontFace(GL_CW);
            //Replace previous frame with a blank screen
            glClearColor(1f, 1f, 1f, 1f);
            glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);

            //Setup per-camera matrices
            Matrix4f viewProjectionMatrix = createViewProjectionMatrix(camera);

            //Render ground if needed
            WorldObject groundObj = new WorldObject();
            groundObj.setScale(new ArrayRealVector(new double[]{10000, 1, 10000}, false));
            RealMatrix transform = groundObj.getObjectToWorldTransform();
            renderModel(groundModel, viewProjectionMatrix, transform);

            //Render objects
            for (WorldObject child : worldRoot.getChildren()){
                renderChildren(child, viewProjectionMatrix, camera);
            }

            //Render debug objects if needed
            WorldObject dummyLine = new WorldObject();
            dummyLine.setName("debug-line");
            if(camera.isVisible(dummyLine)){
                for (Line line : linesToDraw){
                    renderLine(line, viewProjectionMatrix);
                }
            }

            //Add a fence sync object so we can check when the rendering finishes.
            long sync = glFenceSync(GL_SYNC_GPU_COMMANDS_COMPLETE, 0);
            if(sync == 0){
                throw new RuntimeException("glFenceSync failed");
            }

            OpenGLRenderTask task = new OpenGLRenderTask(sync);
            ((OpenGLFrameBuffer) buffer).setCurrentRenderTask(task);
            return task;

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            worldStateLock.release();
        }
    }

    private void renderChildren(WorldObject obj, Matrix4f viewProjectionMatrix, Camera camera){
        //Recursively render children
        for (WorldObject child : obj.getChildren()){
            renderChildren(child, viewProjectionMatrix, camera);
        }

        if(!camera.isVisible(obj)){
            return;
        }

        Model model = null;
        if(obj instanceof Box){
            model = boxModel;
            Color boxColor = ((Box)obj).getColor();
            float[] rgbValues = new float[3];
            boxColor.getRGBColorComponents(rgbValues);
            model.getShader().setUniformFloat("color", rgbValues);
        }else if(obj instanceof Drone){
            model = droneModel;
        }else if(obj instanceof Gate){
            Gate gate = (Gate)obj;
            model = gate.hasPackage() ? withPackageGateModel : gateModel;
        }else if(obj instanceof Runway){
            model = runwayModel;
        }else if(obj instanceof Label3D){
            labelRenderer.renderLabel((Label3D)obj, viewProjectionMatrix, camera);
            return;
        }else{
            return;
        }

        renderModel(model, viewProjectionMatrix, obj.getObjectToWorldTransform());

        //Render icon if necessary
        if(camera instanceof OpenGLOrthographicCamera){
            OpenGLOrthographicCamera orthoCam = (OpenGLOrthographicCamera) camera;

            Vector3D size = model.getMesh().getBoundingBox().getBoxDimensions();
            double avgAxis = (size.getX() + size.getY() + size.getZ()) / 3d;
            double minRatio = Math.min(avgAxis/orthoCam.getWidth(), avgAxis/orthoCam.getHeight());
            if(minRatio < orthoCam.getRenderIconsThresholdRatio()){
                //Render icon

                Billboard icon = null;
                if(obj instanceof Box){
                    icon = boxIcon;
                }else if(obj instanceof Drone){
                    icon = droneIcon;
                }else{
                    return;
                }

                Vector3D billboardRelPos = camera.getWorldRotation().applyTo(
                        new Vector3D(orthoCam.getIconOffset().getX(), orthoCam.getIconOffset().getY(), 0)
                );
                RealVector billboardPosition = obj.getWorldPosition().add(
                        new ArrayRealVector(new double[]{billboardRelPos.getX(), billboardRelPos.getY(), billboardRelPos.getZ()}, false));
                renderModel(icon, viewProjectionMatrix, icon.generateModelMatrix(camera, billboardPosition, orthoCam.getIconSize()));
            }
        }
    }

    void renderModel(Model model, Matrix4f viewProjectionMatrix, RealMatrix objectToWorldTransform){
        //Setup per-object matrices
        model.getShader().setUniformMatrix("viewProjectionTransformation", false, viewProjectionMatrix);
        model.getShader().setUniformMatrix("modelTransformation", false, objectToWorldTransform);

        renderModel(model);
    }

    void renderModel(Model model, Matrix4f viewProjectionMatrix, Matrix4f objectToWorldTransform){
        //Setup per-object matrices
        model.getShader().setUniformMatrix("viewProjectionTransformation", false, viewProjectionMatrix);
        model.getShader().setUniformMatrix("modelTransformation", false, objectToWorldTransform);

        renderModel(model);
    }

    void renderModel(Model model){
        //Bind object model mesh, texture, shader, ...
        glBindVertexArray(model.getMesh().getVertexArrayObjectId());
        glActiveTexture(GL_TEXTURE0);
        if(model.getTexture() != null){
            glBindTexture(GL_TEXTURE_2D, model.getTexture().getTextureId());
            if(model.getShader().hasUniform("textureScale")){
                model.getShader().setUniformFloat(
                        "textureScale",
                        (float)model.getTexture().getTextureScale().getX(),
                        (float)model.getTexture().getTextureScale().getY()
                );
            }
        }else{
            glBindTexture(GL_TEXTURE_2D, 0);
        }
        glUseProgram(model.getShader().getProgramId());

        if(model.getShader().hasUniform("vertexOffset")){
            model.getShader().setUniformFloat("vertexOffset",
                    (float)model.getMesh().getRenderOffset().getX(),
                    (float)model.getMesh().getRenderOffset().getY(),
                    (float)model.getMesh().getRenderOffset().getZ()
            );
        }

        //Draw object
        glDrawElements(GL_TRIANGLES, model.getMesh().getIndexCount(), GL_UNSIGNED_INT, 0);
    }

    private void renderLine(Line line, Matrix4f viewProjectionMatrix){
        //Bind object model mesh, texture, shader, ...
        glBindVertexArray(lineModel.getMesh().getVertexArrayObjectId());

        lineModel.getShader().setUniformMatrix("viewProjectionTransformation", false, viewProjectionMatrix);

        lineModel.getShader().setUniformFloat("pointA",
                (float)line.getPointA().getX(), (float)line.getPointA().getY(), (float)line.getPointA().getZ());
        lineModel.getShader().setUniformFloat("pointB",
                (float)line.getPointB().getX(), (float)line.getPointB().getY(), (float)line.getPointB().getZ());

        lineModel.getShader().setUniformFloat("color",
                (float)line.getColor().getRed()/255f,
                (float)line.getColor().getGreen()/255f,
                (float)line.getColor().getBlue()/255f);
        glUseProgram(lineModel.getShader().getProgramId());

        //Draw object
        glDrawElements(GL_LINES, lineModel.getMesh().getIndexCount(), GL_UNSIGNED_INT, 0);
    }

    @Override
    public RealVector screenPointToWorldSpace(Camera camera, FrameBuffer frameBuffer, int screenX, int screenY){
        float z = ((OpenGLFrameBuffer)frameBuffer).readDepth(screenX, screenY);
        return screenPointToWorldSpace(camera, frameBuffer, screenX, screenY, z);
    }

    @Override
    public RealVector screenPointToWorldSpace(Camera camera, FrameBuffer frameBuffer, int screenX, int screenY, float z){
        Matrix4f transform = createViewProjectionMatrix(camera).invert();

        // Map to [0;1]
        float x = ((float)screenX) / (float)frameBuffer.getWidth();
        float y = ((float)screenY) / (float)frameBuffer.getHeight();
        // Map to [-1;1]
        x = (x*2.0f)-1.0f;
        y = (y*2.0f)-1.0f;
        z = (z*2.0f)-1.0f;

        Vector4f worldSpace = transform.transform(x, y, z, 1, new Vector4f());
        worldSpace = worldSpace.div(worldSpace.w);
        return new ArrayRealVector(new double[]{worldSpace.x, worldSpace.y, worldSpace.z}, false);
    }

    private Matrix4f createViewProjectionMatrix(Camera camera){
        Matrix4f viewMatrix = createViewMatrix(camera);
        Matrix4f projectionMatrix = createProjectionMatrix(camera);
        return new Matrix4f(projectionMatrix).mul(viewMatrix);
    }

    /**
     * Returns a linear transformation matrix for transforming vertices from world space to camera space.
     * @return a non-null matrix
     */
    private Matrix4f createViewMatrix(Camera camera){
        Matrix4f viewMatrix = new Matrix4f();
        viewMatrix.identity();

        Rotation rotation = camera.getWorldRotation();
        Quaternionfc quaternion = new Quaternionf((float)rotation.getQ1(), (float)rotation.getQ2(), (float)rotation.getQ3(), (float)rotation.getQ0());
        viewMatrix.rotate(quaternion);

        viewMatrix.translate(
                (float) -camera.getWorldPosition().getEntry(0),
                (float) -camera.getWorldPosition().getEntry(1),
                (float) -camera.getWorldPosition().getEntry(2)
        );
        return viewMatrix;
    }

    /**
     * Returns a linear transformation matrix for transforming vertices from camera space to screen space.
     * @return a non-null matrix
     */
    private Matrix4f createProjectionMatrix(Camera camera){
        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.identity();

        if(camera instanceof OpenGLPerspectiveCamera){
            OpenGLPerspectiveCamera perspCam = (OpenGLPerspectiveCamera)camera;
            projectionMatrix.perspective(
                    perspCam.getVerticalFOV(), perspCam.getHorizontalFOV()/perspCam.getVerticalFOV(),
                    perspCam.getNearPlane(), perspCam.getFarPlane()
            );
        }else if(camera instanceof OpenGLOrthographicCamera){
            OpenGLOrthographicCamera orthoCam = (OpenGLOrthographicCamera)camera;
            float width = orthoCam.getWidth();
            float height = orthoCam.getHeight();
            projectionMatrix.ortho(
                    -width/2f, width/2f, -height/2f,height/2f,
                    orthoCam.getNearPlane(), orthoCam.getFarPlane()
            );
        }else{
            throw new RuntimeException("No projection matrix defined for camera type");
        }

        //Reading the pixels from an OpenGL framebuffer results in a flipped image.
        //For example:
        //000111222      666777888
        //333444555  =>  333444555
        //666777888      666777888
        //This means that RGB pixels become BGR pixels and that the image is flipped vertically
        //The colorspace issue is fixed in OpenGLFrameBuffer, and the flipped image is fixed here by rendering the world
        //with y-axis flipped so that upon reading, the correct result is produced.
        //Flipping the y-axis of the vertices also flips the winding order, this is fixed above in render()
        projectionMatrix.scale(1, -1 ,1);
        return projectionMatrix;
    }

    public BoundingBox getBoundingBoxFor(WorldObject obj){
        BoundingBox b = getBoundingBoxOrNull(obj);
        return b == null ? new BoundingBox(Vector3D.ZERO) : b;
    }

    private BoundingBox getBoundingBoxOrNull(WorldObject obj){
        BoundingBox selfBox;

        if(obj instanceof Box){
            selfBox = boxModel.getMesh().getBoundingBox();
        }else if(obj instanceof Drone){
            selfBox = droneModel.getMesh().getBoundingBox();
        }else if(obj instanceof Gate){
            selfBox = gateModel.getMesh().getBoundingBox();
        }else if(obj instanceof Runway){
            selfBox = runwayModel.getMesh().getBoundingBox();
        }else{
            selfBox = null;
        }

        if (selfBox != null) {
            selfBox.setRelativePosition(obj.getWorldPosition());
            selfBox.scaleDimensions(obj.getScale());
        }

        return obj.getChildren().stream()
                .map(this::getBoundingBoxOrNull)
                .reduce(selfBox, (b1, b2) -> {
                    if(b1 == null){
                        return b2;
                    }
                    if(b2 == null){
                        return b1;
                    }
                    return b1.merge(b2);
                });
    }

    public void addLineToDraw(Line line){
        this.linesToDraw.add(line);
    }

    public void clearDebugObjects(){
        this.linesToDraw.clear();
    }

    @Override
    public FrameBuffer createFrameBuffer(int width, int height) {
        OpenGLFrameBuffer frameBuffer = OpenGLFrameBuffer.create(width, height);
        frameBuffers.add(frameBuffer);
        return frameBuffer;
    }

    @Override
    public OpenGLPerspectiveCamera createPerspectiveCamera() {
        return new OpenGLPerspectiveCamera();
    }

    @Override
    public OrthographicCamera createOrthographicCamera() {
        return new OpenGLOrthographicCamera();
    }

    @Override
    public Font loadFont(String fontName) {
        return fontName == null ? BMFont.loadDefaultFont() : BMFont.loadFont(fontName);
    }

    public long getHGLRC(){
        return glfwGetWGLContext(windowHandle);
    }

    @Override
    public void close() {
        //Destroy model resources
        droneModel.getTexture().close();
        droneModel.getMesh().close();
        droneModel.getShader().close();

        boxModel.getMesh().close();
        boxModel.getShader().close();

        groundModel.getMesh().close();
        groundModel.getShader().close();

        lineModel.getMesh().close();
        lineModel.getShader().close();

        gateModel.getMesh().close();
        gateModel.getTexture().close();

        runwayModel.getMesh().close();
        runwayModel.getTexture().close();

        //Destroy icons
        droneIcon.getTexture().close();
        boxIcon.getTexture().close();

        //Destroy framebuffers
        for (OpenGLFrameBuffer frameBuffer : frameBuffers) {
            frameBuffer.close();
        }
        frameBuffers.clear();

        //Destroy LWJGL resources
        glfwDestroyWindow(windowHandle);
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
