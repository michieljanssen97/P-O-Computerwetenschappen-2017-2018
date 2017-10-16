package be.kuleuven.cs.robijn.testbed.renderer;

public class Model {
    private Mesh mesh;
    private Texture texture;
    private ShaderProgram shader;

    public Model(Mesh mesh, Texture texture, ShaderProgram shader){
        if(mesh == null){
            throw new IllegalArgumentException("mesh cannot be null");
        }else if(shader == null){
            throw new IllegalArgumentException("shader cannot be null");
        }

        this.mesh = mesh;
        this.texture = texture;
        this.shader = shader;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Texture getTexture() {
        return texture;
    }

    public ShaderProgram getShader() {
        return shader;
    }
}
