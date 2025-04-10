package org.dishevelled.matrix.io.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.dishevelled.matrix.Matrix1D;
import org.dishevelled.matrix.io.Matrix1DReader;

/**
 * Abstract reader for matrices of objects in one dimension.
 *
 * @param <E> 1D matrix element type
 * @author  Michael Heuer
 * @version $Revision$ $Date$
 */
public abstract class AbstractMatrix1DReader<E> implements Matrix1DReader<E> {

    /**
     * Parse the specified value to an instance of type <code>E</code>.
     *
     * @param value value to parse
     * @return an instance of type <code>E</code>
     * @throws IOException if an IO error occurs
     */
    protected abstract E parse(String value) throws IOException;

    /**
     * Create and return a new instance of an implementation of Matrix1D.
     *
     * @param size size
     * @param cardinality approximate cardinality
     * @return a new instance of an implementation of Matrix1D
     */
    protected abstract Matrix1D<E> createMatrix1D(long size, int cardinality);

    /** {@inheritDoc} */
    public final Matrix1D<E> read(final File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return read(inputStream);
        } catch (IOException e) {
            throw e;
        } finally {
            MatrixIOUtils.closeQuietly(inputStream);
        }
    }

    /** {@inheritDoc} */
    public final Matrix1D<E> read(final URL url) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("url must not be null");
        }
        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            return read(inputStream);
        } catch (IOException e) {
            throw e;
        } finally {
            MatrixIOUtils.closeQuietly(inputStream);
        }
    }
}
