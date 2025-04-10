package org.apache.zookeeper.graph.servlets;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class StaticContent extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getRequestURI().substring(request.getServletPath().length());
        InputStream resource = ClassLoader.getSystemResourceAsStream("org/apache/zookeeper/graph/resources" + path);
        if (resource == null) {
            response.getWriter().println(path + " not found!");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        while (resource.available() > 0) {
            response.getWriter().write(resource.read());
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
