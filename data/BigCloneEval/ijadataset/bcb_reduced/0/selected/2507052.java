package jacky.lanlan.song.extension.vraptor2;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.IOUtils;
import org.vraptor.LogicException;
import org.vraptor.LogicFlow;
import org.vraptor.component.LogicMethod;
import org.vraptor.http.VRaptorServletResponse;
import org.vraptor.view.ViewException;

/**
 * 
 * 实现 {@code ZipOut} 的功能。需要依赖 ComponentLookupInterceptor 和 SettingAndValidationInterceptor
 * <p>
 * <i>不需要直接用这个拦截器，请使用 {@code EnhanceInterceptor}</i>
 * 
 * @author Jacky.Song
 */
public class ZipOutIntercepter extends BaseInterceptor {

    @Override
    protected boolean canIntercept(LogicMethod logic) {
        return logic.getMetadata().isAnnotationPresent(ZipOut.class) && logic.getMetadata().getReturnType() == Map.class;
    }

    @Override
    protected void doIntercept(LogicFlow flow) throws LogicException, ViewException {
        try {
            LogicMethod logic = getLogicMethod(flow);
            ZipOut zipOut = logic.getMetadata().getAnnotation(ZipOut.class);
            VRaptorServletResponse resp = getResponse(flow);
            resp.setContentType("application/octet-stream");
            resp.addHeader("Content-disposition", "attachment; filename=" + zipOut.fileName() + ".zip");
            Map<String, byte[]> result = (Map) invokeLogicMethod(flow);
            compressTo(resp.getOutputStream(), result);
        } catch (Exception e) {
            throw new LogicException("ZipOut Error: " + e);
        }
    }

    public static void compressTo(OutputStream os, Map<String, byte[]> datas) {
        ZipOutputStream out = null;
        try {
            CheckedOutputStream cos = new CheckedOutputStream(os, new CRC32());
            out = new ZipOutputStream(cos);
            for (String name : datas.keySet()) {
                compress(name, datas.get(name), out, "");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close(out);
        }
    }

    private static void compress(String eName, byte[] data, ZipOutputStream out, String basedir) throws IOException {
        ZipEntry entry = new ZipEntry(basedir + new String(eName.getBytes(), "utf-8"));
        out.putNextEntry(entry);
        IOUtils.write(data, out);
    }
}
