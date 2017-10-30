package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.Resources;
import org.joml.Vector2f;
import org.joml.Vector3f;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class OBJLoader {
    public static Mesh loadFromResources(String resourceName){
        try(InputStreamReader reader = new InputStreamReader(Resources.class.getResourceAsStream(resourceName))){
            return load(reader);
        }catch (IOException ex){
            throw new UncheckedIOException("Failed to load the mesh resource under path '" + resourceName + "'.", ex);
        }
    }

    public static Mesh load(Reader reader) throws IOException{
        OBJLoader loader = new OBJLoader();
        loader.load(new BufferedReader(reader));
        return loader.buildModel();
    }

    private ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
    private ArrayList<Vector2f> textureCoordinates = new ArrayList<Vector2f>();
    private ArrayList<Vector3f> normals = new ArrayList<Vector3f>();

    //TODO: ArrayList<Integer> can produce a large memory overhead if the model has many faces
    //https://stackoverflow.com/questions/9037468/what-is-the-storage-cost-for-a-boxed-primitive-in-java
    //Solution: dynamically growing arrays/buffers? multiple passes over the file, reading the faces in a second pass?
    private ArrayList<Integer> vertexIndices = new ArrayList<Integer>();
    private ArrayList<Integer> textureIndices = new ArrayList<Integer>();
    private ArrayList<Integer> normalIndices = new ArrayList<Integer>();

    private OBJLoader(){ }

    private void load(BufferedReader reader) throws IOException{
        String curLine;
        while((curLine = reader.readLine()) != null){
            curLine = curLine.trim(); //Remove leading or trailing whitespace
            if(curLine.startsWith("#")){
                //This line is a comment, skip
                continue;
            }else if(curLine.startsWith("v ")){
                loadVertex(curLine.substring(2).split(" "));
            }else if(curLine.startsWith("vt ")){
                loadTextureCoordinate(curLine.substring(3).split(" "));
            }else if(curLine.startsWith("vn ")){
                loadVertexNormal(curLine.substring(3).split(" "));
            }else if(curLine.startsWith("f ")){
                loadFace(curLine.substring(2).split(" "));
            }else {
                System.out.println("Ignoring unknown definition type for line: "+curLine);
            }
        }
    }

    private void loadVertex(String[] args){
        float x = Float.parseFloat(args[0]);
        float y = Float.parseFloat(args[1]);
        float z = Float.parseFloat(args[2]);
        vertices.add(new Vector3f(x, y, z));
    }

    private void loadTextureCoordinate(String[] args){
        float x = Float.parseFloat(args[0]);
        float y = Float.parseFloat(args[1]);
        textureCoordinates.add(new Vector2f(x,y));
    }

    private void loadVertexNormal(String[] args){
        float x = Float.parseFloat(args[0]);
        float y = Float.parseFloat(args[1]);
        float z = Float.parseFloat(args[2]);
        normals.add(new Vector3f(x, y, z));
    }

    private void loadFace(String[] args){
        for (int i = 0; i < args.length; i++){
            String[] coordParts = args[i].split("/");

            int vertexIndex = Integer.parseInt(coordParts[0]);

            int textureIndex = 0;
            if(coordParts.length >= 2 && !coordParts[1].equals("")){
                textureIndex = Integer.parseInt(coordParts[1]);
            }

            int normalIndex = 0;
            if(coordParts.length == 3){
                normalIndex = Integer.parseInt(coordParts[2]);
            }

            vertexIndices.add(vertexIndex);
            textureIndices.add(textureIndex);
            normalIndices.add(normalIndex);
        }
    }

    private Mesh buildModel(){
        //This method uses the cheap and easy way to fix the 'multiple indices to one index' problem.
        //Easy to implement and relatively cheap, but breaks if a vertex is associated with multiple texture/normal coordinates

        float[] vertexArray = linearizeVector3fList(vertices);
        float[] textureArray = new float[vertices.size()*2];
        float[] normalArray = new float[vertices.size()*3];

        int[] indexArray = new int[vertexIndices.size()];
        for(int i = 0; i < vertexIndices.size(); i++){
            indexArray[i] = vertexIndices.get(i)-1;
        }

        //Loop over indices
        for(int i = 0; i < vertexIndices.size(); i++) {
            int vertexIndex = vertexIndices.get(i)-1;
            int textureIndex = textureIndices.get(i)-1;
            int normalIndex = normalIndices.get(i)-1;

            if(textureIndex != -1) {
                textureArray[(vertexIndex * 2)] = textureCoordinates.get(textureIndex).x;
                textureArray[(vertexIndex * 2) + 1] = textureCoordinates.get(textureIndex).y;
            }

            if(normalIndex != -1) {
                normalArray[(vertexIndex * 3)] = normals.get(normalIndex).x;
                normalArray[(vertexIndex * 3) + 1] = normals.get(normalIndex).y;
                normalArray[(vertexIndex * 3) + 2] = normals.get(normalIndex).z;
            }
        }

        return Mesh.loadMesh(vertexArray, textureArray, normalArray, indexArray);
    }

    private float[] linearizeVector3fList(List<Vector3f> vectors){
        float[] arr = new float[vectors.size()*3];
        for(int i = 0; i < vectors.size(); i++){
            Vector3f vector = vectors.get(i);
            arr[(i*3)] = vector.x;
            arr[(i*3)+1] = vector.y;
            arr[(i*3)+2] = vector.z;
        }
        return arr;
    }
}
