package be.kuleuven.cs.robijn.testbed.renderer;

import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
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
        normals = normals == null ? new float[0] : normals;

        //Verify the dimensions of the arrays
        if(textureCoordinates.length % 2 != 0){
            throw new IllegalArgumentException("textureCoordinates must have a number of elements equal to a multiple of 2");
        }else if(normals.length % 3 != 0){
            throw new IllegalArgumentException("normals must have a number of elements equal to a multiple of 3");
        }

        //Create Vertex Array Object
        //The buffers and their properties will be stored in this buffer:
        //See also: https://stackoverflow.com/questions/17149728/when-should-glvertexattribpointer-be-called
        int vertexArrayId = glGenVertexArrays();
        glBindVertexArray(vertexArrayId);

        //Create vertex positions buffer
        int vertexBufferId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferId);
        glBufferData(GL_ARRAY_BUFFER, vertexPositions, GL_STATIC_DRAW);
        glVertexAttribPointer(0,3, GL_FLOAT,false, 0, 0);
        glEnableVertexAttribArray(0);

        //Create texture coordinates buffer
        int textureCoordinatesBuffer = -1;
        if(textureCoordinates.length != 0){
            textureCoordinatesBuffer = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, textureCoordinatesBuffer);
            glBufferData(GL_ARRAY_BUFFER, textureCoordinates, GL_STATIC_DRAW);
            glVertexAttribPointer(1,2, GL_FLOAT,false, 0, 0);
            glEnableVertexAttribArray(1);
        }

        //Create normals buffer
        int normalsBufferId = -1;
        if(normals.length != 0){
            normalsBufferId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, normalsBufferId);
            glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
            glVertexAttribPointer(2,3, GL_FLOAT,false, 0, 0);
            glEnableVertexAttribArray(2);
        }

        //Create indices buffer
        int indicesBufferId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesBufferId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, faceIndices, GL_STATIC_DRAW);

        //According to section "5.1.2 Automatic Unbinding of Deleted Objects" and 5.1.3 in the OpenGL spec,
        //you can 'delete' buffers, textures, renderbuffers, ... without actually removing them from GPU memory.
        //When these objects are references by a VAO which is not currently bound to the context, removing them
        //will only delete the 'name' but not the actual object. The objects will be deleted when the last reference
        //to them is removed.

        //Unbind vertex array
        glBindVertexArray(0);

        //Delete buffers when the vertex array is deleted
        glDeleteBuffers(vertexBufferId);
        if(textureCoordinatesBuffer != -1){
            glDeleteBuffers(textureCoordinatesBuffer);
        }
        if(normalsBufferId != -1){
            glDeleteBuffers(normalsBufferId);
        }
        if(indicesBufferId != -1){
            glDeleteBuffers(indicesBufferId);
        }

        //Calculate bounding box
        float minX =  Float.MAX_VALUE, minY =  Float.MAX_VALUE, minZ =  Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;
        for(int i = 0; i < vertexPositions.length; i += 3){
            float x = vertexPositions[i];
            if(x < minX){  minX = x; }
            if(x > maxX){  maxX = x; }
        }
        for(int i = 1; i < vertexPositions.length; i += 3){
            float y = vertexPositions[i];
            if(y < minY){  minY = y; }
            if(y > maxY){  maxY = y; }
        }
        for(int i = 2; i < vertexPositions.length; i += 3){
            float z = vertexPositions[i];
            if(z < minZ){  minZ = z; }
            if(z > maxZ){  maxZ = z; }
        }
        BoundingBox boundingBox = new BoundingBox(
                new Vector3D(minX, minY, minZ),
                new Vector3D(maxX - minX, maxY - minY, maxZ - minZ)
        );

        return new Mesh(vertexArrayId, faceIndices.length, boundingBox);
    }

    private final int vertexArrayObjectId;
    private final int indexCount;
    private final BoundingBox boundingBox;
    private Vector3D offset = new Vector3D(0, 0, 0);

    private Mesh(int vertexArrayObjectId, int indexCount, BoundingBox boundingBox){
        this.vertexArrayObjectId = vertexArrayObjectId;
        this.indexCount = indexCount;
        this.boundingBox = boundingBox;
    }

    public int getVertexArrayObjectId(){
        return vertexArrayObjectId;
    }

    public int getIndexCount(){
        return indexCount;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public Vector3D getRenderOffset(){
        return this.offset;
    }

    public void setRenderOffset(Vector3D offset){
        if(offset == null){
            throw new IllegalArgumentException("Invalid offset");
        }

        this.offset = offset;
    }

    @Override
    public void close() {
        glDeleteVertexArrays(vertexArrayObjectId);
    }
}
