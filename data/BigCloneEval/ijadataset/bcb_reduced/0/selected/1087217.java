package internal.reflect;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.fileupload.FileItem;
import org.lightcommons.logger.Logger;
import org.lightcommons.util.ConvertUtils;
import org.lightcommons.util.ReflectionUtils;
import org.lightmtv.config.MtvConvert;
import org.lightmtv.request.CommonsFileUpload;

/**
 * @author GL
 */
public class DefaultTypeConverter extends AbstractTypeConverter {

    public DefaultTypeConverter() {
        this(new ArrayList<MtvConvert>());
    }

    public DefaultTypeConverter(List<MtvConvert> mtvConverts) {
        super(mtvConverts);
    }

    private static Map<Class<?>, Method> valueOfMethods = new HashMap<Class<?>, Method>();

    @Override
    protected Object convertFromString(String value, Type toType) {
        Class toClass = ReflectionUtils.getRawClass(toType);
        toClass = ReflectionUtils.getPrimWrap(toClass);
        if (Date.class.equals(toClass)) {
            try {
                if (value == null || value.trim().length() == 0) return null;
                return ConvertUtils.dateOf(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (Boolean.class.equals(toClass)) {
            String lowwer = value.toLowerCase();
            if ("true".equals(lowwer) || "on".equals(lowwer) || "yes".equals(lowwer)) {
                return Boolean.TRUE;
            } else if ("null".equals(lowwer) || "n/a".equals(lowwer) || "nil".equals(lowwer)) {
                return null;
            } else {
                return Boolean.FALSE;
            }
        }
        Method valueOf = null;
        if (!valueOfMethods.containsKey(toClass)) {
            try {
                valueOf = toClass.getMethod("valueOf", String.class);
                if (valueOf != null && Modifier.isPublic(valueOf.getModifiers()) && Modifier.isStatic(valueOf.getModifiers())) {
                    valueOfMethods.put(toClass, valueOf);
                } else {
                    valueOf = null;
                    valueOfMethods.put(toClass, null);
                }
            } catch (Throwable e1) {
            }
        } else {
            valueOf = valueOfMethods.get(toClass);
        }
        if (valueOf != null) {
            try {
                Object ret = valueOf.invoke(null, value);
                if (ret != null) return ret;
            } catch (Throwable e) {
            }
        }
        return super.convertFromString(value, toType);
    }

    @Override
    protected Object convertFromAny(Object value, Class toClass) {
        try {
            if (value instanceof FileItem) {
                if (((FileItem) value).getFieldName() == null || ((FileItem) value).getFieldName().length() == 0) return null;
                if (toClass.isAssignableFrom(CommonsFileUpload.class)) {
                    return new CommonsFileUpload((FileItem) value);
                }
            }
            WrapValue wv = new WrapValue(value);
            InputStream is = wv.openStream();
            if (toClass.equals(String.class)) {
                byte[] bytes = new byte[is.available()];
                is.read(bytes);
                return new String(bytes);
            } else if (toClass.equals(byte[].class)) {
                byte[] bytes = new byte[is.available()];
                is.read(bytes);
                return bytes;
            } else if (toClass.isAssignableFrom(BufferedImage.class)) {
                return ImageIO.read(is);
            } else if (toClass.isAssignableFrom(InputStream.class)) {
                return is;
            } else if (toClass.isAssignableFrom(File.class)) {
                String name = wv.getName() == null ? "mice" : wv.getName().replace('\\', '/');
                int i = name.lastIndexOf('/');
                if (i > 0) name = name.substring(i + 1);
                int x = name.lastIndexOf('.');
                String perf = name;
                String suff = null;
                if (x > 0) {
                    perf = name.substring(0, x);
                    suff = name.substring(x);
                }
                File file = File.createTempFile(perf, suff);
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    fos.write(bytes, 0, len);
                }
                return file;
            }
            return ConvertUtils.convert(value, toClass);
        } catch (Throwable t) {
            Logger.getLogger(getClass()).warn(t);
            return null;
        }
    }

    class WrapValue {

        Object value;

        String name;

        public WrapValue(Object value) {
            super();
            this.value = value;
            if (value instanceof FileItem) {
                name = ((FileItem) value).getName();
            }
        }

        String getName() {
            return name;
        }

        InputStream openStream() throws IOException {
            if (value instanceof FileItem) {
                return ((FileItem) value).getInputStream();
            } else if (value instanceof String) {
                return new ByteArrayInputStream(((String) value).getBytes());
            } else if (value instanceof byte[]) {
                return new ByteArrayInputStream((byte[]) value);
            } else if (value instanceof InputStream) {
                return (InputStream) value;
            } else if (value instanceof RenderedImage) {
                java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
                ImageIO.write((RenderedImage) value, "png", output);
                byte[] buff = output.toByteArray();
                return new ByteArrayInputStream(buff);
            } else if (value instanceof File) {
                return new FileInputStream((File) value);
            }
            return null;
        }
    }
}
