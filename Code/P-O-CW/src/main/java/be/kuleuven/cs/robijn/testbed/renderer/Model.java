package be.kuleuven.cs.robijn.testbed.renderer;

import java.io.Closeable;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Model implements Closeable {
    private int vertexArrayId, vertexBufferId;
    private ShaderProgram shader;

    public static Model loadModel(){
        int vertexArrayId = glGenVertexArrays();
        glBindVertexArray(vertexArrayId);

        glEnableVertexAttribArray(0);

        int vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);

        float[] vertex_data = {
                -1, -1, 0,
                1, -1, 0,
                0,  1, 0
        };

        glBufferData(GL_ARRAY_BUFFER, vertex_data, GL_STATIC_DRAW);

        glVertexAttribPointer(0,3, GL_FLOAT,false, 0, 0);

        return new Model(vertexArrayId, vertexBufferId);
    }

    private Model(int vertexArrayId, int vertexBufferId){
        this.vertexArrayId = vertexArrayId;
        this.vertexBufferId = vertexBufferId;
    }

    public ShaderProgram getShaderProgram(){
        return shader;
    }

    public void setShaderProgram(ShaderProgram shader){
        this.shader = shader;
    }

    public int getVertexArrayId(){
        return vertexArrayId;
    }

    public int getVertexCount(){
        return 3;
    }

    @Override
    public void close() throws IOException {
        glDeleteVertexArrays(vertexArrayId);
        glDeleteBuffers(vertexBufferId);
    }
}
