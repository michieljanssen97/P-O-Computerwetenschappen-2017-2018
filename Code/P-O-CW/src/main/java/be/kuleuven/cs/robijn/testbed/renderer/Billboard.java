package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.Resources;
import be.kuleuven.cs.robijn.worldObjects.Camera;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.RealVector;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class Billboard extends Model {
    public static Billboard create(Texture texture){
        //Generate mesh
        float ratio = (float)texture.getWidth() / (float)texture.getHeight();
        float[] vertices = new float[]{
                -0.5f, -0.5f, 0,
                -0.5f,  0.5f, 0,
                 0.5f, -0.5f, 0,
                 0.5f,  0.5f, 0
        };
        float[] textureCoords = new float[]{
                0, 0,
                0, 1,
                1, 0,
                1, 1
        };
        int[] indices = new int[]{
                0, 2, 1,
                3, 1, 2
        };
        Mesh mesh = Mesh.loadMesh(vertices, textureCoords, null, indices);

        //Load shader
        ShaderProgram shader;
        try(Shader vertexShader = Shader.compileVertexShader(Resources.loadTextResource("/shaders/sprite/vertex.glsl"))){
            try(Shader fragmentShader = Shader.compileFragmentShader(Resources.loadTextResource("/shaders/sprite/fragment.glsl"))){
                shader = ShaderProgram.link(vertexShader, fragmentShader);
            }
        }

        return new Billboard(mesh, texture, shader);
    }

    private Billboard(Mesh mesh, Texture texture, ShaderProgram shader) {
        super(mesh, texture, shader);
    }

    public Matrix4f generateModelMatrix(Camera camera, RealVector position, float scale){
        Rotation cameraRotation = camera.getWorldRotation();
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.identity();
        modelMatrix.translate((float)position.getEntry(0), (float)position.getEntry(1), (float)position.getEntry(2));
        modelMatrix.rotate(new Quaternionf(-(float)cameraRotation.getQ1(), -(float)cameraRotation.getQ2(), -(float)cameraRotation.getQ3(), (float)cameraRotation.getQ0()));
        modelMatrix.scale(scale, scale, scale);

        return modelMatrix;
    }
}
