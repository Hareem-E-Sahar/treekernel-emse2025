package fitnesse.responders.files;

import fitnesse.http.*;
import fitnesse.*;
import fitnesse.responders.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.ParseException;

public class RepoFileResponder implements Responder {

    public static final String RESPONDER_KEY = "~";

    private static FileNameMap fileNameMap = URLConnection.getFileNameMap();

    public String resource;

    public File requestedFile;

    public Date lastModifiedDate;

    public String lastModifiedDateString;

    public static Responder makeResponder(Request request, String rootPath) throws Exception {
        String resource = request.getResource();
        if (resource.indexOf('~') == 0) {
            resource = resource.substring(1);
        }
        if (fileNameHasSpaces(resource)) resource = restoreRealSpacesInFileName(resource);
        File requestedFile = new File(rootPath + "/" + resource);
        if (!requestedFile.exists()) return new NotFoundResponder();
        if (requestedFile.isDirectory()) return new DirectoryResponder(resource, requestedFile); else return new RepoFileResponder(resource, requestedFile);
    }

    public RepoFileResponder(String resource, File requestedFile) {
        this.resource = resource;
        this.requestedFile = requestedFile;
    }

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        InputStreamResponse response = new InputStreamResponse();
        determineLastModifiedInfo();
        if (isNotModified(request)) return createNotModifiedResponse(); else {
            response.setBody(requestedFile);
            setContentType(requestedFile, response);
            response.setLastModifiedHeader(lastModifiedDateString);
        }
        return response;
    }

    public static boolean fileNameHasSpaces(String resource) {
        return resource.indexOf("%20") != 0;
    }

    public static String restoreRealSpacesInFileName(String resource) throws Exception {
        return URLDecoder.decode(resource, "UTF-8");
    }

    String getResource() {
        return resource;
    }

    private boolean isNotModified(Request request) {
        if (request.hasHeader("If-Modified-Since")) {
            String queryDateString = (String) request.getHeader("If-Modified-Since");
            try {
                Date queryDate = Response.makeStandardHttpDateFormat().parse(queryDateString);
                if (!queryDate.before(lastModifiedDate)) return true;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private Response createNotModifiedResponse() {
        Response response = new SimpleResponse();
        response.setStatus(304);
        response.addHeader("Date", Response.makeStandardHttpDateFormat().format(new Date()));
        response.addHeader("Cache-Control", "private");
        response.setLastModifiedHeader(lastModifiedDateString);
        return response;
    }

    private void determineLastModifiedInfo() {
        lastModifiedDate = new Date(requestedFile.lastModified());
        lastModifiedDateString = Response.makeStandardHttpDateFormat().format(lastModifiedDate);
        try {
            lastModifiedDate = Response.makeStandardHttpDateFormat().parse(lastModifiedDateString);
        } catch (java.text.ParseException jtpe) {
            jtpe.printStackTrace();
        }
    }

    private void setContentType(File file, Response response) {
        String contentType = getContentType(file.getName());
        response.setContentType(contentType);
    }

    public static String getContentType(String filename) {
        String contentType = fileNameMap.getContentTypeFor(filename);
        if (contentType == null) {
            if (filename.endsWith(".css")) {
                contentType = "text/css";
            } else if (filename.endsWith(".jar")) {
                contentType = "application/x-java-archive";
            } else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
                contentType = "text/html";
            } else {
                contentType = "text/plain";
            }
        }
        return contentType;
    }
}
