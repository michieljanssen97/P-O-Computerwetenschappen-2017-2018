package be.kuleuven.cs.robijn.common.math;

import java.util.Arrays;

/**
 * This class represents an immutable matrix.
 */
public class Matrix {
    /**
     * Creates a new square matrix with the specified size,
     * filled with zeroes except on the diagonal where the value is 1.
     * @param size the size of the new matrix
     * @return a sizeXsize identity matrix
     */
    public static Matrix createIdentityMatrix(int size){
        if(size <= 0){
            throw new IllegalArgumentException("The matrix size must be greater than zero");
        }

        float[][] values = new float[size][size];
        for (int i = 0; i < size; i++){
            values[i][i] = 1;
        }
        return new Matrix(values);
    }

    private final float[][] values;
    //[row][column]
    public Matrix(float[][] values){
        if(values == null){
            throw new IllegalArgumentException("'values' cannot be null");
        }

        if(values.length == 0){
            throw new IllegalArgumentException("Unsupported matrix size 0");
        }

        for (float[] row : values){
            if(row.length != values[0].length){
                throw new IllegalArgumentException("Each row in the matrix should have the same amount of columns.");
            }
        }

        this.values = values;
    }

    public int getRowCount(){
        return values.length;
    }

    public int getColumnCount(){
        return values[0].length;
    }

    public float getValue(int row, int column){
        if(row < 0 || row >= getRowCount()){
            throw new IndexOutOfBoundsException("The specified row index "+row+" is not in the matrix bounds");
        }

        if(column < 0 || column >= getColumnCount()){
            throw new IndexOutOfBoundsException("The specified column index "+column+" is not in the matrix bounds");
        }

        return values[row][column];
    }

    public Matrix sum(Matrix matrix){
        if(this.getRowCount() != matrix.getRowCount() || this.getColumnCount() != matrix.getColumnCount()){
            throw new IllegalArgumentException("The dimensions of the matrices must be equal");
        }

        float[][] newValues = new float[this.getRowCount()][this.getColumnCount()];
        for (int row = 0; row < this.getRowCount(); row++){
            for (int column = 0; column < this.getColumnCount(); column++){
                newValues[row][column] = values[row][column] + matrix.values[row][column];
            }
        }

        return new Matrix(newValues);
    }

    public Matrix subtract(Matrix matrix){
        if(this.getRowCount() != matrix.getRowCount() || this.getColumnCount() != matrix.getColumnCount()){
            throw new IllegalArgumentException("The dimensions of the matrices must be equal");
        }

        float[][] newValues = new float[this.getRowCount()][this.getColumnCount()];
        for (int row = 0; row < this.getRowCount(); row++){
            for (int column = 0; column < this.getColumnCount(); column++){
                newValues[row][column] = values[row][column] - matrix.values[row][column];
            }
        }

        return new Matrix(newValues);
    }

    public Matrix multiply(float scalar){
        float[][] newValues = new float[this.getRowCount()][this.getColumnCount()];
        for (int row = 0; row < this.getRowCount(); row++){
            for (int column = 0; column < this.getColumnCount(); column++){
                newValues[row][column] = values[row][column] * scalar;
            }
        }

        return new Matrix(newValues);
    }

    public Vector3f multiply(Vector3f vector){
        if(getColumnCount() != 3){
            throw new IllegalArgumentException("The matrix must have 3 columns to multiply it with a vector of size 3");
        }

        float[] result = new float[3];
        for (int row = 0; row < getRowCount(); row++) {
            result[row] = vector.getX() * getValue(row, 0)
                    + vector.getY() * getValue(row, 1)
                    + vector.getZ() * getValue(row, 2);
        }

        return new Vector3f(
                result[0], result[1], result[2]
        );
    }

    /**
     * Performs matrix multiplication.
     * Assuming this matrix is A and the matrix in the parameter is B,
     * this function returns A*B.
     * @param matrix the matrix to multiply this matrix by.
     * @return the result of the multiplication.
     */
    public Matrix multiply(Matrix matrix){
        if(matrix == null){
            throw new IllegalArgumentException("'matrix' cannot be null");
        }

        if(this.getColumnCount() != matrix.getRowCount()){
            throw new IllegalArgumentException("The specified matrix must have an amount of rows equal to the amount of columns in this matrix");
        }

        float[][] newValues = new float[matrix.getRowCount()][getColumnCount()];
        for (int i = 0; i < this.getRowCount(); i++) {
            for (int j = 0; j < matrix.getColumnCount(); j++) {
                for (int k = 0; k < this.getRowCount(); k++) {
                    newValues[i][j] += this.values[i][k] * matrix.values[k][j];
                }
            }
        }
        return new Matrix(newValues);
    }

    public String toString(){
        String str = "[";
        for(float[] row : values){
            str += Arrays.toString(row);
            str += ", ";
        }
        str += "]";
        return str;
    }
}