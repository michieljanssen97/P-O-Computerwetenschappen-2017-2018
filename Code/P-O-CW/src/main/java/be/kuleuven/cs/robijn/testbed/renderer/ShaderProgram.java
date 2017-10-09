package be.kuleuven.cs.robijn.testbed.renderer;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

public class ShaderProgram {
    public static ShaderProgram createProgram(Shader vertexShader, Shader fragmentShader){
        int program = glCreateProgram();
        glAttachShader(program, vertexShader.getShaderId());
        glAttachShader(program, fragmentShader.getShaderId());
        //glBindFragDataLocation(shaderProgram, 0, "outColor");

        glLinkProgram(program);

        if(glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE){
            throw new RuntimeException("Shader program failed to link:\n" + glGetProgramInfoLog(program));
        }

        int posAttr = glGetAttribLocation(program, "vertexPos_modelspace");

        glVertexAttribPointer(posAttr, 3, GL_FLOAT, false,0, 0);
        glEnableVertexAttribArray(posAttr);

        return new ShaderProgram(program);
    }

    private final int programId;

    public ShaderProgram(int programId) {
        this.programId = programId;
    }

    public int getProgramId(){
        return programId;
    }
}
