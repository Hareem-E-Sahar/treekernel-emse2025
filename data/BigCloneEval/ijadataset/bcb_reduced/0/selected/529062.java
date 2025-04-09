package com.ivis.xprocess.web.tapestry.services;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.engine.IEngineService;
import org.apache.tapestry.engine.ILink;
import org.apache.tapestry.services.LinkFactory;

/**
 * A Service class to generate links to Artifacts
 *
 */
public class ArtifactService implements IEngineService {

    HttpServletResponse response;

    private LinkFactory linkFactory;

    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public void setLinkFactory(LinkFactory linkFactory) {
        this.linkFactory = linkFactory;
    }

    public ILink getLink(boolean post, Object parameter) {
        String artifactId = (String) ((Object[]) parameter)[0];
        String label = (String) ((Object[]) parameter)[1];
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("uuid", artifactId);
        parameters.put("label", label);
        return linkFactory.constructLink(this, post, parameters, false);
    }

    public String getName() {
        return "artifact";
    }

    public void service(IRequestCycle cycle) throws IOException {
        String uuid = cycle.getParameter("uuid");
        String label = cycle.getParameter("label");
        byte[] data = getArtifact(uuid);
        response.setHeader("Content-disposition", "attachment; filename=" + label);
        response.setContentType("");
        response.setContentLength(data.length);
        try {
            OutputStream out = response.getOutputStream();
            out.write(data);
        } catch (IOException e) {
            throw new ApplicationRuntimeException(e);
        }
    }

    /**
     * @param uuid
     * @return the Artifact as bytes
     */
    public static byte[] getArtifact(String uuid) {
        if (uuid == null) {
            return new byte[] {};
        }
        InputStream input;
        try {
            input = new BufferedInputStream(new FileInputStream(uuid));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            for (; ; ) {
                int noBytesRead;
                try {
                    noBytesRead = input.read(buf);
                    if (noBytesRead == -1) {
                        return output.toByteArray();
                    }
                    output.write(buf, 0, noBytesRead);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return new byte[] {};
        }
    }
}
