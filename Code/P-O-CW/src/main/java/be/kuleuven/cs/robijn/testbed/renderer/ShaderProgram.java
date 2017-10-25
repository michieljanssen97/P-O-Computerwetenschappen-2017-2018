package be.kuleuven.cs.robijn.testbed.renderer;

import be.kuleuven.cs.robijn.common.math.Matrix;
import org.joml.Matrix4f;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL21.*;

public class ShaderProgram {
    public static ShaderProgram link(Shader... shaders){
        if(shaders == null || shaders.length == 0){
            throw new IllegalArgumentException("'shaders' can not be null or empty");
        }

        //Create new blank shader program
        int program = glCreateProgram();

        //Attach all shaders to the new program
        for (Shader shader : shaders){
            glAttachShader(program, shader.getShaderId());
        }

        //Link program, merging the list of shaders into one program
        glLinkProgram(program);

        //Check for link errors
        if(glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE){
            throw new RuntimeException("Shader program failed to link:\n" + glGetProgramInfoLog(program));
        }

        return new ShaderProgram(program);
    }

    private final int programId;

    public ShaderProgram(int programId) {
        this.programId = programId;
    }

    public int getProgramId(){
        return programId;
    }

    //
    //UNIFORM VARIABLES
    //Uniform variables are shader variables that have the same value in every instance of the shader program.
    //They are part of the shader program state.
    //

    //Getters
    public float getUniformFloat(String argumentName){
        glUseProgram(programId);
        return glGetUniformf(programId, findUniformLocation(argumentName));
    }

    public float[] getUniformFloatVector(String argumentName, int vectorLength){
        glUseProgram(programId);
        float[] returnValue = new float[vectorLength];
        glGetUniformfv(programId, findUniformLocation(argumentName), returnValue);
        return returnValue;
    }

    public void getUniformFloatVector(String argumentName, float[] targetBuffer){
        glUseProgram(programId);
        glGetUniformfv(programId, findUniformLocation(argumentName), targetBuffer);
    }

    public int getUniformInteger(String argumentName){
        glUseProgram(programId);
        return glGetUniformi(programId, findUniformLocation(argumentName));
    }

    public int[] getUniformIntegerVector(String argumentName, int vectorLength){
        glUseProgram(programId);
        int[] returnValue = new int[vectorLength];
        glGetUniformiv(programId, findUniformLocation(argumentName), returnValue);
        return returnValue;
    }

    public void getUniformIntegerVector(String argumentName, int[] targetBuffer){
        glUseProgram(programId);
        glGetUniformiv(programId, findUniformLocation(argumentName), targetBuffer);
    }

    //Setters
    public void setUniformFloat(String argumentName, float value){
        glUseProgram(programId);
        glUniform1f(findUniformLocation(argumentName), value);
    }

    public void setUniformFloat(String argumentName, float... value){
        glUseProgram(programId);
        final int location = findUniformLocation(argumentName);
        switch (value.length){
            case 2: glUniform2fv(location, value);
            case 3: glUniform3fv(location, value);
            case 4: glUniform4fv(location, value);
            default: throw new IllegalArgumentException("A uniform float vector cannot contain more than 4 elements");
        }
    }

    public void setUniformInteger(String argumentName, int value){
        glUseProgram(programId);
        glUniform1i(findUniformLocation(argumentName), value);
    }

    public void setUniformInteger(String argumentName, int... value){
        glUseProgram(programId);
        final int location = findUniformLocation(argumentName);
        switch (value.length){
            case 2: glUniform2iv(location, value);
            case 3: glUniform3iv(location, value);
            case 4: glUniform4iv(location, value);
            default: throw new IllegalArgumentException("A uniform integer vector cannot contain more than 4 elements");
        }
    }

    public void setUniformMatrix(String argumentName, boolean transpose, Matrix4f matrix){
        glUseProgram(programId);
        final int location = findUniformLocation(argumentName);
        float[] data = new float[16];
        matrix.get(data);
        glUniformMatrix4fv(location, transpose, data);
    }

    public void setUniformMatrix(String argumentName, boolean transpose, Matrix matrix){
        glUseProgram(programId);
        final int location = findUniformLocation(argumentName);
        if(matrix.getRowCount() == matrix.getColumnCount()){
            setUniformSquareMatrix(location, transpose, matrix);
        }else{
            setUniformRectangleMatrix(location, transpose, matrix);
        }
    }

    private void setUniformSquareMatrix(int uniformLocation, boolean transpose, Matrix matrix){
        switch (matrix.getRowCount()){
            case 2:
                glUniformMatrix2fv(uniformLocation, transpose, matrix.getValuesRowLinearized());
                return;
            case 3:
                glUniformMatrix3fv(uniformLocation, transpose, matrix.getValuesRowLinearized());
                return;
            case 4:
                glUniformMatrix4fv(uniformLocation, transpose, matrix.getValuesRowLinearized());
                return;
        }
        throw new IllegalArgumentException("Unsupported matrix size");
    }

    private void setUniformRectangleMatrix(int uniformLocation, boolean transpose, Matrix matrix){
        switch (matrix.getColumnCount()){
            case 2:
                if(matrix.getRowCount() == 3){
                    glUniformMatrix2x3fv(uniformLocation, transpose, matrix.getValuesRowLinearized());
                }else if(matrix.getRowCount() == 4){
                    glUniformMatrix2x4fv(uniformLocation, transpose, matrix.getValuesRowLinearized());
                }
                return;
            case 3:
                if(matrix.getRowCount() == 2){
                    glUniformMatrix3x2fv(uniformLocation, transpose, matrix.getValuesRowLinearized());
                }else if(matrix.getRowCount() == 4){
                    glUniformMatrix3x4fv(uniformLocation, transpose, matrix.getValuesRowLinearized());
                }
                return;
            case 4:
                if(matrix.getRowCount() == 2){
                    glUniformMatrix4x2fv(uniformLocation, transpose, matrix.getValuesRowLinearized());
                }else if(matrix.getRowCount() == 3){
                    glUniformMatrix4x3fv(uniformLocation, transpose, matrix.getValuesRowLinearized());
                }
                return;
        }
        throw new IllegalArgumentException("Unsupported matrix size");
    }

    private int findUniformLocation(String argumentName){
        int location = glGetUniformLocation(programId, argumentName);
        if(location == -1){
            throw new RuntimeException("'"+argumentName+"' is not a uniform variable in this shader program");
        }
        return location;
    }
}
