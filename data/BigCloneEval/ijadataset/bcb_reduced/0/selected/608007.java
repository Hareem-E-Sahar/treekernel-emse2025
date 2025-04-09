package com.flagstone.transform;

/**
FSCoordTransform is used to specify two-dimensional coordinate transforms, allowing an 
object to be scaled, rotated or moved without changing the original definition of how 
the object is drawn.
 
<p>A two-dimensional transform is defined using a 3x3 matrix and the new values for a pair of coordinates (x,y) are calculated using the following matrix multiplication:</p>

<img src="doc-files/transform.gif">

<p>Different transformations such as scaling, rotation, shearing and translation can be performed using the above matrix multiplication. More complex transformations can be defined by performing successive matrix multiplications in a process known as compositing. This allows a complex transformations to performed on an object.</p>

<p>The FSCoordTransform contains a 3x3 array for defining the transformations. However when it is encoded the matrix is reduced to the following set attributes:</p>

<table class="datasheet">

<tr><th align="left" colspan="2">Attributes</th></tr>

<tr>
<td><a name="FSCoordTransform_0">scaleX</a></td>
<td>The value to scale the shape in the x direction combined with the cosine 
component of any rotation.</td>
</tr>

<tr>
<td><a name="FSCoordTransform_1">scaleY</a></td>
<td>The value to scale the shape in the x direction combined with the cosine 
component of any rotation.</td>
</tr>

<tr>
<td><a name="FSCoordTransform_2">rotate0</a></td>
<td>The sine component of any rotation applied to the shape.</td>
</tr>

<tr>
<td><a name="FSCoordTransform_3">rotate1</a></td>
<td>The negative sine component of any rotation applied to the shape.</td>
</tr>

<tr>
<td><a name="FSCoordTransform_4">translateX</a></td>
<td>The x-coordinate of any translation applied to the shape.</td>
</tr>

<tr>
<td><a name="FSCoordTransform_5">translateY</a></td>
<td>The y-coordinate of any translation applied to the shape.</td>
</tr>

</table>

<h2 class="datasheet">Examples</h2>

<p>The FSCoordTransform provides a set of methods for generating the matrices that will perform specific transformations. Methods are provided that represent matrices for performing translation, scaling, rotation and shearing transformations.</p>

<pre>
FSCoordTransform = new FSCoordTransform();

transform.scale(2.0, 2.0); // scale(x,y)
transform.rotate(30.0);  // rotate(degrees)
transform.shear(1.2, 0.9);  // shear(x, y)
</pre>

<p>The composite method can be used to multiply two matrices together to create complex transformations though successive compositing steps. For example to place a new object on the screen first rotating it by 30 degrees and scaling it to twice its original size the required transform can be constructed using the following steps:</p>

<pre>
FSCoordTransform transform = new FSCoordTranform();

transform.scale(2.0, 2.0);
transform.rotate(30.0);

int layer = 1;
int identifier = movie.newIdentifier();

FSDefineShape shape = new FSDefineShape(identifier, ...);

FSPlaceObject2 placeShape = new FSPlaceObject2(identifier, layer, transform);
</pre>

<p>Compositing transforms are not commutative, the order in which transformations are applied will affect the final result. For example consider the following pair if transforms:</p>

FSCoordTransform transform = new FSCoordTransform();

transform.translate(100, 100);
transform.scale(2.0, 2.0);

<p>The composite transform places an object at the coordinates (100,100) then scales it to twice its original size. If the transform was composited in the opposite order:</p>

<pre>
FSCoordTransform transform = new FSCoordTransform();

transform.scale(2.0, 2.0);
transform.translate(100, 100);
</pre>     

<p>Then the coordinates for the object's location would also be scaled, placing the object at (200,200).</p>

<p>Arbitrary coordinate transforms are created by specifying the 3 by 3 array of floating-point values in the constructor:</p>

<pre>
float[][] matrix = new float[][] {
    {0.923f, 0.321f, 1000.0f}, 
    {0.868f, 0.235f, 1000.0f}, 
    {0.000f, 0.000f, 1.0000f}
};

FSCoordTransform transform = new FSCoordTransform(matrix);
</pre>

<p>A constructor is also provided to handle the most common composite transform - scaling and translating an object at the same time:</p>

<pre>
FSCoordTransform composite = new FSCoordTransform(100, 150, 2.0, 2.0);
</pre>

<p>Will place the object at the twip coordinates (100, 150) and scale the object to twice its original size.</P>

<h1 class="datasheet">History</h1>

<p>The FSCoordTransform class represents the Matrix data structure from the Macromedia Flash (SWF) File Format Specification. It was introduced in Flash 1.</p>
 */
