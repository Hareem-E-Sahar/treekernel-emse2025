package org.creavi.engine.compiler;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.creavi.engine.resource.ResourceLocator;
import com.jme.util.export.Savable;
import com.jme.util.export.binary.BinaryExporter;
import com.jme.util.export.binary.BinaryImporter;

public class ResourceCompiler {

    private static final ResourceCompiler resourceCompiler = new ResourceCompiler();

    public static ResourceCompiler getInstance() {
        return resourceCompiler;
    }

    private ResourceCompiler() {
    }

    public Savable loadResource(String name, PrimitiveLoader loader) {
        Savable objeto = null;
        URL url = ResourceLocator.locateFile(loader.getBaseFolder(), name, loader.getCompiledExtension());
        if (url == null) {
            url = ResourceLocator.locateFile(loader.getBaseFolder(), name, loader.getPrimitiveExtension());
            if (url != null) {
                try {
                    objeto = loader.loadResource(name, url.openStream());
                    File file = ResourceLocator.replaceExtension(url, loader.getCompiledExtension());
                    BinaryExporter.getInstance().save(objeto, file);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                objeto = BinaryImporter.getInstance().load(url.openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return objeto;
    }
}
