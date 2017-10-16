package be.kuleuven.cs.robijn.testbed.renderer;

import java.io.Closeable;
import java.io.IOException;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Mesh implements AutoCloseable {
    public static Mesh loadMesh(float[] vertexPositions, float[] textureCoordinates, float[] normals, int[] faceIndices){
        //Check that vertexPositions is valid
        if(vertexPositions == null || vertexPositions.length == 0 || vertexPositions.length % 3 != 0){
            throw new IllegalArgumentException("vertexPositions must be non-null, non-empty, and have a number of elements equal to a multiple of 3");
        }

        //Check that faceVertexIndices is valid
        if(faceIndices == null || faceIndices.length == 0 || faceIndices.length % 3 != 0){
            throw new IllegalArgumentException("faceIndices must be non-null, non-empty, and have a number of elements equal to a multiple of 3");
        }

        //If any of the other arguments is null, make them an empty array instead (easier to work with)
        textureCoordinates = textureCoordinates == null ? new float[0] : textureCoordinates;
        normals = normals == null ? new float[0] : textureCoordinates;

        //Verify the dimensions of the arrays
        if(textureCoordinates.length % 2 != 0){
            throw new IllegalArgumentException("textureCoordinates must have a number of elements equal to a multiple of 2");
        }else if(normals.length % 3 != 0){
            throw new IllegalArgumentException("normals must have a number of elements equal to a multiple of 3");
        }

        //Create Vertex Array Object
        int vertexArrayId = glGenVertexArrays();
        glBindVertexArray(vertexArrayId);

        //Create vertex positions buffer
        int vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertexPositions, GL_STATIC_DRAW);
        //glVertexAttribPointer(0,3, GL_FLOAT,false, 0, 0);

        //Create texture coordinates buffer
        int textureCoordinatesBuffer = -1;
        if(textureCoordinates.length != 0){
            textureCoordinatesBuffer = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, textureCoordinatesBuffer);
            glBufferData(textureCoordinatesBuffer, textureCoordinates, GL_STATIC_DRAW);
            //glVertexAttribPointer(1,2, GL_FLOAT,false, 0, 0);
        }

        //Create normals buffer
        int normalsBufferId = -1;
        if(normals.length != 0){
            normalsBufferId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, normalsBufferId);
            glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
            //glVertexAttribPointer(2,3, GL_FLOAT,false, 0, 0);
        }

        //Create indices buffer
        int indicesBufferId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesBufferId);
        //glBufferData(indicesBufferId, faceVertexIndices, GL_STATIC_DRAW);

        return new Mesh(vertexArrayId, vertexBufferId, textureCoordinatesBuffer, normalsBufferId, indicesBufferId);
    }

    private int vertexArrayObjectId;
    private int vertexPositionsBufferId;
    private int textureCoordinatesBufferId;
    private int normalsBufferId;
    private int indicesBufferId;

    private Mesh(int vertexArrayObjectId, int vertexPositionsBufferId,
                 int textureCoordinatesBufferId, int normalsBufferId, int indicesBufferId){
        this.vertexArrayObjectId = vertexArrayObjectId;
        this.vertexPositionsBufferId = vertexPositionsBufferId;
        this.textureCoordinatesBufferId = textureCoordinatesBufferId;
        this.normalsBufferId = normalsBufferId;
        this.indicesBufferId = indicesBufferId;
    }

    @Override
    public void close() {
        glDeleteVertexArrays(vertexArrayObjectId);
        glDeleteBuffers(vertexPositionsBufferId);
        if(textureCoordinatesBufferId != -1){
            glDeleteBuffers(textureCoordinatesBufferId);
        }
        if(normalsBufferId != -1){
            glDeleteBuffers(normalsBufferId);
        }
        glDeleteBuffers(indicesBufferId);
    }
}