public class FSCoordTransform extends FSTransformObject {

    private float[][] matrix = new float[][] { { 1.0f, 0.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { 0.0f, 0.0f, 1.0f } };

    /**
     * Construct an FSCoordTransform object and initialize it with values decoded 
     * from a binary encoded FSCoordTransform object.
     * 
     * @param coder an FSCoder object containing an FSColor encoded as binary
     * data.
     */
    public FSCoordTransform(FSCoder coder) {
        decode(coder);
    }

    /** 
     * Constructs an FSCoordTransform object defining a unity transform. If the 
     * transform is applied to a shape its location or appearance will not change.
     */
    public FSCoordTransform() {
    }

    /** 
     * Constructs an FSCoordTransform object defining a translation transform 
     * that will change an objects location to the specified coordinates. 
     * 
     * @param x the x-coordinate where the object will be displayed.
     * @param y the y-coordinate where the object will be displayed.
     */
    public FSCoordTransform(int x, int y) {
        float xValue = (float) x;
        float yValue = (float) y;
        matrix[0][2] = xValue;
        matrix[1][2] = yValue;
    }

    /** 
     * Constructs an FSCoordTransform object defining translation and scaling transforms
     * that will change an object's location and size.
    
        @param x the x-coordinate where the object will be displayed.
        @param y the y-coordinate where the object will be displayed.
        @param scaleX value to scale the object in the x direction.
        @param scaleY value to scale the object in the y direction.
        */
    public FSCoordTransform(int x, int y, double scaleX, double scaleY) {
        matrix[0][0] = (float) scaleX;
        matrix[1][1] = (float) scaleY;
        matrix[0][2] = (float) x;
        matrix[1][2] = (float) y;
    }

    /** Constructs an FSCoordTransform object with the specified transformation matrix.
    
        @param aMatrix a 3x3 array of floats containing the values defining the transform.
        */
    public FSCoordTransform(float[][] aMatrix) {
        setMatrix(aMatrix);
    }

    /**
     * Construct an FSCoordTransform object by copying an existing object.
     */
    public FSCoordTransform(FSCoordTransform obj) {
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) matrix[i][j] = obj.matrix[i][j];
    }

    /** Sets the translation points of the transform.
     * 
     * @param x the x-coordinate where the object will be displayed.
     * @param y the y-coordinate where the object will be displayed.
     */
    public void translate(int x, int y) {
        float[][] m = new float[][] { { 1.0f, 0.0f, (float) x }, { 0.0f, 1.0f, (float) y }, { 0.0f, 0.0f, 1.0f } };
        composite(m);
    }

    /** Sets the scaling factor for the transform.
     * 
     * @param x value to scale the object in the x direction.
     * @param y value to scale the object in the y direction.
     */
    public void scale(double x, double y) {
        float[][] m = new float[][] { { (float) x, 0.0f, 0.0f }, { 0.0f, (float) y, 0.0f }, { 0.0f, 0.0f, 1.0f } };
        composite(m);
    }

