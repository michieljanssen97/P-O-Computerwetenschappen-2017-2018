package be.kuleuven.cs.robijn.testbed.renderer.bmfont;

import be.kuleuven.cs.robijn.common.*;
import be.kuleuven.cs.robijn.testbed.renderer.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL30.*;

public class StringRenderer implements AutoCloseable {
    private static final String VERTEX_SHADER_PATH = "/shaders/text/bake_vertex.glsl";
    private static final String FRAGMENT_SHADER_PATH = "/shaders/text/bake_fragment.glsl";

    private static int instances = 0;

    private static ShaderProgram shaderProgram;

    public StringRenderer(){
        instances++;
    }

    public Texture bake(RenderableString string){
        Texture result = Texture.createEmpty(new int[]{ string.getWidth(), string.getHeight() }, 2);

        //Create framebuffer
        try(OpenGLFrameBuffer frameBuffer = OpenGLFrameBuffer.create(result, false)) {
            glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer.getId());
            glViewport(0, 0, frameBuffer.getWidth(), frameBuffer.getHeight());

            //Configure OpenGL
            glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT);
            glDisable(GL_DEPTH_TEST);
            glDisable(GL_CULL_FACE);
            glFrontFace(GL_CW);

            //Set texture
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, string.getFont().getAtlas().getTextureId());

            //Set shader
            ShaderProgram shader = getShader();
            glUseProgram(shader.getProgramId());

            //Set geometry
            try(Mesh mesh = buildMesh(string)){

                //Render
                glBindVertexArray(mesh.getVertexArrayObjectId());
                glDrawElements(GL_TRIANGLES, mesh.getIndexCount(), GL_UNSIGNED_INT, 0);
            }
        }

        return result;
    }

    private static Mesh buildMesh(RenderableString string){
        int charCount = string.getCharacters().length;
        int vertexCount = charCount * 4;

        float[] vertices = new float[vertexCount * 3];
        float[] texCoords = new float[vertexCount * 2];
        float[] normals = new float[vertexCount * 3];
        int[] indices = new int[charCount * 2 * 3];

        //Build vertices, texcoords & indices
        for (int i = 0; i < string.getCharacters().length; i++) {
            RenderableString.CharQuad quad = string.getCharacters()[i];
            setQuadVertices(vertices, i * 4 * 3, quad, (float)string.getWidth(), (float)string.getHeight());
            setQuadTexCoords(texCoords, i * 4 * 2, quad.getCharacter(), string.getFont().getAtlas());
            setQuadTriangleIndices(indices, i);
        }

        //Build normals
        for (int i = 0; i < vertexCount; i++) {
            setVector3f(normals, i*3, 0, 0, 1);
        }

        return Mesh.loadMesh(vertices, texCoords, normals, indices);
    }

    private static void setQuadVertices(float[] vertices, int offset, RenderableString.CharQuad quad, float totalWidth, float totalHeight){
        float x = quad.getX();
        float y = quad.getY();
        float width = quad.getCharacter().width;
        float height = quad.getCharacter().height;

        setVertex(vertices, offset+(3*0), x, y, totalWidth, totalHeight);
        setVertex(vertices, offset+(3*1), x+width, y, totalWidth, totalHeight);
        setVertex(vertices, offset+(3*2), x, y+height, totalWidth, totalHeight);
        setVertex(vertices, offset+(3*3), x+width, y+height, totalWidth, totalHeight);
    }

    private static void setQuadTexCoords(float[] texCoords, int offset, BMFontFile.Char character, Texture atlas){
        float u = transformToTexCoord(character.x, atlas.getSize(0));
        float v = transformToTexCoord(character.y, atlas.getSize(1));
        float s = transformToTexCoord(character.x + character.width, atlas.getSize(0));
        float t = transformToTexCoord(character.y + character.height, atlas.getSize(1));

        setVector2f(texCoords, offset+(2*0), u, v);
        setVector2f(texCoords, offset+(2*1), s, v);
        setVector2f(texCoords, offset+(2*2), u, t);
        setVector2f(texCoords, offset+(2*3), s, t);
    }

    private static void setQuadTriangleIndices(int[] indices, int charI) {
        int offset = charI * 2 * 3; // 2 triangles, 3 vertices per triangle
        indices[offset + 0] = (charI * 4) + 0;
        indices[offset + 1] = (charI * 4) + 1;
        indices[offset + 2] = (charI * 4) + 2;
        indices[offset + 3] = (charI * 4) + 2;
        indices[offset + 4] = (charI * 4) + 1;
        indices[offset + 5] = (charI * 4) + 3;
    }

    private static void setVertex(float[] vertices, int offset, float x, float y, float totalWidth, float totalHeight){
        setVector3f(vertices, offset, transformToNDC(x, totalWidth), transformToNDC(y, totalHeight), 1);
    }

    private static void setVector3f(float[] coordinates, int offset, float x, float y, float z){
        coordinates[offset] = x;
        coordinates[offset+1] = y;
        coordinates[offset+2] = z;
    }

    private static void setVector2f(float[] coordinates, int offset, float x, float y){
        coordinates[offset] = x;
        coordinates[offset+1] = y;
    }

    private static float transformToNDC(float coord, float size){
        return ((coord/size)*2.0f)-1.0f;
    }

    private static float transformToTexCoord(float coord, float size){
        return coord/size;
    }

    private static ShaderProgram getShader(){
        if(shaderProgram == null){
            try(Shader vertexShader = Shader.compileVertexShader(Resources.loadTextResource(VERTEX_SHADER_PATH))){
                try(Shader fragmentShader = Shader.compileFragmentShader(Resources.loadTextResource(FRAGMENT_SHADER_PATH))){
                    shaderProgram = ShaderProgram.link(vertexShader, fragmentShader);
                }
            }
        }

        return shaderProgram;
    }

    @Override
    public void close() {
        instances--;
        if(instances == 0 && shaderProgram != null){
            shaderProgram.close();
            shaderProgram = null;
        }
    }
}
