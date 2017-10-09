package be.kuleuven.cs.robijn.testbed.renderer;

import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;

public class Shader {
    public static Shader loadVertexShader(String source){
        return loadShader(source, GL_VERTEX_SHADER);
    }

    public static Shader loadFragmentShader(String source){
        return loadShader(source, GL_FRAGMENT_SHADER);
    }

    private static Shader loadShader(String source, int shaderType){
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
}