    /** Sets the angle which the transform will rotate an object.
     * 
     * @param angle value, in degrees, to rotate the object clockwise.
     */
    public void rotate(double angle) {
        float[][] m = new float[][] { { 1.0f, 0.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { 0.0f, 0.0f, 1.0f } };
        m[0][0] = (float) Math.cos(Math.toRadians(angle));
        m[0][1] = -(float) Math.sin(Math.toRadians(angle));
        m[1][0] = (float) Math.sin(Math.toRadians(angle));
        m[1][1] = (float) Math.cos(Math.toRadians(angle));
        composite(m);
    }

    /** Sets the shearing factor for the transform.
     * 
     * @param x value to shear the object in the x direction.
     * @param y value to shear the object in the y direction.
     */
    public void shear(double x, double y) {
        float[][] m = new float[][] { { 1.0f, (float) y, 0.0f }, { (float) x, 1.0f, 0.0f }, { 0.0f, 0.0f, 1.0f } };
        composite(m);
    }

    /**
     * Applies the transformation to the coordinates of a point.
     * 
     * @param x x-coordinate of a point.
     * @param y x-coordinate of a point.
     * @return an array containing the transformed point.
     */
    public int[] transformPoint(int x, int y) {
        float[] point = new float[] { (float) x, (float) y, 1.0f };
        int[] result = new int[2];
        result[0] = (int) (matrix[0][0] * point[0] + matrix[0][1] * point[1] + matrix[0][2] * point[2]);
        result[1] = (int) (matrix[1][0] * point[0] + matrix[1][1] * point[1] + matrix[1][2] * point[2]);
        return result;
    }

    /** Gets the 3 X 3 array that is used to store the transformation values.
    
        @return an array, float[3][3], containing the values for the transformation matrix.
        */
    public float[][] getMatrix() {
        return matrix;
    }

    /** Sets the values in the 3 X 3 array that is used to store the transformation values. 
    
        @param aMatrix a 3x3 array of floats containing the values defining the transform.
        */
    public void setMatrix(float[][] aMatrix) {
        for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) matrix[i][j] = aMatrix[i][j];
    }

    /** Composite the 3 X 3 matrix with the FSCoordTransform argument. This method is used to create multiple transformation effects that can be applied to an object in a single step. Using the instance method fixes the order in which the transforms are composited. Since matrix multiplication is not commutative this limits the number of complex transforms that can be generated when compared to the class method.

        @param transform an FSCoordTransform object to composite with this instance.
        */
    public void composite(FSCoordTransform transform) {
        composite(transform.getMatrix());
    }

    /** 
     * Returns true if anObject is equal to this one. Objects are considered 
     * equal if they would generate identical binary data when they are encoded 
     * to a Flash file.
     *
     * @return true if this object would be identical to anObject when encoded.
     */
    public boolean equals(Object anObject) {
        boolean result = false;
        if (super.equals(anObject)) {
            FSCoordTransform typedObject = (FSCoordTransform) anObject;
            float m[][] = typedObject.matrix;
            result = true;
            for (int i = 0; i < 3; i++) for (int j = 0; j < 3; j++) if (matrix[i][j] != m[i][j]) result = false;
        }
        return result;
    }

    public void appendDescription(StringBuffer buffer, int depth) {
        buffer.append(name());
        if (depth > 0) {
            buffer.append(": { ");
            buffer.append("[ ");
            buffer.append("[" + matrix[0][0] + ", " + matrix[0][1] + ", " + matrix[0][2] + "], ");
            buffer.append("[" + matrix[1][0] + ", " + matrix[1][1] + ", " + matrix[1][2] + "], ");
            buffer.append("[" + matrix[2][0] + ", " + matrix[2][1] + ", " + matrix[2][2] + "] ");
            buffer.append("]; ");
            buffer.append("}");
        }
    }

    public int length(FSCoder coder) {
        int numberOfBits = 7 + translateFieldSize() * 2;
        if (containsScaling()) numberOfBits += 5 + scaleFieldSize() * 2;
        if (containsRotation()) numberOfBits += 5 + rotateFieldSize() * 2;
        numberOfBits += (numberOfBits % 8 > 0) ? 8 - (numberOfBits % 8) : 0;
        return numberOfBits >> 3;
    }

    public void encode(FSCoder coder) {
        int translateBits = translateFieldSize();
        coder.alignToByte();
        coder.writeBits(containsScaling() ? 1 : 0, 1);
        if (containsScaling()) {
            int scaleBits = scaleFieldSize();
            coder.writeBits(scaleBits, 5);
            coder.writeFixedBits(matrix[0][0], scaleBits, 16);
            coder.writeFixedBits(matrix[1][1], scaleBits, 16);
        }
        coder.writeBits(containsRotation() ? 1 : 0, 1);
        if (containsRotation()) {
            int rotateBits = rotateFieldSize();
            coder.writeBits(rotateBits, 5);
            coder.writeFixedBits(matrix[1][0], rotateBits, 16);
            coder.writeFixedBits(matrix[0][1], rotateBits, 16);
        }
        coder.writeBits(translateBits, 5);
        coder.writeBits((int) matrix[0][2], translateBits);
        coder.writeBits((int) matrix[1][2], translateBits);
        coder.alignToByte();
    }

