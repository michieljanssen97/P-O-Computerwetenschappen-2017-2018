package be.kuleuven.cs.robijn.testbed.renderer;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;

public class ShaderProgram implements AutoCloseable{
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

    @Override
    public void close() {
        glDeleteProgram(programId);
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
        float[] data = new float[16];
        matrix.get(data);
        setUniformMatrix(argumentName, transpose, 4, 4, data);
    }

    public void setUniformMatrix(String argumentName, boolean transpose, RealMatrix matrix){
        double[][] data;
        if(matrix instanceof Array2DRowRealMatrix){
            data = ((Array2DRowRealMatrix)matrix).getDataRef();
        }else{
            data = matrix.getData();
        }
        int rows = data.length;
        int columns = data[0].length;
        setUniformMatrix(argumentName, transpose, rows, columns, getColumnLinearizedMatrix(data));
    }

    private void setUniformMatrix(String argumentName, boolean transpose, int rows, int columns, float[] values) {
        if(argumentName == null || argumentName.isEmpty()){
           throw new IllegalArgumentException("invalid argumentName: "+argumentName);
        }

        glUseProgram(programId);
        final int location = findUniformLocation(argumentName);
        if(rows == columns){
            setUniformSquareMatrix(location, transpose, rows, values);
        }else{
            setUniformRectangleMatrix(location, transpose, rows, columns, values);
        }
    }

    private void setUniformSquareMatrix(int uniformLocation, boolean transpose, int matrixSize, float[] values){
        switch (matrixSize){
            case 2:
                glUniformMatrix2fv(uniformLocation, transpose, values);
                return;
            case 3:
                glUniformMatrix3fv(uniformLocation, transpose, values);
                return;
            case 4:
                glUniformMatrix4fv(uniformLocation, transpose, values);
                return;
        }
        throw new IllegalArgumentException("Unsupported matrix size");
    }

    private void setUniformRectangleMatrix(int uniformLocation, boolean transpose, int rowCount, int columnCount, float[] values){
        switch (columnCount){
            case 2:
                if(rowCount == 3){
                    glUniformMatrix2x3fv(uniformLocation, transpose, values);
                }else if(rowCount == 4){
                    glUniformMatrix2x4fv(uniformLocation, transpose, values);
                }
                return;
            case 3:
                if(rowCount == 2){
                    glUniformMatrix3x2fv(uniformLocation, transpose, values);
                }else if(rowCount == 4){
                    glUniformMatrix3x4fv(uniformLocation, transpose, values);
                }
                return;
            case 4:
                if(rowCount == 2){
                    glUniformMatrix4x2fv(uniformLocation, transpose, values);
                }else if(rowCount == 3){
                    glUniformMatrix4x3fv(uniformLocation, transpose, values);
                }
                return;
        }
        throw new IllegalArgumentException("Unsupported matrix size");
    }

    /**
     * Takes in a 2D row-major matrix, and outputs a column-linearized matrix.
     * input[row][column] == output[(column * rowCount) + row]
     */
    private float[] getColumnLinearizedMatrix(double[][] input){
        int rowCount = input.length;
        int columnCount = input[0].length;
        float[] rowLinearized = new float[rowCount * columnCount];
        for(int column = 0; column < columnCount; column++){
            for(int row = 0; row < rowCount; row++){
                rowLinearized[(column * rowCount) + row] = (float)input[row][column];
            }
        }
        return rowLinearized;
    }

    private int findUniformLocation(String argumentName){
        int location = glGetUniformLocation(programId, argumentName);
        if(location == -1){
            throw new RuntimeException("'"+argumentName+"' is not a uniform variable in this shader program");
        }
        return location;
    }
}
