package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.Resources;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private ArrayList<Vector3i> indices = new ArrayList<Vector3i>();

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
        //3 index values of the vertices that make up the new face
        Vector3i faceVertexIndices = new Vector3i();

        //3 texture index values that make up the new face
        Vector3i faceTextureIndices = new Vector3i();
        boolean faceTextureIndicesSpecified = false;

        //3 normal index values that make up the new face
        Vector3i faceNormalIndices = new Vector3i();
        boolean faceNormalIndicesSpecified = false;

        for (int i = 0; i < args.length; i++){
            String[] coordParts = args[i].split("/");

            int vertexIndex = Integer.parseInt(coordParts[0]);

            int textureIndex = -1;
            if(coordParts.length >= 2 && !coordParts[1].equals("")){
                faceTextureIndicesSpecified = true;
                textureIndex = Integer.parseInt(coordParts[1]);
            }

            int normalIndex = -1;
            if(coordParts.length == 3){
                faceNormalIndicesSpecified = true;
                normalIndex = Integer.parseInt(coordParts[2]);
            }

            faceVertexIndices.setComponent(i, vertexIndex);
            faceTextureIndices.setComponent(i, textureIndex);
            faceNormalIndices.setComponent(i, normalIndex);
        }


        indices.add(faceVertexIndices);
        if(faceTextureIndicesSpecified){
            if(faceVertexIndices.x != faceTextureIndices.x ||
                    faceVertexIndices.y != faceTextureIndices.y ||
                    faceVertexIndices.z != faceTextureIndices.z ){
                //Can fix by implementing https://stackoverflow.com/a/23356738/915418
                throw new RuntimeException("Models with seperate normal or texture indices are currently unsupported");
            }
        }
        if(faceNormalIndicesSpecified){
            if(faceVertexIndices.x != faceNormalIndices.x ||
                    faceVertexIndices.y != faceNormalIndices.y ||
                    faceVertexIndices.z != faceNormalIndices.z ){
                //Can fix by implementing https://stackoverflow.com/a/23356738/915418
                throw new RuntimeException("Models with seperate normal or texture indices are currently unsupported");
            }
        }
    }

    private Mesh buildModel(){
        float[] vertexArray = linearizeVector3fList(vertices);
        float[] textureCoordinatesArray = linearizeVector2fList(textureCoordinates);
        float[] normalsArray = linearizeVector3fList(normals);

        int[] faceIndexArray = linearizeVector3iList(indices);

        return Mesh.loadMesh(vertexArray, textureCoordinatesArray, normalsArray, faceIndexArray);
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

    private float[] linearizeVector2fList(List<Vector2f> vectors){
        float[] arr = new float[vectors.size()*2];
        for(int i = 0; i < vectors.size(); i++){
            Vector2f vector = vectors.get(i);
            arr[(i*2)] = vector.x;
            arr[(i*2)+1] = vector.y;
        }
        return arr;
    }

    private int[] linearizeVector3iList(List<Vector3i> vectors){
        int[] arr = new int[vectors.size()*3];
        for(int i = 0; i < vectors.size(); i++){
            Vector3i vector = vectors.get(i);
            arr[(i*3)] = vector.x;
            arr[(i*3)+1] = vector.y;
            arr[(i*3)+2] = vector.z;
        }
        return arr;
    }
}
