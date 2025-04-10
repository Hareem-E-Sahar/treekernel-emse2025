package org.remus.infomngmnt.ui.calendar.internal;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Utility class for managing OS resources associated with SWT/JFace controls
 * such as colors, fonts, images, etc.
 * 
 * !!! IMPORTANT !!! Application code must explicitly invoke the
 * <code>dispose()</code> method to release the operating system resources
 * managed by cached objects when those objects and OS resources are no longer
 * needed (e.g. on application shutdown)
 * 
 * This class may be freely distributed as part of any application or plugin.
 * <p>
 * Copyright (c) 2003 - 2005, Instantiations, Inc. <br>
 * All Rights Reserved
 * 
 * @author scheglov_ke
 * @author Dan Rubel
 */
public class ResourceManager extends SWTResourceManager {

    public static final String PLUGIN_ID = "org.remus.infomngmnt.ui.widgets.databinding";

    /**
	 * Dispose of cached objects and their underlying OS resources. This should
	 * only be called when the cached objects are no longer needed (e.g. on
	 * application shutdown)
	 */
    public static void dispose() {
        disposeColors();
        disposeFonts();
        disposeImages();
        disposeCursors();
    }

    /**
	 * Maps image descriptors to images
	 */
    private static HashMap<ImageDescriptor, Image> m_DescriptorImageMap = new HashMap<ImageDescriptor, Image>();

    /**
	 * Maps images to image decorators
	 */
    private static HashMap<Image, HashMap<Image, Image>> m_ImageToDecoratorMap = new HashMap<Image, HashMap<Image, Image>>();

    /**
	 * Returns an image descriptor stored in the file at the specified path
	 * relative to the specified class
	 * 
	 * @param clazz
	 *            Class The class relative to which to find the image descriptor
	 * @param path
	 *            String The path to the image file
	 * @return ImageDescriptor The image descriptor stored in the file at the
	 *         specified path
	 */
    public static ImageDescriptor getImageDescriptor(final Class<?> clazz, final String path) {
        return ImageDescriptor.createFromFile(clazz, path);
    }

