package org.xith3d.test.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import org.jagatoo.util.streams.LittleEndianDataInputStream;
import org.xith3d.scenegraph.TriangleArray;

public class JCDModelLoader {

    public static final TriangleArray readModelMesh(URL url) throws IOException {
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(new BufferedInputStream(url.openStream()));
        int numTriangles = in.readInt();
        int numVertices = numTriangles * 3;
        TriangleArray geometry = new TriangleArray(numVertices);
        for (int i = 0; i < numVertices; i++) {
            geometry.setNormal(i, in.readFloat(), in.readFloat(), in.readFloat());
            geometry.setCoordinate(i, in.readFloat(), in.readFloat(), in.readFloat());
        }
        in.close();
        geometry.setTextureCoordinate(0, 0, 0f, 0f);
        return (geometry);
    }
}
