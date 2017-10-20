package graph;

import java.util.Vector;

/**
 * A basic matrix class. This really belongs in some utility package...
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Id$
 */
public class Matrix implements Cloneable {

    //XXX other matrix types (float, etc.)
    /**
     * The matrix class.
     */
    public int[][] values = null;

    /**
     * Construct a WxH-dimensioned matrix and zero out all of its values.
     *
     * @param w	The width of the matrix.
     * @param h The height of the matrix.
     */
    public Matrix(int w, int h) {
        values = new int[w][h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                values[i][j] = 0;
            }
        }
    }

    /**
     * Make a copy of this matrix.
     *
     * @return The copied matrix.
     */
    public Object clone() {
        int w = values.length;
        int h = values[0].length;
        Matrix m = new Matrix(w, h);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                m.values[i][j] = values[i][j];
            }
        }

        return m;
    }

    /**
     * Return a Vector of int[] eigenvectors. Not yet implemented.
     */
    public Vector eigenVectors() {
        Vector result = new Vector();
        Matrix trans = transpose();
        return null;
    }

    /**
     * Create a new matrix which is the transpose of this matrix.
     */
    public Matrix transpose() {
        int w = values.length;
        int h = values[0].length;

        Matrix dup = new Matrix(h, w);
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                dup.values[j][i] = values[i][j];
            }
        }

        return dup;
    }

    /**
     * Calculate the determinant of the matrix. Unimplemented.
     */
    public int determinant() {
        return 0;
    }

    /**
     * Multiply every element in the matrix by a scalar value.
     *
     * @param scalar	The scaling factor.
     */
    public void multScalar(int scalar) {
        int w = values.length;
        int h = values[0].length;

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                values[i][j] *= scalar;
            }
        }
    }

    /**
     * Print the contents of the matrix into a string.
     */
    public String toString() {
        String s = new String();

        int w = values.length;
        int h = values[0].length;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                s = s + " " + values[i][j];
            }
            s = s + "\n";
        }
        return s;
    }
}