    /**
	 * Returns an image descriptor stored in the file at the specified path
	 * 
	 * @param path
	 *            String The path to the image file
	 * @return ImageDescriptor The image descriptor stored in the file at the
	 *         specified path
	 */
    public static ImageDescriptor getImageDescriptor(final String path) {
        try {
            return ImageDescriptor.createFromURL((new File(path)).toURI().toURL());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
	 * Returns an image based on the specified image descriptor
	 * 
	 * @param descriptor
	 *            ImageDescriptor The image descriptor for the image
	 * @return Image The image based on the specified image descriptor
	 */
    public static Image getImage(final ImageDescriptor descriptor) {
        if (descriptor == null) return null;
        Image image = m_DescriptorImageMap.get(descriptor);
        if (image == null) {
            image = descriptor.createImage();
            m_DescriptorImageMap.put(descriptor, image);
        }
        return image;
    }

    /**
	 * Returns an image composed of a base image decorated by another image
	 * 
	 * @param baseImage
	 *            Image The base image that should be decorated
	 * @param decorator
	 *            Image The image to decorate the base image
	 * @param corner
	 *            The corner to place decorator image
	 * @return Image The resulting decorated image
	 */
    public static Image decorateImage(final Image baseImage, final Image decorator, final int corner) {
        HashMap<Image, Image> decoratedMap = m_ImageToDecoratorMap.get(baseImage);
        if (decoratedMap == null) {
            decoratedMap = new HashMap<Image, Image>();
            m_ImageToDecoratorMap.put(baseImage, decoratedMap);
        }
        Image result = decoratedMap.get(decorator);
        if (result == null) {
            final Rectangle bid = baseImage.getBounds();
            final Rectangle did = decorator.getBounds();
            final Point baseImageSize = new Point(bid.width, bid.height);
            CompositeImageDescriptor compositImageDesc = new CompositeImageDescriptor() {

                @Override
                protected void drawCompositeImage(final int width, final int height) {
                    drawImage(baseImage.getImageData(), 0, 0);
                    if (corner == TOP_LEFT) {
                        drawImage(decorator.getImageData(), 0, 0);
                    } else if (corner == TOP_RIGHT) {
                        drawImage(decorator.getImageData(), bid.width - did.width - 1, 0);
                    } else if (corner == BOTTOM_LEFT) {
                        drawImage(decorator.getImageData(), 0, bid.height - did.height - 1);
                    } else if (corner == BOTTOM_RIGHT) {
                        drawImage(decorator.getImageData(), bid.width - did.width - 1, bid.height - did.height - 1);
                    }
                }

                @Override
                protected Point getSize() {
                    return baseImageSize;
                }
            };
            result = compositImageDesc.createImage();
            decoratedMap.put(decorator, result);
        }
        return result;
    }

    /**
	 * Dispose all of the cached images
	 */
    public static void disposeImages() {
        SWTResourceManager.disposeImages();
        {
            for (Iterator<Image> I = m_DescriptorImageMap.values().iterator(); I.hasNext(); ) I.next().dispose();
            m_DescriptorImageMap.clear();
        }
        {
            for (Iterator<Image> I = m_URLImageMap.values().iterator(); I.hasNext(); ) I.next().dispose();
            m_URLImageMap.clear();
        }
    }

    /**
	 * Maps URL to images
	 */
    private static HashMap<URL, Image> m_URLImageMap = new HashMap<URL, Image>();

    /**
	 * Retuns an image based on a plugin and file path
	 * 
	 * @param plugin
	 *            Object The plugin containing the image
	 * @param name
	 *            String The path to th eimage within the plugin
	 * @return Image The image stored in the file at the specified path
	 */
    public static Image getPluginImage(final Object plugin, final String name) {
        try {
            try {
                URL url = getPluginImageURL(plugin, name);
                if (m_URLImageMap.containsKey(url)) return m_URLImageMap.get(url);
                InputStream is = url.openStream();
                Image image;
                try {
                    image = getImage(is);
                    m_URLImageMap.put(url, image);
                } finally {
                    is.close();
                }
                return image;
            } catch (Throwable e) {
            }
        } catch (Throwable e) {
        }
        return null;
    }

    /**
	 * Retuns an image descriptor based on a plugin and file path
	 * 
	 * @param plugin
	 *            Object The plugin containing the image
	 * @param name
	 *            String The path to th eimage within the plugin
	 * @return ImageDescriptor The image descriptor stored in the file at the
	 *         specified path
	 */
    public static ImageDescriptor getPluginImageDescriptor(final Object plugin, final String name) {
        try {
            try {
                URL url = getPluginImageURL(plugin, name);
                return ImageDescriptor.createFromURL(url);
            } catch (Throwable e) {
            }
        } catch (Throwable e) {
        }
        return null;
    }

    /**
	 * Retuns an URL based on a plugin and file path
	 * 
	 * @param plugin
	 *            Object The plugin containing the file path
	 * @param name
	 *            String The file path
	 * @return URL The URL representing the file at the specified path
	 * @throws Exception
	 */
    private static URL getPluginImageURL(final Object plugin, final String name) throws Exception {
        try {
            Class<?> bundleClass = Class.forName("org.osgi.framework.Bundle");
            Class<?> bundleContextClass = Class.forName("org.osgi.framework.BundleContext");
            if (bundleContextClass.isAssignableFrom(plugin.getClass())) {
                Method getBundleMethod = bundleContextClass.getMethod("getBundle", new Class[0]);
                Object bundle = getBundleMethod.invoke(plugin, new Object[0]);
                Class<?> ipathClass = Class.forName("org.eclipse.core.runtime.IPath");
                Class<?> pathClass = Class.forName("org.eclipse.core.runtime.Path");
                Constructor<?> pathConstructor = pathClass.getConstructor(new Class[] { String.class });
                Object path = pathConstructor.newInstance(new Object[] { name });
                Class<?> platformClass = Class.forName("org.eclipse.core.runtime.Platform");
                Method findMethod = platformClass.getMethod("find", new Class[] { bundleClass, ipathClass });
                return (URL) findMethod.invoke(null, new Object[] { bundle, path });
            }
        } catch (Throwable e) {
        }
        {
            Class<?> pluginClass = Class.forName("org.eclipse.core.runtime.Plugin");
            if (pluginClass.isAssignableFrom(plugin.getClass())) {
                Class<?> ipathClass = Class.forName("org.eclipse.core.runtime.IPath");
                Class<?> pathClass = Class.forName("org.eclipse.core.runtime.Path");
                Constructor<?> pathConstructor = pathClass.getConstructor(new Class[] { String.class });
                Object path = pathConstructor.newInstance(new Object[] { name });
                Method findMethod = pluginClass.getMethod("find", new Class[] { ipathClass });
                return (URL) findMethod.invoke(plugin, new Object[] { path });
            }
        }
        return null;
    }
}
