package be.kuleuven.cs.robijn.testbed.renderer;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;

public class Shader implements AutoCloseable{
    public static Shader compileVertexShader(String source){
        return compileShader(source, GL_VERTEX_SHADER);
    }

    public static Shader compileFragmentShader(String source){
        return compileShader(source, GL_FRAGMENT_SHADER);
    }

    private static Shader compileShader(String source, int shaderType){
        //Create shader object
        int shader = glCreateShader(shaderType);
        //Load shader source
        glShaderSource(shader, source);
        //Compile source code to shader program
        glCompileShader(shader);

        if(glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE){
            //Shader failed to compile, get logs
            String log = glGetShaderInfoLog(shader);
            throw new RuntimeException("Shader failed to compile: \n"+log);
        }

        return new Shader(shaderType, shader);
    }

    private final int shaderType;
    private final int shaderId;

    private Shader(int shaderType, int shaderId) {
        this.shaderType = shaderType;
        this.shaderId = shaderId;
    }

    public int getShaderId(){
        return shaderId;
    }

    @Override
    public void close() {
        glDeleteShader(shaderId);
    }
}
