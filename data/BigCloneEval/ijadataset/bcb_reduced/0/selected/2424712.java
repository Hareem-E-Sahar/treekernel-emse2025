package net.sf.jtracer.util;

import net.sf.jtracer.exception.IncorrectDimensionsException;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Matrix {

    private int width;

    private int height;

    private float[][] elements;

    public Matrix(int height, int width) {
        this.width = width;
        this.height = height;
        this.elements = new float[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                elements[i][j] = 0;
            }
        }
    }

    public Matrix(int height, int width, float[][] elements) throws IncorrectDimensionsException {
        if (elements.length != height || elements[0].length != width) {
            throw new IncorrectDimensionsException();
        }
        this.height = height;
        this.width = width;
        this.elements = elements;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float[][] getElements() {
        return elements;
    }

    public float getElementAt(int i, int j) {
        return elements[i][j];
    }

    public void setElementAt(int i, int j, float value) {
        elements[i][j] = value;
    }

    public void setElements(float[][] elements) {
        this.elements = elements;
    }

    public Matrix multiply(float f) {
        Matrix result = new Matrix(height, width);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result.elements[i][j] = f * elements[i][j];
            }
        }
        return result;
    }

    public Matrix multiply(Matrix m) {
        Matrix result = new Matrix(height, m.width);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < m.width; j++) {
                for (int k = 0; k < width; k++) {
                    result.elements[i][j] += elements[i][k] * m.elements[k][j];
                }
            }
        }
        return result;
    }

    public Matrix add(Matrix m) {
        Matrix result = new Matrix(height, width);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result.elements[i][j] = elements[i][j] + m.elements[i][j];
            }
        }
        return result;
    }

    public Matrix subtract(Matrix m) {
        Matrix result = new Matrix(height, width);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result.elements[i][j] = elements[i][j] - m.elements[i][j];
            }
        }
        return result;
    }

    public Matrix transpose() {
        Matrix result = new Matrix(height, width);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                result.elements[i][j] = elements[j][i];
            }
        }
        return result;
    }

    public float determinant() {
        if (height == 1) {
            return elements[0][0];
        }
        if (height == 2) {
            return elements[0][0] * elements[1][1] - elements[0][1] * elements[1][0];
        }
        float determinant = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.println("cofactor " + i + " " + j + ": " + getCoFactor(i, j));
                determinant += elements[i][j] * getCoFactor(i, j);
            }
        }
        return determinant;
    }

    public float getCoFactor(int a, int b) {
        Matrix reduced = new Matrix(height - 1, width - 1);
        for (int i = 0; i < width; i++) {
            for (int j = 1; j < height; j++) {
                System.arraycopy(elements[j], 0, reduced.elements[j - 1], 0, i);
                System.arraycopy(elements[j], i + 1, reduced.elements[j - 1], i, elements[0].length - i - 1);
            }
        }
        System.out.println(reduced.elements);
        return (float) (reduced.determinant() * Math.pow(-1, a + b));
    }

    public Matrix inverse() {
        return null;
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hb = new HashCodeBuilder().append(height).append(width);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                hb.append(elements[i][j]);
            }
        }
        return hb.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }
        Matrix other = Matrix.class.cast(obj);
        EqualsBuilder eb = new EqualsBuilder().append(height, other.height).append(width, other.width);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                eb.append(elements[i][j], other.elements[i][j]);
            }
        }
        return eb.isEquals();
    }

    public Vector3D toVector() throws IncorrectDimensionsException {
        if (width > 1) {
            throw new IncorrectDimensionsException();
        }
        return new Vector3D(elements[0][0], elements[1][0], elements[2][0]);
    }

    public Vector3D toPoint() throws IncorrectDimensionsException {
        if (width > 1) {
            throw new IncorrectDimensionsException();
        }
        return new Vector3D(elements[0][0], elements[1][0], elements[2][0]);
    }
}
