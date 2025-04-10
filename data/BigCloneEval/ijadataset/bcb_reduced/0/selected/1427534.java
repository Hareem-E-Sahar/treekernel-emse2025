package org.isakiev.wic.processor.j2kadapter;

import java.util.ArrayList;
import java.util.List;
import org.isakiev.wic.geometry.ArraySurface;
import org.isakiev.wic.geometry.Region;
import org.isakiev.wic.geometry.Surface;
import org.isakiev.wic.j2kfacade.DataAdapter;
import org.isakiev.wic.j2kfacade.J2KProcessor;
import org.isakiev.wic.processor.Processor;

/**
 * J2K processor adapter with double decomposition and symmetric reflection
 * 
 * @author Ruslan Isakiev
 */
class J2KProcessorAdapterWithDoubleDecompositionAndSymmetricReflection implements J2KProcessor {

    private Processor processor;

    private int gap;

    public J2KProcessorAdapterWithDoubleDecompositionAndSymmetricReflection(Processor processor) {
        this.processor = processor;
        this.gap = processor.getMaxFilterWidth();
    }

    private static final double MULTIPLIER = 2;

    public void decompose(DataAdapter data) {
        Region sourceRegion = new Region(0, 0, data.getWidth(), data.getHeight());
        Surface surface = new SurfaceWithSymmetricReflection(new SurfaceAdapter(data, sourceRegion), gap, false, 0);
        printMinMax(surface, 0, data.getWidth());
        List<Surface> decomposedSurfaces = processor.decompose(surface);
        List<List<Surface>> twiceDecomposedSurfaces = new ArrayList<List<Surface>>();
        for (Surface decomposedSurface : decomposedSurfaces) {
            twiceDecomposedSurfaces.add(processor.decompose(decomposedSurface));
        }
        int channelsNumber = processor.getChannelsNumber();
        int horizontalSize = data.getWidth() / channelsNumber;
        int verticalSize = data.getHeight() / channelsNumber;
        for (int cy = 0; cy < channelsNumber; cy++) {
            for (int cx = 0; cx < channelsNumber; cx++) {
                Surface s = twiceDecomposedSurfaces.get(cy).get(cx);
                printMinMax(s, 0, horizontalSize);
                for (int x = 0; x < horizontalSize; x++) {
                    for (int y = 0; y < verticalSize; y++) {
                        int actualX = cx * horizontalSize + x;
                        int actualY = cy * verticalSize + y;
                        if (cy == 1 && cx == 0) {
                            data.setValue(actualX, actualY, s.getValue(x + 1, y) / MULTIPLIER);
                        } else {
                            data.setValue(actualX, actualY, s.getValue(x, y) / MULTIPLIER);
                        }
                    }
                }
            }
        }
    }

    public void reconstruct2(DataAdapter data) {
    }

    public void reconstruct(DataAdapter data) {
        int width = data.getWidth();
        int height = data.getHeight();
        int channelsNumber = processor.getChannelsNumber();
        int horizontalSize = width / channelsNumber;
        int verticalSize = height / channelsNumber;
        Region region = new Region(0, 0, horizontalSize, verticalSize);
        Region region2 = new Region(1, 0, horizontalSize, verticalSize);
        List<List<Surface>> twiceDecomposedSurfaces = new ArrayList<List<Surface>>(channelsNumber);
        for (int cy = 0; cy < channelsNumber; cy++) {
            List<Surface> list = new ArrayList<Surface>();
            twiceDecomposedSurfaces.add(list);
            for (int cx = 0; cx < channelsNumber; cx++) {
                int componentIndex = 2 * cy + cx;
                Surface s = new ArraySurface(cy == 1 && cx == 0 ? region2 : region);
                for (int x = 0; x < horizontalSize; x++) {
                    for (int y = 0; y < verticalSize; y++) {
                        int actualX = cx * horizontalSize + x;
                        int actualY = cy * verticalSize + y;
                        if (cy == 1 && cx == 0) {
                            s.setValue(x + 1, y, data.getValue(actualX, actualY) * MULTIPLIER);
                        } else {
                            s.setValue(x, y, data.getValue(actualX, actualY) * MULTIPLIER);
                        }
                    }
                }
                printMinMax(s, 0, horizontalSize);
                list.add(new SurfaceWithSymmetricReflection(s, gap, true, componentIndex));
            }
        }
        List<Surface> decomposedSurfaces = new ArrayList<Surface>(channelsNumber);
        for (int i = 0; i < channelsNumber; i++) {
            decomposedSurfaces.add(processor.reconstruct(twiceDecomposedSurfaces.get(i)));
        }
        Surface reconstructedSurface = processor.reconstruct(decomposedSurfaces);
        printMinMax(reconstructedSurface, 0, width);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                data.setValue(x, y, reconstructedSurface.getValue(x, y));
            }
        }
    }

    private void printMinMax(Surface surface, int minz, int wz) {
    }
}