    public void decode(FSCoder coder) {
        int scaleFieldSize = 0;
        int rotateFieldSize = 0;
        int translateFieldSize = 0;
        coder.alignToByte();
        boolean _containsScaling = coder.readBits(1, false) != 0 ? true : false;
        if (_containsScaling) {
            scaleFieldSize = coder.readBits(5, false);
            matrix[0][0] = coder.readFixedBits(scaleFieldSize, 16);
            matrix[1][1] = coder.readFixedBits(scaleFieldSize, 16);
        }
        boolean _containsRotation = coder.readBits(1, false) != 0 ? true : false;
        if (_containsRotation) {
            rotateFieldSize = coder.readBits(5, false);
            matrix[1][0] = coder.readFixedBits(rotateFieldSize, 16);
            matrix[0][1] = coder.readFixedBits(rotateFieldSize, 16);
        }
        translateFieldSize = coder.readBits(5, false);
        matrix[0][2] = (float) coder.readBits(translateFieldSize, true);
        matrix[1][2] = (float) coder.readBits(translateFieldSize, true);
        coder.alignToByte();
    }

    /** Returns true if the values in the transformation matrix represent a unity transform - one which will not change the physical appearance or location of a shape.

        @return true if the object represents a unity transform, false otherwise.
        */
    public boolean isUnityTransform() {
        return !(containsScaling() || containsRotation() || containsTranslation());
    }

    private boolean containsScaling() {
        return matrix[0][0] != 1.0f || matrix[1][1] != 1.0f;
    }

    private boolean containsRotation() {
        return matrix[1][0] != 0.0f || matrix[0][1] != 0.0f;
    }

    private boolean containsTranslation() {
        return matrix[0][2] != 0.0f || matrix[1][2] != 0.0f;
    }

    private int scaleFieldSize() {
        int size = 0;
        if (isUnityTransform() == false) size = FSCoder.fixedSize(new float[] { matrix[0][0], matrix[1][1] });
        return size;
    }

    private int rotateFieldSize() {
        int size = FSCoder.fixedSize(new float[] { matrix[1][0], matrix[0][1] });
        return size;
    }

    private int translateFieldSize() {
        int size = 0;
        if (containsTranslation()) size = FSCoder.size(new int[] { (int) matrix[0][2], (int) matrix[1][2] }, true);
        return size;
    }

    private void composite(float[][] m) {
        float result[][] = new float[3][3];
        result[0][0] = matrix[0][0] * m[0][0] + matrix[0][1] * m[1][0] + matrix[0][2] * m[2][0];
        result[0][1] = matrix[0][0] * m[0][1] + matrix[0][1] * m[1][1] + matrix[0][2] * m[2][1];
        result[0][2] = matrix[0][0] * m[0][2] + matrix[0][1] * m[1][2] + matrix[0][2] * m[2][2];
        result[1][0] = matrix[1][0] * m[0][0] + matrix[1][1] * m[1][0] + matrix[1][2] * m[2][0];
        result[1][1] = matrix[1][0] * m[0][1] + matrix[1][1] * m[1][1] + matrix[1][2] * m[2][1];
        result[1][2] = matrix[1][0] * m[0][2] + matrix[1][1] * m[1][2] + matrix[1][2] * m[2][2];
        result[2][0] = matrix[2][0] * m[0][0] + matrix[2][1] * m[1][0] + matrix[2][2] * m[2][0];
        result[2][1] = matrix[2][0] * m[0][1] + matrix[2][1] * m[1][1] + matrix[2][2] * m[2][1];
        result[2][2] = matrix[2][0] * m[0][2] + matrix[2][1] * m[1][2] + matrix[2][2] * m[2][2];
        matrix[0][0] = result[0][0];
        matrix[0][1] = result[0][1];
        matrix[0][2] = result[0][2];
        matrix[1][0] = result[1][0];
        matrix[1][1] = result[1][1];
        matrix[1][2] = result[1][2];
        matrix[2][0] = result[2][0];
        matrix[2][1] = result[2][1];
        matrix[2][2] = result[2][2];
    }
}
