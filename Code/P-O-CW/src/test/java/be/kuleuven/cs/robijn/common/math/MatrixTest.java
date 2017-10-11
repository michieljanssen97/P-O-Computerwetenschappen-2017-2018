package be.kuleuven.cs.robijn.common.math;

import org.junit.Test;

import static org.junit.Assert.*;

public class MatrixTest {
    @Test
    public void testCreateIdentityMatrix() throws Exception {
        Matrix identity = Matrix.createIdentityMatrix(3);
        assertEquals(identity.getRowCount(), 3);
        assertEquals(identity.getColumnCount(), 3);

        assertEquals(identity.getValue(0, 0), 1f, 1E-6);
        assertEquals(identity.getValue(1, 1), 1f, 1E-6);
        assertEquals(identity.getValue(2, 2), 1f, 1E-6);
        assertEquals(identity.getValue(0, 1), 0f, 1E-6);
        assertEquals(identity.getValue(0, 2), 0f, 1E-6);
        assertEquals(identity.getValue(1, 0), 0f, 1E-6);
        assertEquals(identity.getValue(1, 2), 0f, 1E-6);
        assertEquals(identity.getValue(2, 0), 0f, 1E-6);
        assertEquals(identity.getValue(2, 1), 0f, 1E-6);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIdentityMatrixNotNegativeSize() throws Exception {
        Matrix.createIdentityMatrix(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateIdentityMatrixNotZeroSize() throws Exception {
        Matrix.createIdentityMatrix(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorValuesNotNull() throws Exception {
        new Matrix(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorValuesNotEmpty() throws Exception {
        new Matrix(new float[0][0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorValuesRectangular() throws Exception {
        new Matrix(new float[][]{
                {1, 2, 3, 4},
                {5, 6}
        });
    }

    @Test
    public void testGetRowCount() throws Exception {
        Matrix matrix = new Matrix(new float[2][3]);
        assertEquals(matrix.getRowCount(), 2);
    }

    @Test
    public void testGetColumnCount() throws Exception {
        Matrix matrix = new Matrix(new float[2][3]);
        assertEquals(matrix.getColumnCount(), 3);
    }

    @Test
    public void testGetValue() throws Exception {
        Matrix matrix = new Matrix(new float[][]{
                {1, 2},
                {3, 4}
        });
        assertEquals(matrix.getValue(0, 0), 1, 1E-6);
        assertEquals(matrix.getValue(0, 1), 2, 1E-6);
        assertEquals(matrix.getValue(1, 0), 3, 1E-6);
        assertEquals(matrix.getValue(1, 1), 4, 1E-6);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueNegativeRow() throws Exception {
        Matrix matrix = new Matrix(new float[][]{
                {1, 2},
                {3, 4}
        });
        matrix.getValue(-1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueNegativeColumn() throws Exception {
        Matrix matrix = new Matrix(new float[][]{
                {1, 2},
                {3, 4}
        });
        matrix.getValue(0, -1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueRowOutOfBounds() throws Exception {
        Matrix matrix = new Matrix(new float[][]{
                {1, 2},
                {3, 4}
        });
        matrix.getValue(2, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetValueColumnOutOfBounds() throws Exception {
        Matrix matrix = new Matrix(new float[][]{
                {1, 2},
                {3, 4}
        });
        matrix.getValue(0, 2);
    }

    @Test
    public void testMultiplyByVector() throws Exception {
        Matrix matrix = new Matrix(new float[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });

        Vector3f vector = new Vector3f(5, 6, 7);

        Vector3f product = new Vector3f(38, 92, 146);
        Vector3f calculatedProduct = matrix.multiply(vector);

        assertEquals(product.getX(), calculatedProduct.getX(), 1E-6);
        assertEquals(product.getY(), calculatedProduct.getY(), 1E-6);
        assertEquals(product.getZ(), calculatedProduct.getZ(), 1E-6);
    }

    @Test
    public void testMultiplyByVectorDoesNotChangeMatrix() throws Exception {
        Matrix matrix = new Matrix(new float[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });
        Matrix matrixCopy = new Matrix(new float[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });

        Vector3f calculatedProduct = matrix.multiply(new Vector3f(5, 6, 7));

        assertMatricesEqual(matrix, matrixCopy);
    }

    @Test
    public void testMultiplyByMatrix() throws Exception {
        Matrix matrix1 = new Matrix(new float[][]{
                {1, 2, 3},
                {4, 5, 6},
                {7, 8, 9}
        });

        Matrix matrix2 = new Matrix(new float[][]{
                {6, 5, 4},
                {3, 2, 1},
                {7, 8, 9}
        });

        Matrix product = new Matrix(new float[][]{
                {33, 33, 33},
                {81, 78, 75},
                {129, 123, 117}
        });

        Matrix calculatedProduct = matrix1.multiply(matrix2);

        assertMatricesEqual(product, calculatedProduct);
    }

    @Test
    public void testMatrixNotChangedAfterMultiply(){
        Matrix matrix1 = new Matrix(new float[][]{
                {1, 2},
                {4, 5}
        });

        Matrix matrix1Copy = new Matrix(new float[][]{
                {1, 2},
                {4, 5}
        });

        Matrix matrix2 = new Matrix(new float[][]{
                {6, 5},
                {3, 2}
        });

        Matrix matrix2Copy = new Matrix(new float[][]{
                {6, 5},
                {3, 2}
        });

        matrix1.multiply(matrix2);

        assertMatricesEqual(matrix1, matrix1Copy);
        assertMatricesEqual(matrix2, matrix2Copy);
    }

    @Test
    public void testMatrixSum(){
        Matrix matrix1 = new Matrix(new float[][]{
                {1, 2},
                {4, 5}
        });

        Matrix matrix1Copy = new Matrix(new float[][]{
                {1, 2},
                {4, 5}
        });

        Matrix matrix2 = new Matrix(new float[][]{
                {3, 8},
                {8, 6}
        });

        Matrix matrix2Copy = new Matrix(new float[][]{
                {3, 8},
                {8, 6}
        });

        Matrix sum = new Matrix(new float[][]{
                {4, 10},
                {12, 11}
        });

        Matrix calculatedSum = matrix1.sum(matrix2);
        assertMatricesEqual(calculatedSum, matrix2.sum(matrix1));

        assertMatricesEqual(calculatedSum, sum);

        assertMatricesEqual(matrix1, matrix1Copy);
        assertMatricesEqual(matrix2, matrix2Copy);
    }

    @Test
    public void testMatrixSubtract(){
        Matrix matrix1 = new Matrix(new float[][]{
                {1, 2},
                {4, 5}
        });

        Matrix matrix1Copy = new Matrix(new float[][]{
                {1, 2},
                {4, 5}
        });

        Matrix matrix2 = new Matrix(new float[][]{
                {3, 8},
                {8, 6}
        });

        Matrix matrix2Copy = new Matrix(new float[][]{
                {3, 8},
                {8, 6}
        });

        Matrix difference = new Matrix(new float[][]{
                {-2, -6},
                {-4, -1}
        });

        Matrix calculatedDifference = matrix1.subtract(matrix2);

        assertMatricesEqual(calculatedDifference, difference);

        assertMatricesEqual(matrix1, matrix1Copy);
        assertMatricesEqual(matrix2, matrix2Copy);
    }

    @Test
    public void testMatrixScalarMultiplication(){
        Matrix matrix1 = new Matrix(new float[][]{
                {1, 2},
                {4, 5}
        });

        float scalar = 5f;
        Matrix result = new Matrix(new float[][]{
                {5f, 10f},
                {20f, 25f}
        });

        Matrix calculatedProduct = matrix1.multiply(scalar);

        assertMatricesEqual(matrix1, result);
    }

    private void assertMatricesEqual(Matrix mat1, Matrix mat2){
        assertEquals(mat1.getRowCount(), mat2.getRowCount());
        assertEquals(mat1.getColumnCount(), mat2.getColumnCount());

        for (int row = 0; row < mat1.getRowCount(); row++) {
            for(int col = 0; col < mat2.getColumnCount(); col++) {
                assertEquals(mat1.getValue(row, col), mat2.getValue(row, col), 1E-6);
            }
        }
    }
}